package no.cantara.docsite.controller;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.cantara.docsite.util.GitHubWebhookUtility;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;

class GithubWebhookController implements HttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GithubWebhookController.class);
    private final DynamicConfiguration configuration;

    GithubWebhookController(DynamicConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        if (exchange.getRequestURI().endsWith("webhook")) {
            try {
                String xHubSignature = exchange.getRequestHeaders().getFirst("X-Hub-Signature");
                String xHubEvent = exchange.getRequestHeaders().getFirst("X-GitHub-Event");

                StringBuilder payloadBuilder = new StringBuilder();
                exchange.getRequestReceiver().receiveFullString((exchangeReceiver, message) -> payloadBuilder.append(message));
                String payload = payloadBuilder.toString();

                LOG.trace("Event -- xHubSignature: {} -- xHubEvent: {} -> Payload:\n{}", xHubSignature, xHubEvent, payload);

                if (!GitHubWebhookUtility.verifySignature(payload, xHubSignature, configuration.evaluateToString("github.webhook.securityAccessToken"))) {
                    LOG.error("GitHub WebHook authorization failed!");
                    exchange.setStatusCode(HTTP_FORBIDDEN);
                    return;
                } else {
                    LOG.trace("GitHub WebHook is authorized..");
                }


                // ------------------------------------------------------------------------------------------------------
                // Github Ping Event
                // ------------------------------------------------------------------------------------------------------

                if ("ping".equals(xHubEvent)) {
                    LOG.trace("Received Ping Event!");
                }

                // ------------------------------------------------------------------------------------------------------
                // Github Module update Event
                // ------------------------------------------------------------------------------------------------------

                //
                // compare module update with current module
                //   new sub-modules (sjekk om id finnes i internalMap. sjekk også om en id er tatt bort fra en module.
                //   lag en ny map hvor id-er legges til for en new-module og sammenlikn med gammel module
                //   name changes
                // fetch new page if there are changes
                // 5144b64a-cc59-11e7-9b70-737fe2f2f4fa

                if ("push".equals(xHubEvent)) {

                }

                // ------------------------------------------------------------------------------------------------------
                // Github Pages Event
                // ------------------------------------------------------------------------------------------------------

                if ("push".equals(xHubEvent)) {

                }

                // ------------------------------------------------------------------------------------------------------
                // Github Commit Event
                // ------------------------------------------------------------------------------------------------------

                if ("push".equals(xHubEvent)) {

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
        exchange.getResponseSender().send("Unsupported resource path: " + exchange.getRequestPath());
    }
}
