package no.cantara.docsite.controller;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.executor.ExecutorService;
import no.ssb.config.DynamicConfiguration;

public class ApplicationController implements HttpHandler {

    private final String corsAllowOrigin;
    private final String corsAllowHeaders;
    private final boolean corsAllowOriginTest;
    private final int undertowPort;
    private final DynamicConfiguration configuration;
    private final ExecutorService executorService;
    private final CacheStore cacheStore;

    public ApplicationController(String corsAllowOrigin, String corsAllowHeaders, boolean corsAllowOriginTest, int undertowPort, DynamicConfiguration configuration, ExecutorService executorService, CacheStore cacheStore) {
        this.corsAllowOrigin = corsAllowOrigin;
        this.corsAllowHeaders = corsAllowHeaders;
        this.corsAllowOriginTest = corsAllowOriginTest;
        this.undertowPort = undertowPort;
        this.configuration = configuration;
        this.executorService = executorService;
        this.cacheStore = cacheStore;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String requestPath = exchange.getRequestPath();

        // NOTE: CORSController cannot be shared across requests or threads
        CORSController cors = new CORSController(corsAllowOrigin, corsAllowHeaders, corsAllowOriginTest, undertowPort);

        cors.handleRequest(exchange);

        if (requestPath.trim().length() <= 1 && !cors.isOptionsRequest()) {
            exchange.setStatusCode(StatusCodes.FOUND);
            exchange.getResponseHeaders().put(Headers.LOCATION, "/docs/home");
            exchange.endExchange();
            return;
        }

        if (cors.isBadRequest()) {
            return;
        }

        if (cors.isOptionsRequest()) {
            cors.doOptions();
            return;
        }

        cors.handleValidRequest();

        if (requestPath.startsWith("/ping")) {
            new PingController().handleRequest(exchange);
            return;
        }

        if (requestPath.startsWith("/health")) {
            new HealthController(configuration, executorService, cacheStore).handleRequest(exchange);
            return;
        }

        if (requestPath.startsWith("/echo")) {
            new EchoController().handleRequest(exchange);
            return;
        }

        if (requestPath.startsWith("/github")) {
            new GithubWebhookController(configuration, executorService, cacheStore).handleRequest(exchange);
            return;
        }

        if (requestPath.startsWith("/css")) {
            new StaticContentController(configuration, "META-INF/views", "text/css").handleRequest(exchange);
            return;
        }

        if (requestPath.startsWith("/js")) {
            new StaticContentController(configuration, "META-INF/js", "application/javascript").handleRequest(exchange);
            return;
        }

        if (requestPath.startsWith("/docs")) {
            new DocsController(configuration, cacheStore).handleRequest(exchange);
            return;
        }

        exchange.setStatusCode(400);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        String namespace = requestPath.substring(1, Math.max(requestPath.substring(1).indexOf("/") + 1, requestPath.length()));
        exchange.getResponseSender().send("Unsupported namespace: \"" + namespace + "\"");
    }
}
