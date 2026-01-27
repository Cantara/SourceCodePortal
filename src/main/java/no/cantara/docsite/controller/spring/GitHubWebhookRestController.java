package no.cantara.docsite.controller.spring;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.config.ApplicationProperties;
import no.ssb.config.DynamicConfiguration;
import no.cantara.docsite.domain.github.commits.FetchGitHubCommitRevisionTask;
import no.cantara.docsite.domain.github.commits.GitHubPushCommitEvent;
import no.cantara.docsite.domain.github.contents.FetchGitHubContentsTask;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.health.HealthResource;
import no.cantara.docsite.json.JsonbFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * GitHub Webhook REST Controller for Spring Boot
 * <p>
 * Handles GitHub webhook events for real-time updates to the portal.
 * Replaces the Undertow GithubWebhookController with Spring MVC.
 * <p>
 * Supported Events:
 * - ping: GitHub webhook configuration test
 * - push: Code commits or page commits (triggers cache updates)
 * - create: Tag/branch creation (planned)
 * - release: Release events (planned)
 * <p>
 * Security:
 * - Verifies HMAC-SHA1 signature from X-Hub-Signature header
 * - Uses github.webhook.securityAccessToken from configuration
 * <p>
 * Endpoints:
 * - POST /github/webhook: Receives webhook events from GitHub
 */
@RestController
@RequestMapping("/github")
public class GitHubWebhookRestController {

    private static final Logger LOG = LoggerFactory.getLogger(GitHubWebhookRestController.class);
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final char[] HEX = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    private final ApplicationProperties properties;
    private final DynamicConfiguration configuration;
    private final ExecutorService executorService;
    private final CacheStore cacheStore;

    public GitHubWebhookRestController(ApplicationProperties properties,
                                        DynamicConfiguration configuration,
                                        ExecutorService executorService,
                                        CacheStore cacheStore) {
        this.properties = properties;
        this.configuration = configuration;
        this.executorService = executorService;
        this.cacheStore = cacheStore;
    }

    /**
     * Handle GitHub webhook POST requests
     *
     * @param xHubSignature HMAC-SHA1 signature for payload verification
     * @param xHubEvent     GitHub event type (ping, push, create, release)
     * @param payload       Raw JSON payload from GitHub
     * @return ResponseEntity with appropriate status code
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(value = "X-Hub-Signature", required = false) String xHubSignature,
            @RequestHeader(value = "X-GitHub-Event", required = false) String xHubEvent,
            @RequestBody String payload) {

        LOG.trace("Event -- xHubSignature: {} -- xHubEvent: {} -> Payload:\n{}",
                xHubSignature, xHubEvent, JsonbFactory.prettyPrint(payload));

        // Verify signature
        String secretToken = properties.getGithub().getWebhook().getSecurityAccessToken();
        if (!verifySignature(payload, xHubSignature, secretToken)) {
            LOG.error("GitHub WebHook authorization failed!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        HealthResource.instance().markGitHubLastSeen();
        LOG.debug("GitHub WebHook is authorized..");

        try {
            // Handle different event types
            switch (xHubEvent) {
                case "ping":
                    handlePingEvent();
                    break;

                case "push":
                    handlePushEvent(payload);
                    break;

                case "create":
                    handleCreateEvent(payload);
                    break;

                case "release":
                    handleReleaseEvent(payload);
                    break;

                default:
                    LOG.warn("Received unsupported GitHub event: {}", xHubEvent);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            LOG.error("Error processing GitHub webhook", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Handle GitHub ping event (webhook configuration test)
     */
    private void handlePingEvent() {
        LOG.debug("Received Ping Event!");
    }

    /**
     * Handle GitHub push event (code commits or page commits)
     */
    private void handlePushEvent(String payload) {
        LOG.debug("Received Push Event!");

        GitHubPushCommitEvent pushCommitEvent = JsonbFactory.instance()
                .fromJson(payload, GitHubPushCommitEvent.class);

        // Handle code commits
        if (pushCommitEvent.isCodeCommit()) {
            CacheKey cacheKey = CacheKey.of(
                    pushCommitEvent.repository.owner.name,
                    pushCommitEvent.repository.name,
                    pushCommitEvent.getBranch()
            );

            for (GitHubPushCommitEvent.Commit commit : pushCommitEvent.commits) {
                executorService.queue(new FetchGitHubCommitRevisionTask(
                        configuration,
                        executorService,
                        cacheStore,
                        cacheKey,
                        commit.id
                ));
            }
        }

        // Handle page commits (documentation updates)
        if (pushCommitEvent.isPageCommit()) {
            CacheKey cacheKey = CacheKey.of(
                    pushCommitEvent.repository.owner.name,
                    pushCommitEvent.repository.name,
                    pushCommitEvent.getBranch()
            );

            CacheRepositoryKey cacheRepositoryKey = cacheStore.getCacheKeys().get(cacheKey);
            ScmRepository repository = cacheStore.getRepositories().get(cacheRepositoryKey);

            // TODO: Handle multiple commits with page changes
            String commitId = pushCommitEvent.headCommit.id;
            for (String modifiedFile : pushCommitEvent.headCommit.modifiedList) {
                executorService.queue(new FetchGitHubContentsTask(
                        configuration,
                        executorService,
                        cacheStore,
                        cacheKey,
                        repository.apiContentsURL,
                        modifiedFile,
                        commitId
                ));
            }
        }
    }

    /**
     * Handle GitHub create event (tag/branch creation)
     */
    private void handleCreateEvent(String payload) {
        LOG.debug("Received Create Event!");
        // TODO: Implement create event handling
    }

    /**
     * Handle GitHub release event
     */
    private void handleReleaseEvent(String payload) {
        LOG.debug("Received Release Event!");
        // TODO: Implement release event handling
    }

    /**
     * Verify HMAC-SHA1 signature from GitHub webhook
     *
     * @param payload   Raw JSON payload
     * @param signature X-Hub-Signature header value (format: "sha1=...")
     * @param secret    Webhook secret token from configuration
     * @return true if signature is valid, false otherwise
     */
    static boolean verifySignature(String payload, String signature, String secret) {
        if (payload == null || signature == null || secret == null) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(payload.getBytes());

            String expected = signature.substring(5);  // Remove "sha1=" prefix
            String actual = new String(encode(rawHmac));

            return expected.equals(actual);

        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException ex) {
            throw new RuntimeException("Error verifying webhook signature: " + ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Encode bytes to hex string
     */
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
}
