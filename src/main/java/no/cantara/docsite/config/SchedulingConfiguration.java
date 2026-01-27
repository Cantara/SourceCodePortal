package no.cantara.docsite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Spring Scheduling Configuration
 *
 * Configures Spring's @Scheduled support for periodic task execution.
 * Replaces the custom ScheduledExecutorService with Spring's ThreadPoolTaskScheduler.
 *
 * Migration from Custom ScheduledExecutorService:
 * Before (Custom):
 * <pre>
 * ScheduledWorker worker = new ScheduledWorker("jenkins", 0, 5, TimeUnit.MINUTES);
 * worker.queue(new QueueJenkinsStatusTask(...));
 * scheduledExecutorService.queue(worker);
 * scheduledExecutorService.start();
 * </pre>
 *
 * After (Spring @Scheduled):
 * <pre>
 * @Service
 * public class JenkinsStatusService {
 *     @Scheduled(fixedRateString = "${scp.scheduled.jenkins.interval-minutes}", timeUnit = TimeUnit.MINUTES)
 *     public void updateJenkinsStatus() {
 *         // Fetch and cache Jenkins build status
 *     }
 * }
 * </pre>
 *
 * Benefits:
 * - Declarative scheduling with @Scheduled annotation
 * - No manual thread pool management
 * - Expression-based configuration (SpEL)
 * - Better error handling and logging
 * - Integration with Actuator for monitoring
 * - Can disable/enable via configuration
 *
 * @Scheduled Options:
 * - fixedRate: Execute at fixed intervals (rate-based)
 * - fixedDelay: Execute with fixed delay after completion (delay-based)
 * - cron: Execute using cron expression
 * - initialDelay: Delay before first execution
 *
 * Configuration from application.yml:
 * - scp.scheduled.enabled: true/false (global enable/disable)
 * - scp.scheduled.jenkins.interval-minutes: 5
 * - scp.scheduled.snyk.interval-minutes: 15
 * - scp.scheduled.shields.interval-minutes: 15
 * - scp.scheduled.repository-refresh-minutes: 30
 * - scp.scheduled.commit-fetch-minutes: 15
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration - Task 6
 */
@Configuration
@EnableScheduling
@Profile("!test")
public class SchedulingConfiguration implements SchedulingConfigurer {

    private final ApplicationProperties properties;

    public SchedulingConfiguration(ApplicationProperties properties) {
        this.properties = properties;
    }

    /**
     * Configure the task scheduler
     *
     * Thread Pool Configuration:
     * - Pool size: 10 threads
     * - Thread name prefix: "scheduled-"
     * - Thread group: ScheduledThreadGroup
     *
     * Sizing:
     * - 10 threads handles all current scheduled tasks:
     *   1. Repository refresh (30min)
     *   2. Commit fetch (15min)
     *   3. Jenkins status (5min)
     *   4. Snyk status (15min)
     *   5. Shields status (15min)
     *   6. Cantara Wiki (60s)
     *
     * Tuning:
     * - Increase pool size if adding more scheduled tasks
     * - Monitor via Actuator: /actuator/metrics/executor.pool.size
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // Thread pool sizing
        scheduler.setPoolSize(10);

        // Thread naming for diagnostics
        scheduler.setThreadNamePrefix("scheduled-");

        // Thread group for monitoring
        scheduler.setThreadGroupName("ScheduledThreadGroup");

        // Graceful shutdown
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);

        // Error handler: Log exceptions but continue scheduling
        scheduler.setErrorHandler(throwable -> {
            org.slf4j.LoggerFactory.getLogger(SchedulingConfiguration.class).error(
                "Uncaught exception in scheduled task", throwable
            );
        });

        // Reject policy: Abort (throw exception if pool exhausted)
        scheduler.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.AbortPolicy());

        scheduler.initialize();
        return scheduler;
    }

    /**
     * Configure scheduling behavior
     *
     * This method is called by Spring to configure the scheduling infrastructure.
     * We provide the custom task scheduler bean created above.
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler());
    }

    /**
     * Example: Migration of ScheduledWorker to @Scheduled
     *
     * Before (Custom ScheduledExecutorService):
     * <pre>
     * // In ScheduledFetchData.java:
     * ScheduledWorker jenkinsScheduledWorker = new ScheduledWorker(
     *     "jenkins",
     *     0,  // initialDelay
     *     configuration.evaluateToInt("scheduled.check.jenkins.build.status.interval"),
     *     TimeUnit.MINUTES
     * );
     * jenkinsScheduledWorker.queue(new QueueJenkinsStatusTask(configuration, executorService, cacheStore));
     * scheduledExecutorService.queue(jenkinsScheduledWorker);
     * </pre>
     *
     * After (Spring @Scheduled):
     * <pre>
     * @Service
     * public class JenkinsStatusService {
     *     private final ApplicationProperties properties;
     *     private final CacheStore cacheStore;
     *
     *     @Scheduled(
     *         fixedRateString = "${scp.scheduled.jenkins.interval-minutes}",
     *         timeUnit = TimeUnit.MINUTES,
     *         initialDelayString = "${scp.scheduled.jenkins.initial-delay-minutes:0}"
     *     )
     *     public void updateJenkinsStatus() {
     *         log.info("Updating Jenkins build status");
     *         // Fetch Jenkins build status for all repositories
     *         // Cache results in cacheStore
     *     }
     * }
     * </pre>
     *
     * Benefits of @Scheduled:
     * - No ScheduledWorker boilerplate
     * - Configuration from application.yml via SpEL
     * - Can disable via @ConditionalOnProperty
     * - Automatic error handling
     * - Metrics via /actuator/scheduledtasks
     *
     * @Scheduled Patterns:
     *
     * 1. Fixed Rate (execute every X time):
     * @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
     * - Executes every 5 minutes regardless of previous execution time
     * - Use for independent tasks
     *
     * 2. Fixed Delay (wait X time after completion):
     * @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
     * - Waits 5 minutes after previous execution completes
     * - Use for dependent tasks
     *
     * 3. Cron Expression:
     * @Scheduled(cron = "0 0 * * * *")
     * - Executes at specific times (top of every hour)
     * - Use for time-specific tasks
     *
     * 4. Configuration-driven:
     * @Scheduled(fixedRateString = "${scp.scheduled.jenkins.interval-minutes}")
     * - Reads interval from application.yml
     * - Allows environment-specific configuration
     *
     * 5. Conditional Scheduling:
     * @Scheduled(...)
     * @ConditionalOnProperty(name = "scp.scheduled.enabled", havingValue = "true")
     * - Only schedules if property is true
     * - Allows disabling in tests or specific environments
     */
}
