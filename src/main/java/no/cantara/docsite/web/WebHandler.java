package no.cantara.docsite.web;

import io.undertow.server.HttpServerExchange;
import no.cantara.docsite.cache.CacheStore;
import no.ssb.config.DynamicConfiguration;

public interface WebHandler {

    boolean handleRequest(DynamicConfiguration configuration, CacheStore cacheStore, ResourceContext resourceContext, WebContext webContext, HttpServerExchange exchange);

}
