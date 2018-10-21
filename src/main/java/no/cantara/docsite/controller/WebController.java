package no.cantara.docsite.controller;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.Repository;
import no.cantara.docsite.web.ThymeleafViewEngineProcessor;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class WebController implements HttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(WebController.class);
    static final WebContext[] VALID_CONTEXTS = new WebContext[]{
            WebContext.of("/index", ""),
            WebContext.of("/home", "docs")
    };

    final DynamicConfiguration configuration;
    final CacheStore cacheStore;

    public WebController(DynamicConfiguration configuration, CacheStore cacheStore) {
        this.configuration = configuration;
        this.cacheStore = cacheStore;
    }

    static boolean isValidContext(HttpServerExchange exchange) {
        return Arrays.asList(VALID_CONTEXTS).stream().filter(f -> exchange.getRequestPath().startsWith(f.uri)).count() > 0;
    }

    static WebContext getWebContext(HttpServerExchange exchange) {
        return Arrays.asList(VALID_CONTEXTS).stream().filter(f -> exchange.getRequestPath().startsWith(f.uri)).findFirst().get();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        Map<String, Object> templateVariables = new HashMap<>();

        templateVariables.put("groups", cacheStore.getGroups());
        Map<String, List<Repository>> repositoryGroups = new LinkedHashMap<>();

        for (String group : cacheStore.getGroups()) {
            repositoryGroups.put(group, cacheStore.getRepositoryGroupsByGroupId(group));
        }

        templateVariables.put("repositoryGroups", repositoryGroups);
        if (ThymeleafViewEngineProcessor.processView(exchange, getWebContext(exchange).subContext, templateVariables)) {
            return;
        }

        exchange.setStatusCode(404);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Not found: " + exchange.getRequestPath());
    }

    static class WebContext {
        public final String uri;        // /index /home
        public final String subContext; // ""      docs

        WebContext(String uri, String subContext) {
            this.uri = uri;
            this.subContext = subContext;
        }

        static WebContext of(String uri, String root) {
            return new WebContext(uri, root);
        }
    }
}
