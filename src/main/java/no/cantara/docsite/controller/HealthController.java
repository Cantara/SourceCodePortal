package no.cantara.docsite.controller;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.health.HealthResource;
import no.cantara.docsite.util.JsonUtil;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.bind.JsonbBuilder;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static java.net.HttpURLConnection.HTTP_OK;

public class HealthController implements HttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(HealthController.class);

    private final DynamicConfiguration configuration;
    private final ExecutorService executorService;
    private final CacheStore cacheStore;

    public HealthController(DynamicConfiguration configuration, ExecutorService executorService, CacheStore cacheStore) {
        this.configuration = configuration;
        this.executorService = executorService;
        this.cacheStore = cacheStore;
    }

    Future<HttpResponse<String>> getGitHubRateLimit() {
        GetGitHubCommand<String> cmd = new GetGitHubCommand<>("gitHubRateLimit", configuration, Optional.empty(),
                "https://api.github.com/rate_limit", HttpResponse.BodyHandlers.ofString());
        Future<HttpResponse<String>> future = cmd.queue();
        return future;
    }

    JsonObject getRateLimitJson(Future<HttpResponse<String>> futureGitHubRateLimit) throws InterruptedException, ExecutionException {
        HttpResponse<String> response = futureGitHubRateLimit.get();
        if (response.statusCode() == HTTP_OK) {
            return JsonbBuilder.create().fromJson(response.body(), JsonObject.class);
        } else {
            return JsonbBuilder.create().fromJson(String.format("{\"error\": \"%s\"}", response.statusCode()), JsonObject.class);
        }
    }

    boolean isGitHubHealtEndpoint(HttpServerExchange exchange) {
        return exchange.getRequestPath().startsWith("/health/github");
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Future<HttpResponse<String>> futureGitHubRateLimit = null;
        if (isGitHubHealtEndpoint(exchange)) {
            futureGitHubRateLimit = getGitHubRateLimit();
        }

        boolean healthyExecutorService = executorService.getThreadPool().getActiveCount() > 0;
        boolean healthyCacheStore = !cacheStore.getCacheManager().isClosed();
        String status = (healthyExecutorService && healthyCacheStore ? "OK" : "FAILURE");

        JsonObjectBuilder builder = Json.createObjectBuilder();

        {
            builder.add("status", status);
            builder.add("version", HealthResource.instance().getVersion());
            builder.add("now", Instant.now().toString());
            builder.add("since", HealthResource.instance().getRunningSince());
        }

        JsonObjectBuilder serviceStatusBuilder = Json.createObjectBuilder();

        {
            serviceStatusBuilder.add("executor-service", healthyExecutorService ? "up" : "terminated");
            serviceStatusBuilder.add("cache-store", healthyExecutorService ? "up" : "down");
            serviceStatusBuilder.add("github-last-seen", Instant.ofEpochMilli(HealthResource.instance().getGitHubLastSeen()).toString());
        }

        builder.add("service-status", serviceStatusBuilder);

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
            executorServiceBuilder.add("max-blocking-queue-size", ExecutorService.BLOCKING_QUEUE_SIZE);
            executorServiceBuilder.add("max-worker-retries", ExecutorService.MAX_RETRIES);
        }

        builder.add("thread-pool", executorServiceBuilder);


        JsonObjectBuilder cacheBuilder = Json.createObjectBuilder();

        {
            builder.add("cache-provider", cacheStore.getCacheManager().getCachingProvider().getDefaultURI().toString());
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
        if (futureGitHubRateLimit != null) {
            builder.add("github-rate-limit", getRateLimitJson(futureGitHubRateLimit));
        }

        exchange.setStatusCode(HTTP_OK);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(JsonUtil.prettyPrint(JsonbBuilder.create().toJson(builder.build())));
    }

}
