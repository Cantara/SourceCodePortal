package no.cantara.docsite.controller;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.executor.ExecutorThreadPool;
import no.cantara.docsite.health.HealthResource;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.bind.JsonbBuilder;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static java.net.HttpURLConnection.HTTP_OK;

public class HealthController implements HttpHandler {

    private final ExecutorThreadPool executorService;
    private final CacheStore cacheStore;

    public HealthController(ExecutorThreadPool executorService, CacheStore cacheStore) {
        this.executorService = executorService;
        this.cacheStore = cacheStore;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        {
            builder.add("status", "OK");
            builder.add("version", HealthResource.instance().getVersion());
            builder.add("now", Instant.now().toString());
            builder.add("since", HealthResource.instance().getRunningSince());
            builder.add("cache-provider", cacheStore.getCacheManager().getCachingProvider().getDefaultURI().toString());
        }


        JsonObjectBuilder executorServiceBuilder = Json.createObjectBuilder();

        {
            executorServiceBuilder.add("core-pool-size", executorService.getThreadPool().getCorePoolSize());
            executorServiceBuilder.add("pool-size", executorService.getThreadPool().getPoolSize());
            executorServiceBuilder.add("task-count", executorService.getThreadPool().getTaskCount());
            executorServiceBuilder.add("completed-task-count", executorService.getThreadPool().getCompletedTaskCount());
            executorServiceBuilder.add("active-count", executorService.getThreadPool().getActiveCount());
            executorServiceBuilder.add("maximum-pool-size", executorService.getThreadPool().getMaximumPoolSize());
            executorServiceBuilder.add("largest-pool-size", executorService.getThreadPool().getLargestPoolSize());
            executorServiceBuilder.add("blocking-queue-size", executorService.getThreadPool().getQueue().size());
            executorServiceBuilder.add("max-blocking-queue-size", ExecutorThreadPool.BLOCKING_QUEUE_SIZE);
            executorServiceBuilder.add("max-worker-retries", ExecutorThreadPool.MAX_RETRIES);
        }

        builder.add("thread-pool", executorServiceBuilder);


        JsonObjectBuilder cacheBuilder = Json.createObjectBuilder();

        {
            AtomicInteger count = new AtomicInteger(0);
            cacheStore.getCacheKeys().iterator().forEachRemaining(a -> count.incrementAndGet());
            cacheBuilder.add("cache-keys", count.get());
        }

        {
            cacheBuilder.add("groups", cacheStore.getGroups().size());
        }

        {
            AtomicInteger count = new AtomicInteger(0);
            cacheStore.getRepositoryGroups().iterator().forEachRemaining(a -> count.incrementAndGet());
            cacheBuilder.add("repositories", count.get());
        }

        {
            AtomicInteger count = new AtomicInteger(0);
            cacheStore.getProjects().iterator().forEachRemaining(a -> count.incrementAndGet());
            cacheBuilder.add("maven-projects", count.get());
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

        builder.add("cache-provider", cacheStore.getCacheManager().getCachingProvider().getDefaultURI().toString());
        builder.add("cache", cacheBuilder);

        exchange.setStatusCode(HTTP_OK);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(JsonbBuilder.create().toJson(builder.build()));
    }
}
