package no.cantara.docsite.controller;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.web.ResourceContext;
import no.cantara.docsite.web.WebContext;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

class WebController implements HttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(WebController.class);
    static final WebContext[] VALID_CONTEXTS = new WebContext[]{
            WebContext.of("/index", "", true, "index.html", new DashboardHandler()),
            WebContext.of("/group", "group", true, "card.html", new CardHandler()),
            WebContext.of("/contents", "contents", false, "content.html", new ContentsHandler()),
            WebContext.of("/commits", "commits", true, "commits.html", new CommitsHandler())
    };

    final DynamicConfiguration configuration;
    final CacheStore cacheStore;
    final ResourceContext resourceContext;

    WebController(DynamicConfiguration configuration, CacheStore cacheStore, ResourceContext resourceContext) {
        this.configuration = configuration;
        this.cacheStore = cacheStore;
        this.resourceContext = resourceContext;
    }

    static boolean isValidContext(HttpServerExchange exchange) {
        ResourceContext context = new ResourceContext(exchange.getRequestPath());
        return Arrays.asList(VALID_CONTEXTS).stream().filter(f -> (f.exactMatch ? context.match(f.uri) : context.subMatch(f.uri))).count() > 0;
    }

    static Optional<WebContext> getWebContext(HttpServerExchange exchange) {
        ResourceContext context = new ResourceContext(exchange.getRequestPath());
        return Arrays.asList(VALID_CONTEXTS).stream().filter(f -> (f.exactMatch ? context.match(f.uri) : context.subMatch(f.uri))).findFirst();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        if (isValidContext(exchange)) {
            WebContext webContext = getWebContext(exchange).get();
            if (webContext.webHandler.handleRequest(configuration, cacheStore, resourceContext, webContext, exchange)){
                return;
            }
        }

        exchange.setStatusCode(404);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Not found: " + exchange.getRequestPath());
    }

}
