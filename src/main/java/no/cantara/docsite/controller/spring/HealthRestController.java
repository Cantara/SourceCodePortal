package no.cantara.docsite.controller.spring;

import no.cantara.docsite.cache.CacheHelper;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.config.ApplicationProperties;
import no.cantara.docsite.domain.config.RepoConfig;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.ScheduledExecutorService;
import no.cantara.docsite.health.GitHubRateLimit;
import no.cantara.docsite.health.HealthResource;
import no.cantara.docsite.json.JsonbFactory;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Spring MVC Health Controller
 * <p>
 * Comprehensive health status endpoint for Source Code Portal.
 * Replaces the Undertow-based HealthController with Spring MVC.
 * <p>
 * Endpoints:
 * - GET /health: Comprehensive application health status
 * - GET /health/github: GitHub API rate limit status
 * - GET /health/threads: Detailed thread pool status
 * <p>
 * Backward Compatible:
 * - Maintains identical JSON response format as legacy controller
 * - All fields preserved for monitoring tools compatibility
 * <p>
 * Migration from Undertow:
 * - Before: Manual routing with ResourceContext.exactMatch()
 * - After: Declarative routing with @GetMapping
 * - Before: Manual JSON building with javax.json.Json
 * - After: Automatic JSON serialization via Jackson
 * <p>
 * Code Reduction: ~197 lines → ~280 lines (enhanced functionality)
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration - Task 5 (Enhanced)
 */
@RestController
@RequestMapping("/health")
public class HealthRestController {

    private static final Logger LOG = LoggerFactory.getLogger(HealthRestController.class);

    private final DynamicConfiguration configuration;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final CacheStore cacheStore;

    public HealthRestController(
            DynamicConfiguration configuration,
            ExecutorService executorService,
            ScheduledExecutorService scheduledExecutorService,
            CacheStore cacheStore) {
        this.configuration = configuration;
        this.executorService = executorService;
        this.scheduledExecutorService = scheduledExecutorService;
        this.cacheStore = cacheStore;
    }

