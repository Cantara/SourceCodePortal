package no.cantara.docsite.controller;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.web.ThymeleafViewEngineProcessor;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

class DocsController implements HttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DocsController.class);
    private final DynamicConfiguration configuration;
    private final CacheStore cacheStore;

    DocsController(DynamicConfiguration configuration, CacheStore cacheStore) {
        this.configuration = configuration;
        this.cacheStore = cacheStore;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        Map<String, Object> templateVariables = new HashMap<>();
        if (ThymeleafViewEngineProcessor.processView(exchange, templateVariables)) {
            return;
        }

        exchange.setStatusCode(404);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Not found: " + exchange.getRequestPath());
    }

}
