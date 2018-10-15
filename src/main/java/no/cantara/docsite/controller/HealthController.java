package no.cantara.docsite.controller;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.cantara.docsite.cache.CacheStore;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.bind.JsonbBuilder;
import java.util.concurrent.atomic.AtomicInteger;

import static java.net.HttpURLConnection.HTTP_OK;

public class HealthController implements HttpHandler {

    private final CacheStore cacheStore;

    public HealthController(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        JsonObjectBuilder cacheBuilder = Json.createObjectBuilder();

        {
            AtomicInteger count = new AtomicInteger(0);
            cacheStore.getProjects().iterator().forEachRemaining(a -> count.incrementAndGet());
            cacheBuilder.add("projects", count.get());
        }

        {
            AtomicInteger count = new AtomicInteger(0);
            cacheStore.getPages().iterator().forEachRemaining(a -> count.incrementAndGet());
            cacheBuilder.add("pages", count.get());
        }

        {
            AtomicInteger count = new AtomicInteger(0);
            cacheStore.getCommits().iterator().forEachRemaining(a -> count.incrementAndGet());
            cacheBuilder.add("commits", count.get());
        }

        {
            AtomicInteger count = new AtomicInteger(0);
            cacheStore.getReleases().iterator().forEachRemaining(a -> count.incrementAndGet());
            cacheBuilder.add("releases", count.get());
        }

        builder.add("cache", cacheBuilder);

        exchange.setStatusCode(HTTP_OK);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(JsonbBuilder.create().toJson(builder.build()));
    }
}
