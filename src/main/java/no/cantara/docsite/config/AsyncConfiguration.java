package no.cantara.docsite.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Spring Async Configuration
 *
 * Configures Spring's @Async support for asynchronous method execution.
 * Replaces the custom ExecutorService with Spring's ThreadPoolTaskExecutor.
 *
 * Migration from Custom ExecutorService:
 * Before (Custom):
 * <pre>
 * ExecutorService executorService = ExecutorService.create();
 * executorService.queue(task);
 * Future<?> future = task.queue();
 * </pre>
 *
 * After (Spring @Async):
 * <pre>
 * @Async
 * public CompletableFuture<Result> asyncMethod() {
 *     Result result = doWork();
 *     return CompletableFuture.completedFuture(result);
 * }
 *
 * // Call from another bean
 * CompletableFuture<Result> future = service.asyncMethod();
 * </pre>
 *
 * Benefits:
 * - Declarative async with @Async annotation
 * - No manual thread pool management
 * - Better exception handling
 * - Integration with Spring's transaction management
 * - Metrics and monitoring via Actuator
 *
 * Configuration:
 * - Core pool size: 10 threads
 * - Max pool size: 50 threads
 * - Queue capacity: 500 tasks
 * - Thread name prefix: "async-"
 *
 * Usage Example:
 * <pre>
 * @Service
 * public class GitHubService {
 *     @Async
 *     public CompletableFuture<List<Repo>> fetchRepositories() {
 *         List<Repo> repos = githubApi.getRepos();
 *         return CompletableFuture.completedFuture(repos);
 *     }
 * }
 * </pre>
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration - Task 6
 */
@Configuration
@EnableAsync
@Profile("!test")
public class AsyncConfiguration implements AsyncConfigurer {

    /**
     * Configure the async executor
     *
     * Thread Pool Configuration:
     * - Core size: 10 - Always maintained threads
     * - Max size: 50 - Maximum threads under load
     * - Queue: 500 - Tasks queued before creating new threads
     * - Keep alive: 60s - Idle thread lifetime
     *
     * Sizing Guidelines:
     * - Core = 10: Good for moderate async load
     * - Max = 50: Handles bursts (GitHub API calls, etc.)
     * - Queue = 500: Buffer for temporary spikes
     *
     * Tuning:
     * - Increase core for sustained high load
     * - Increase max for burst capacity
     * - Monitor via Actuator: /actuator/metrics/executor.pool.size
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Thread pool sizing
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(500);

        // Thread naming for diagnostics
        executor.setThreadNamePrefix("async-");

        // Graceful shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        // Thread group for monitoring
        executor.setThreadGroupName("AsyncThreadGroup");

        // Reject policy: Caller runs (blocks caller if queue full)
        // Alternatives: AbortPolicy, DiscardPolicy, DiscardOldestPolicy
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }

    /**
     * Handle uncaught exceptions in async methods
     *
     * Called when an @Async method throws an exception that isn't
     * caught within the method itself.
     *
     * Logs the exception and method details for debugging.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            org.slf4j.LoggerFactory.getLogger(AsyncConfiguration.class).error(
                "Uncaught async exception in method '{}' with parameters {}",
                method.getName(), java.util.Arrays.toString(params), throwable
            );
        };
    }

    /**
     * Example: Migration of ExecutorService usage to @Async
     *
     * Before (Custom ExecutorService):
     * <pre>
     * public class FetchGitHubRepositories extends WorkerTask {
     *     @Override
     *     public void run() {
     *         GetGitHubCommand cmd = new GetGitHubCommand(...);
     *         Future<HttpResponse<String>> future = cmd.queue();
     *         HttpResponse<String> response = future.get();
     *         // Process response
     *     }
     * }
     *
     * // In controller/service:
     * executorService.queue(new FetchGitHubRepositories(...));
     * </pre>
     *
     * After (Spring @Async):
     * <pre>
     * @Service
     * public class GitHubService {
     *     @Async
     *     public CompletableFuture<List<Repository>> fetchRepositories() {
     *         // Blocking GitHub API call (runs on async thread pool)
     *         List<Repository> repos = githubApi.getRepositories();
     *         return CompletableFuture.completedFuture(repos);
     *     }
     * }
     *
     * // In controller:
     * @Autowired
     * private GitHubService gitHubService;
     *
     * public void updateRepositories() {
     *     CompletableFuture<List<Repository>> future = gitHubService.fetchRepositories();
     *     future.thenAccept(repos -> cacheStore.putAll(repos));
     * }
     * </pre>
     *
     * Benefits of @Async:
     * - No WorkerTask boilerplate
     * - Better composability with CompletableFuture
     * - Exception handling via AsyncUncaughtExceptionHandler
     * - Automatic thread pool management
     * - Metrics via Actuator
     */
}
