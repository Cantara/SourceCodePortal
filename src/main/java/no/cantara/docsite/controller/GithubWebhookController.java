package no.cantara.docsite.controller;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GithubWebhookController implements HttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GithubWebhookController.class);

    GithubWebhookController() {
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        if (exchange.getRequestURI().endsWith("webhook")) {
            String xHubSignature = exchange.getRequestHeaders().getFirst("X-Hub-Signature");
            String xHubEvent = exchange.getRequestHeaders().getFirst("X-GitHub-Event");

            StringBuilder payload = new StringBuilder();
            exchange.getRequestReceiver().receiveFullString((exchangeReceiver, message) -> payload.append(message));

//            LOG.trace("Event: {} -> Payload: {}", xHubEvent, payload);

            if ("push".equals(xHubEvent)) {

            }

            exchange.setStatusCode(200);
            return;
        }

        exchange.setStatusCode(404);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Unsupported resource path: " + exchange.getRequestPath());
    }
}
