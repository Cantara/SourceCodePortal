package no.cantara.docsite.controller;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.github.commits.FetchGitHubCommitRevisionTask;
import no.cantara.docsite.domain.github.commits.GitHubPushCommitEvent;
import no.cantara.docsite.domain.github.contents.FetchGitHubContentsTask;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.health.HealthResource;
import no.cantara.docsite.util.JsonbFactory;
import no.cantara.docsite.web.ResourceContext;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;

class GithubWebhookController implements HttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GithubWebhookController.class);
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final char[] HEX = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    private final DynamicConfiguration configuration;
    private final ExecutorService executorService;
    private final CacheStore cacheStore;
    private ResourceContext resourceContext;

    GithubWebhookController(DynamicConfiguration configuration, ExecutorService executorService, CacheStore cacheStore, ResourceContext resourceContext) {
        this.configuration = configuration;
        this.executorService = executorService;
        this.cacheStore = cacheStore;
        this.resourceContext = resourceContext;
    }

    static boolean verifySignature(String payload, String signature, String secret) {
        if (payload == null || signature == null || secret == null) {
            return false;
        }
        boolean isValid;
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(payload.getBytes());
            String expected = signature.substring(5);
            String actual = new String(encode(rawHmac));
            isValid = expected.equals(actual);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException ex) {
            throw new RuntimeException(ex.getLocalizedMessage());
        }
        return isValid;
    }

    static char[] encode(byte[] bytes) {
        final int amount = bytes.length;
        char[] result = new char[2 * amount];
        int j = 0;
        for (int i = 0; i < amount; i++) {
            result[j++] = HEX[(0xF0 & bytes[i]) >>> 4];
            result[j++] = HEX[(0x0F & bytes[i])];
        }
        return result;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        if (resourceContext.getLast().get().id.equals("webhook") && "post".equalsIgnoreCase(exchange.getRequestMethod().toString())) {
            try {
                String xHubSignature = exchange.getRequestHeaders().getFirst("X-Hub-Signature");
                String xHubEvent = exchange.getRequestHeaders().getFirst("X-GitHub-Event");

                StringBuilder payloadBuilder = new StringBuilder();
                exchange.getRequestReceiver().receiveFullString((exchangeReceiver, message) -> payloadBuilder.append(message));
                String payload = payloadBuilder.toString();

                LOG.trace("Event -- xHubSignature: {} -- xHubEvent: {} -> Payload:\n{}", xHubSignature, xHubEvent, JsonbFactory.prettyPrint(payload));

                if (!verifySignature(payload, xHubSignature, configuration.evaluateToString("github.webhook.securityAccessToken"))) {
                    LOG.error("GitHub WebHook authorization failed!");
                    exchange.setStatusCode(HTTP_FORBIDDEN);
                    return;
                }

                HealthResource.instance().markGitHubLastSeen();
                LOG.debug("GitHub WebHook is authorized..");

                // ------------------------------------------------------------------------------------------------------
                // Github Ping Event
                // ------------------------------------------------------------------------------------------------------

                if ("ping".equals(xHubEvent)) {
                    LOG.debug("Received Ping Event!");
                }

                // ------------------------------------------------------------------------------------------------------
                // Github Module update Event
                // ------------------------------------------------------------------------------------------------------

                if ("push".equals(xHubEvent)) {
                    LOG.debug("Received Push Event!");

                    GitHubPushCommitEvent pushCommitEvent = JsonbFactory.instance().fromJson(payload, GitHubPushCommitEvent.class);

                    // ------------------------------------------------------------------------------------------------------
                    // Github Commit Event
                    // ------------------------------------------------------------------------------------------------------
                    if (pushCommitEvent.isCodeCommit()) {
                        CacheKey cacheKey = CacheKey.of(pushCommitEvent.repository.owner.name, pushCommitEvent.repository.name, pushCommitEvent.getBranch());
                        for (GitHubPushCommitEvent.Commit commit : pushCommitEvent.commits) {
                            executorService.queue(new FetchGitHubCommitRevisionTask(configuration, executorService, cacheStore, cacheKey, commit.id));
                        }
                    }

                    // ------------------------------------------------------------------------------------------------------
                    // Github Page Commit Event
                    // ------------------------------------------------------------------------------------------------------
                    if (pushCommitEvent.isPageCommit()) {
                        CacheKey cacheKey = CacheKey.of(pushCommitEvent.repository.owner.name, pushCommitEvent.repository.name, pushCommitEvent.getBranch());
                        CacheRepositoryKey cacheRepositoryKey = cacheStore.getCacheKeys().get(cacheKey);
                        ScmRepository repository = cacheStore.getRepositories().get(cacheRepositoryKey);
                        // TODO if there are page changes in multiple commits they will not be handled
                        String commitId = pushCommitEvent.headCommit.id;
                        for (String modifiedFile : pushCommitEvent.headCommit.modifiedList) {
                            executorService.queue(new FetchGitHubContentsTask(configuration, executorService, cacheStore, cacheKey, repository.apiContentsURL, modifiedFile, commitId));
                        }
                    }
                }

                // ------------------------------------------------------------------------------------------------------
                // Github Create Tag Event
                // ------------------------------------------------------------------------------------------------------

                if ("create".equals(xHubEvent)) {

                }

                // ------------------------------------------------------------------------------------------------------
                // Github Release Event
                // ------------------------------------------------------------------------------------------------------

                if ("release".equals(xHubEvent)) {

                }

                exchange.setStatusCode(200);
                return;
//            } catch (UnsupportedEncodingException e) {
            } catch (Exception e) {
                e.printStackTrace();
                exchange.setStatusCode(HTTP_BAD_REQUEST);
                return;
            }
        }

        exchange.setStatusCode(404);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send(exchange.getRequestMethod() + " " + exchange.getRequestPath() + " NOT supported!");
    }
}