    /**
     * Comprehensive health status
     * <p>
     * GET /health
     * <p>
     * Returns JSON with:
     * - status: OK or FAILURE
     * - version: Application version
     * - now: Current timestamp
     * - since: Application start time
     * - service-status: All internal services status
     * - thread-pool: Executor service statistics
     * - scheduled-thread-pool: Scheduled executor statistics
     * - cache: Cache statistics for all caches
     *
     * @return Comprehensive health status map
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        boolean healthyExecutorService = executorService.getThreadPool().getActiveCount() > 0;
        boolean healthyScheduledExecutorService = !scheduledExecutorService.getThreadPool().isTerminated();
        boolean healthyCacheStore = !cacheStore.getCacheManager().isClosed();
        String status = (healthyExecutorService && healthyScheduledExecutorService && healthyCacheStore) ? "OK" : "FAILURE";

        Map<String, Object> response = new LinkedHashMap<>();

        // Basic status
        response.put("status", status);
        response.put("version", HealthResource.instance().getVersion());
        response.put("now", Instant.now().toString());
        response.put("since", HealthResource.instance().getRunningSince());

        // Service status
        Map<String, Object> serviceStatus = new LinkedHashMap<>();
        serviceStatus.put("executor-service", healthyExecutorService ? "up" : "terminated");
        serviceStatus.put("scheduled-executor-service", healthyScheduledExecutorService ? "up" : "terminated");
        serviceStatus.put("cache-store", healthyCacheStore ? "up" : "down");
        serviceStatus.put("github-last-seen", Instant.ofEpochMilli(HealthResource.instance().getGitHubLastSeen()).toString());

        // Scheduled workers status
        scheduledExecutorService.getScheduledWorkers().forEach(scheduledWorker -> {
            Long scheduledWorkerLastSeen = HealthResource.instance().getScheduledWorkerLastSeen(scheduledWorker.id);
            serviceStatus.put(scheduledWorker.id + "-last-run", Instant.ofEpochMilli(scheduledWorkerLastSeen).toString());

            if ("cantara-wiki".equals(scheduledWorker.id)) {
                serviceStatus.put("cantara-wiki-last-seen", Instant.ofEpochMilli(HealthResource.instance().getCantaraWikiLastSeen()).toString());
            } else if ("jenkins".equals(scheduledWorker.id)) {
                serviceStatus.put("jenkins-last-seen", Instant.ofEpochMilli(HealthResource.instance().getJenkinLastSeen()).toString());
            } else if ("snyk".equals(scheduledWorker.id)) {
                serviceStatus.put("snyk-last-seen", Instant.ofEpochMilli(HealthResource.instance().getSnykLastSeen()).toString());
            } else if ("shields".equals(scheduledWorker.id)) {
                serviceStatus.put("shields-last-seen", Instant.ofEpochMilli(HealthResource.instance().getShieldsLastSeen()).toString());
            }

            Long nextRun = scheduledWorkerLastSeen + scheduledWorker.timeUnit.toMillis(scheduledWorker.timeUnit.convert(scheduledWorker.period, scheduledWorker.timeUnit));
            serviceStatus.put(scheduledWorker.id + "-next-run", Instant.ofEpochMilli(nextRun).toString());
        });

        response.put("service-status", serviceStatus);

        // Thread pool statistics
        Map<String, Object> threadPoolStats = new LinkedHashMap<>();
        threadPoolStats.put("core-pool-size", executorService.getThreadPool().getCorePoolSize());
        threadPoolStats.put("pool-size", executorService.getThreadPool().getPoolSize());
        threadPoolStats.put("task-count", executorService.getThreadPool().getTaskCount());
        threadPoolStats.put("completed-task-count", executorService.getThreadPool().getCompletedTaskCount());
        threadPoolStats.put("active-count", executorService.getThreadPool().getActiveCount());
        threadPoolStats.put("maximum-pool-size", executorService.getThreadPool().getMaximumPoolSize());
        threadPoolStats.put("largest-pool-size", executorService.getThreadPool().getLargestPoolSize());
        threadPoolStats.put("blocking-queue-size", executorService.getThreadPool().getQueue().size());
        threadPoolStats.put("max-blocking-queue-size", ExecutorService.BLOCKING_QUEUE_SIZE);
        threadPoolStats.put("worker-queue-remaining", executorService.countRemainingWorkerTasks());
        threadPoolStats.put("max-worker-retries", ExecutorService.MAX_RETRIES);
        response.put("thread-pool", threadPoolStats);

        // Scheduled thread pool statistics
        Map<String, Object> scheduledThreadPoolStats = new LinkedHashMap<>();
        scheduledExecutorService.getScheduledWorkers().forEach(scheduledWorker -> {
            Map<String, Object> workerStats = new LinkedHashMap<>();
            workerStats.put("initial-delay", scheduledWorker.initialDelay);
            workerStats.put("period", scheduledWorker.period);
            workerStats.put("time-unit", scheduledWorker.timeUnit.name());
            workerStats.put("worker-task-count", scheduledWorker.getTaskCount());
            scheduledThreadPoolStats.put(scheduledWorker.id, workerStats);
        });
        response.put("scheduled-thread-pool", scheduledThreadPoolStats);

        // Cache statistics
        Map<String, Object> cacheStats = new LinkedHashMap<>();
        cacheStats.put("cache-keys", CacheHelper.cacheSize(cacheStore.getCacheKeys()));
        cacheStats.put("cache-group-keys", CacheHelper.cacheSize(cacheStore.getCacheRepositoryKeys()));
        cacheStats.put("groups", cacheStore.getRepositoryConfig().getConfig().repos.get(RepoConfig.ScmProvider.GITHUB).size());
        cacheStats.put("repositories", CacheHelper.cacheSize(cacheStore.getRepositories()));
        cacheStats.put("maven-projects", CacheHelper.cacheSize(cacheStore.getMavenProjects()));
        cacheStats.put("contents", CacheHelper.cacheSize(cacheStore.getReadmeContents()));
        cacheStats.put("commits", CacheHelper.cacheSize(cacheStore.getCommits()));
        cacheStats.put("releases", CacheHelper.cacheSize(cacheStore.getReleases()));
        cacheStats.put("confluence-pages", CacheHelper.cacheSize(cacheStore.getCantaraWiki()));
        cacheStats.put("jenkins-build-status", CacheHelper.cacheSize(cacheStore.getJenkinsBuildStatus()));
        cacheStats.put("snyk-test-status", CacheHelper.cacheSize(cacheStore.getSnykTestStatus()));
        cacheStats.put("shields-issues-status", CacheHelper.cacheSize(cacheStore.getSheildIssuesStatus()));
        cacheStats.put("shields-commits-status", CacheHelper.cacheSize(cacheStore.getSheildCommitsStatus()));
        cacheStats.put("shields-releases-status", CacheHelper.cacheSize(cacheStore.getShieldReleasesStatus()));

        response.put("cache-provider", cacheStore.getCacheManager().getCachingProvider().getDefaultURI().toString());
        response.put("cache", cacheStats);

        return ResponseEntity.ok(response);
    }

    /**
     * GitHub API rate limit status
     * <p>
     * GET /health/github
     * <p>
     * Returns GitHub API rate limit information:
     * - status, version, now, since (same as /health)
     * - service-status (same as /health)
     * - thread-pool (same as /health)
     * - scheduled-thread-pool (same as /health)
     * - cache (same as /health)
     * - github-rate-limit: Detailed rate limit from GitHub API
     *
     * @return Health status with GitHub rate limit details
     */
    @GetMapping("/github")
    public ResponseEntity<Map<String, Object>> githubHealth() {
        // Get base health response
        Map<String, Object> response = health().getBody();

        // Add GitHub rate limit
        try {
            Future<HttpResponse<String>> futureGitHubRateLimit = getGitHubRateLimit();
            GitHubRateLimit rateLimit = getRateLimitJson(futureGitHubRateLimit);

            if (rateLimit != null) {
                response.put("github-rate-limit", JsonbFactory.asJsonObject(rateLimit.toString()));
            }
        } catch (Exception e) {
            LOG.error("Error fetching GitHub rate limit", e);
            response.put("github-rate-limit-error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Thread pool status with detailed thread information
     * <p>
     * GET /health/threads
     * <p>
     * Returns all health information plus detailed thread dumps:
     * - threads: Detailed thread information (stack traces, etc.)
     *
     * @return Health status with detailed thread information
     */
    @GetMapping("/threads")
    public ResponseEntity<Map<String, Object>> threadsHealth() {
        // Get base health response
        Map<String, Object> response = health().getBody();

        // Add detailed thread information
        response.put("threads", HealthResource.instance().getThreadInfo());

        return ResponseEntity.ok(response);
    }

    // Helper methods

    private Future<HttpResponse<String>> getGitHubRateLimit() {
        GetGitHubCommand<String> cmd = new GetGitHubCommand<>(
                "gitHubRateLimit",
                configuration,
                Optional.empty(),
                "https://api.github.com/rate_limit",
                HttpResponse.BodyHandlers.ofString()
        );
        return cmd.queue();
    }

    private GitHubRateLimit getRateLimitJson(Future<HttpResponse<String>> futureGitHubRateLimit)
            throws InterruptedException, ExecutionException {
        HttpResponse<String> response = futureGitHubRateLimit.get();
        if (response.statusCode() == HTTP_OK) {
            return JsonbFactory.instance().fromJson(response.body(), GitHubRateLimit.class);
        }
        return null;
    }
}
