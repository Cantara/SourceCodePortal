package no.cantara.docsite.controller;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.cantara.docsite.cache.CacheHelper;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.domain.config.RepoConfig;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.ScheduledExecutorService;
import no.cantara.docsite.health.GitHubRateLimit;
import no.cantara.docsite.health.HealthResource;
import no.cantara.docsite.json.JsonbFactory;
import no.cantara.docsite.web.ResourceContext;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.net.HttpURLConnection.HTTP_OK;

public class HealthController implements HttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(HealthController.class);

    private final DynamicConfiguration configuration;
    private final ExecutorService executorService;
    private final CacheStore cacheStore;
    private ScheduledExecutorService scheduledExecutorService;
    private ResourceContext resourceContext;

    public HealthController(DynamicConfiguration configuration, ExecutorService executorService, ScheduledExecutorService scheduledExecutorService, CacheStore cacheStore, ResourceContext resourceContext) {
        this.configuration = configuration;
        this.executorService = executorService;
        this.scheduledExecutorService = scheduledExecutorService;
        this.cacheStore = cacheStore;
        this.resourceContext = resourceContext;
    }

    Future<HttpResponse<String>> getGitHubRateLimit() {
        GetGitHubCommand<String> cmd = new GetGitHubCommand<>("gitHubRateLimit", configuration, Optional.empty(),
                "https://api.github.com/rate_limit", HttpResponse.BodyHandlers.ofString());
        Future<HttpResponse<String>> future = cmd.queue();
        return future;
    }

    GitHubRateLimit getRateLimitJson(Future<HttpResponse<String>> futureGitHubRateLimit) throws InterruptedException, ExecutionException {
        HttpResponse<String> response = futureGitHubRateLimit.get();
        if (response.statusCode() == HTTP_OK) {
            return JsonbFactory.instance().fromJson(response.body(), GitHubRateLimit.class);
        } else {
//            return JsonbFactory.instance().fromJson(String.format("{\"error\": \"%s\"}", response.statusCode()), JsonObject.class);
            return null;
        }
    }

    boolean isGitHubHealtEndpoint(HttpServerExchange exchange) {
        return resourceContext.exactMatch("/health/github");
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Future<HttpResponse<String>> futureGitHubRateLimit = null;
        if (isGitHubHealtEndpoint(exchange)) {
            futureGitHubRateLimit = getGitHubRateLimit();
        }

        boolean healthyExecutorService = executorService.getThreadPool().getActiveCount() > 0;
        boolean healthyScheduledExecutorService = !scheduledExecutorService.getThreadPool().isTerminated();
        boolean healthyCacheStore = !cacheStore.getCacheManager().isClosed();
        String status = (healthyExecutorService && healthyScheduledExecutorService && healthyCacheStore ? "OK" : "FAILURE");

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
            serviceStatusBuilder.add("scheduled-executor-service", healthyScheduledExecutorService ? "up" : "terminated");
            serviceStatusBuilder.add("cache-store", healthyExecutorService ? "up" : "down");
            serviceStatusBuilder.add("github-last-seen", Instant.ofEpochMilli(HealthResource.instance().getGitHubLastSeen()).toString());

            scheduledExecutorService.getScheduledWorkers().forEach(scheduledWorker -> {
                Long scheduledWorkerLastSeen = HealthResource.instance().getScheduledWorkerLastSeen(scheduledWorker.id);
                serviceStatusBuilder.add(scheduledWorker.id + "-last-run", Instant.ofEpochMilli(scheduledWorkerLastSeen).toString());

                if ("cantara-wiki".equals(scheduledWorker.id)) {
                    serviceStatusBuilder.add("cantara-wiki-last-seen", Instant.ofEpochMilli(HealthResource.instance().getCantaraWikiLastSeen()).toString());
                } else if ("jenkins".equals(scheduledWorker.id)) {
                    serviceStatusBuilder.add("jenkins-last-seen", Instant.ofEpochMilli(HealthResource.instance().getJenkinLastSeen()).toString());
                } else if ("snyk".equals(scheduledWorker.id)) {
                    serviceStatusBuilder.add("snyk-last-seen", Instant.ofEpochMilli(HealthResource.instance().getSnykLastSeen()).toString());
                } else if ("shields".equals(scheduledWorker.id)) {
                    serviceStatusBuilder.add("shields-last-seen", Instant.ofEpochMilli(HealthResource.instance().getShieldsLastSeen()).toString());
                }

                Long nextRun = scheduledWorkerLastSeen + scheduledWorker.timeUnit.toMillis(scheduledWorker.timeUnit.convert(scheduledWorker.period, scheduledWorker.timeUnit));
                serviceStatusBuilder.add(scheduledWorker.id + "-next-run", Instant.ofEpochMilli(nextRun).toString());
            });
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
            executorServiceBuilder.add("worker-queue-remaining", executorService.countRemainingWorkerTasks());
            executorServiceBuilder.add("max-worker-retries", ExecutorService.MAX_RETRIES);
        }

        builder.add("thread-pool", executorServiceBuilder);


        JsonObjectBuilder scheduledExecutorServiceBuilder = Json.createObjectBuilder();

        {
            scheduledExecutorService.getScheduledWorkers().forEach(scheduledWorker -> {
                JsonObjectBuilder scheduledWorkedBuilder = Json.createObjectBuilder();
                scheduledWorkedBuilder.add("initial-delay", scheduledWorker.initialDelay);
                scheduledWorkedBuilder.add("period", scheduledWorker.period);
                scheduledWorkedBuilder.add("time-unit", scheduledWorker.timeUnit.name());
                scheduledWorkedBuilder.add("worker-task-count", scheduledWorker.getTaskCount());
                scheduledExecutorServiceBuilder.add(scheduledWorker.id, scheduledWorkedBuilder);
            });

        }

        builder.add("scheduled-thread-pool", scheduledExecutorServiceBuilder);


        JsonObjectBuilder cacheBuilder = Json.createObjectBuilder();

        {
            builder.add("cache-provider", cacheStore.getCacheManager().getCachingProvider().getDefaultURI().toString());
            cacheBuilder.add("cache-keys", CacheHelper.cacheSize(cacheStore.getCacheKeys()));
            cacheBuilder.add("cache-group-keys", CacheHelper.cacheSize(cacheStore.getCacheRepositoryKeys()));
            cacheBuilder.add("groups", cacheStore.getRepositoryConfig().getConfig().repos.get(RepoConfig.ScmProvider.GITHUB).size());
            cacheBuilder.add("repositories", CacheHelper.cacheSize(cacheStore.getRepositories()));
            cacheBuilder.add("maven-projects", CacheHelper.cacheSize(cacheStore.getMavenProjects()));
            cacheBuilder.add("contents", CacheHelper.cacheSize(cacheStore.getReadmeContents()));
            cacheBuilder.add("commits", CacheHelper.cacheSize(cacheStore.getCommits()));
            cacheBuilder.add("releases", CacheHelper.cacheSize(cacheStore.getReleases()));
            cacheBuilder.add("confluence-pages", CacheHelper.cacheSize(cacheStore.getCantaraWiki()));
            cacheBuilder.add("jenkins-build-status", CacheHelper.cacheSize(cacheStore.getJenkinsBuildStatus()));
            cacheBuilder.add("snyk-test-status", CacheHelper.cacheSize(cacheStore.getSnykTestStatus()));
            cacheBuilder.add("shields-issues-status", CacheHelper.cacheSize(cacheStore.getSheildIssuesStatus()));
            cacheBuilder.add("shields-commits-status", CacheHelper.cacheSize(cacheStore.getSheildCommitsStatus()));
            cacheBuilder.add("shields-releases-status", CacheHelper.cacheSize(cacheStore.getShieldReleasesStatus()));
        }

        builder.add("cache-provider", cacheStore.getCacheManager().getCachingProvider().getDefaultURI().toString());
        builder.add("cache", cacheBuilder);

        if (futureGitHubRateLimit != null) {
            GitHubRateLimit rateLimitJson = getRateLimitJson(futureGitHubRateLimit);
            builder.add("github-rate-limit", JsonbFactory.asJsonObject(rateLimitJson.toString()));
        }

        builder.add("threads", HealthResource.instance().getThreadInfo());

        exchange.setStatusCode(HTTP_OK);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(JsonbFactory.prettyPrint(JsonbFactory.instance().toJson(builder.build())));
    }

}
