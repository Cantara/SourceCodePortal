package no.cantara.docsite.controller;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.RepositoryConfigLoader;
import no.cantara.docsite.executor.ExecutorThreadPool;

public class ApplicationController implements HttpHandler {

    private String corsAllowOrigin;
    private String corsAllowHeaders;
    private boolean corsAllowOriginTest;
    private int undertowPort;
    private final ExecutorThreadPool executorService;
    private CacheStore cacheStore;
    private final RepositoryConfigLoader configLoader;

    public ApplicationController(String corsAllowOrigin, String corsAllowHeaders, boolean corsAllowOriginTest, int undertowPort, ExecutorThreadPool executorService, CacheStore cacheStore, RepositoryConfigLoader configLoader) {
        this.corsAllowOrigin = corsAllowOrigin;
        this.corsAllowHeaders = corsAllowHeaders;
        this.corsAllowOriginTest = corsAllowOriginTest;
        this.undertowPort = undertowPort;
        this.executorService = executorService;
        this.cacheStore = cacheStore;
        this.configLoader = configLoader;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String requestPath = exchange.getRequestPath();

        // NOTE: CORSController cannot be shared across requests or threads
        CORSController cors = new CORSController(corsAllowOrigin, corsAllowHeaders, corsAllowOriginTest, undertowPort);

        cors.handleRequest(exchange);

        if (requestPath.trim().length() <= 1 && !cors.isOptionsRequest()) {
            exchange.setStatusCode(404);
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
            new HealthController(executorService, cacheStore).handleRequest(exchange);
            return;
        }

        if (requestPath.startsWith("/dump")) {
            new DumpController().handleRequest(exchange);
            return;
        }

        if (requestPath.startsWith("/github")) {
            new GithubController().handleRequest(exchange);
            return;
        }

        if (requestPath.startsWith("/docs")) {
            new DocsController().handleRequest(exchange);
            return;
        }

        exchange.setStatusCode(400);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        String namespace = requestPath.substring(1, Math.max(requestPath.substring(1).indexOf("/") + 1, requestPath.length()));
        exchange.getResponseSender().send("Unsupported namespace: \"" + namespace + "\"");
    }
}
