package no.cantara.docsite.config;

import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.ScheduledExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Executor Service Configuration
 *
 * Creates Spring-managed beans for ExecutorService and ScheduledExecutorService.
 * These are used for async task execution and scheduled background jobs.
 *
 * Migration Notes:
 * - In Undertow mode: Executors created manually in Application.java
 * - In Spring Boot mode: Executors created as Spring @Beans
 * - Same implementation, just different lifecycle management
 *
 * ExecutorService:
 * - Custom thread pool for async operations
 * - Used for GitHub API calls, cache population, etc.
 * - Thread pool size and configuration from ExecutorService.create()
 *
 * ScheduledExecutorService:
 * - Custom scheduled executor for periodic tasks
 * - Used for repository refresh, commit fetch, badge updates, etc.
 * - Scheduling intervals from ApplicationProperties
 *
 * Future Enhancement:
 * Once Task 6 is complete, these custom executors can be replaced with:
 * - Spring's @Async for async operations
 * - Spring's @Scheduled for scheduled tasks
 * - ThreadPoolTaskExecutor for thread pool management
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration - Task 5
 */
@Configuration
public class ExecutorConfiguration {

    /**
     * Create ExecutorService bean for async operations
     *
     * @return ExecutorService instance
     */
    @Bean
    public ExecutorService executorService() {
        ExecutorService executorService = ExecutorService.create();
        executorService.start();
        return executorService;
    }

    /**
     * Create ScheduledExecutorService bean for periodic tasks
     *
     * @param configurationBridge Configuration bridge for accessing properties
     * @param executorService ExecutorService for async operations
     * @param cacheStore CacheStore for cache operations
     * @return ScheduledExecutorService instance
     */
    @Bean
    public ScheduledExecutorService scheduledExecutorService(
            ConfigurationBridge configurationBridge,
            ExecutorService executorService,
            CacheStore cacheStore) {

        // Create adapter for DynamicConfiguration interface
        DynamicConfigurationAdapter adapter = new DynamicConfigurationAdapter(configurationBridge);

        ScheduledExecutorService scheduledExecutorService = ScheduledExecutorService.create(
            adapter,
            executorService,
            cacheStore
        );

        scheduledExecutorService.start();
        return scheduledExecutorService;
    }

    /**
     * Adapter to make ConfigurationBridge compatible with DynamicConfiguration interface
     */
    private static class DynamicConfigurationAdapter implements no.ssb.config.DynamicConfiguration {
        private final ConfigurationBridge bridge;

        DynamicConfigurationAdapter(ConfigurationBridge bridge) {
            this.bridge = bridge;
        }

        @Override
        public String evaluateToString(String key) {
            return bridge.evaluateToString(key);
        }

        @Override
        public int evaluateToInt(String key) {
            return bridge.evaluateToInt(key);
        }

        @Override
        public boolean evaluateToBoolean(String key) {
            return bridge.evaluateToBoolean(key);
        }

        @Override
        public java.util.Map<String, String> asMap() {
            return bridge.asMap();
        }
    }

    /**
     * TODO (Task 6): Replace custom executors with Spring's built-in solutions
     *
     * Example future implementation:
     *
     * @Bean
     * public ThreadPoolTaskExecutor asyncExecutor() {
     *     ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
     *     executor.setCorePoolSize(10);
     *     executor.setMaxPoolSize(50);
     *     executor.setQueueCapacity(500);
     *     executor.setThreadNamePrefix("async-");
     *     executor.initialize();
     *     return executor;
     * }
     *
     * @Bean
     * public ThreadPoolTaskScheduler taskScheduler() {
     *     ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
     *     scheduler.setPoolSize(10);
     *     scheduler.setThreadNamePrefix("scheduled-");
     *     scheduler.initialize();
     *     return scheduler;
     * }
     */
}
