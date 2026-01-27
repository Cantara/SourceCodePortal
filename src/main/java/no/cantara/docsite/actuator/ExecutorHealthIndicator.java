package no.cantara.docsite.actuator;

import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Executor Service Health Indicator
 *
 * Monitors custom executor service and scheduled executor service health.
 * Exposed via Spring Boot Actuator at /actuator/health.
 *
 * Health Status:
 * - UP: Both executors running, active threads < pool size
 * - DOWN: Either executor terminated
 * - DEGRADED: Active threads >= pool size (saturated)
 *
 * Health Details:
 * - status: UP/DOWN/DEGRADED
 * - executorService:
 *   - status: running/terminated
 *   - activeThreads: Current active thread count
 *   - poolSize: Current pool size
 *   - queueSize: Number of queued tasks
 * - scheduledExecutorService:
 *   - status: running/terminated
 *   - activeThreads: Current active thread count
 *   - poolSize: Current pool size
 *   - queueSize: Number of queued tasks
 *   - scheduledTaskCount: Number of scheduled tasks
 *
 * Configuration:
 * - management.endpoint.health.show-details=always
 *
 * Example Response:
 * <pre>
 * {
 *   "status": "UP",
 *   "components": {
 *     "executor": {
 *       "status": "UP",
 *       "details": {
 *         "executorService": {
 *           "status": "running",
 *           "activeThreads": 3,
 *           "poolSize": 20,
 *           "queueSize": 5
 *         },
 *         "scheduledExecutorService": {
 *           "status": "running",
 *           "activeThreads": 1,
 *           "poolSize": 4,
 *           "queueSize": 0,
 *           "scheduledTaskCount": 6
 *         }
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * Note: This monitors the custom ExecutorService and ScheduledExecutorService
 * from the legacy code. Once migrated to Spring's @Async and @Scheduled,
 * Spring Boot's built-in executor metrics will be used instead.
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration - Task 7
 */
@Component("executor")
@Profile("!test")
public class ExecutorHealthIndicator implements HealthIndicator {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutorHealthIndicator.class);
    private static final double SATURATION_THRESHOLD = 0.9; // Warn if > 90% utilized

    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutorService;

    public ExecutorHealthIndicator(ExecutorService executorService, ScheduledExecutorService scheduledExecutorService) {
        this.executorService = executorService;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public Health health() {
        try {
            // Check executor service
            ThreadPoolExecutor executor = executorService.getThreadPool();
            boolean executorRunning = !executor.isTerminated();
            int executorActiveThreads = executor.getActiveCount();
            int executorPoolSize = executor.getPoolSize();
            int executorQueueSize = executor.getQueue().size();

            // Check scheduled executor service
            ScheduledThreadPoolExecutor scheduledExecutor =
                (ScheduledThreadPoolExecutor) scheduledExecutorService.getThreadPool();
            boolean scheduledExecutorRunning = !scheduledExecutor.isTerminated();
            int scheduledActiveThreads = scheduledExecutor.getActiveCount();
            int scheduledPoolSize = scheduledExecutor.getPoolSize();
            int scheduledQueueSize = scheduledExecutor.getQueue().size();
            int scheduledTaskCount = scheduledExecutorService.getScheduledWorkers().size();

            // Build details
            Map<String, Object> executorDetails = new HashMap<>();
            executorDetails.put("status", executorRunning ? "running" : "terminated");
            executorDetails.put("activeThreads", executorActiveThreads);
            executorDetails.put("poolSize", executorPoolSize);
            executorDetails.put("queueSize", executorQueueSize);

            Map<String, Object> scheduledExecutorDetails = new HashMap<>();
            scheduledExecutorDetails.put("status", scheduledExecutorRunning ? "running" : "terminated");
            scheduledExecutorDetails.put("activeThreads", scheduledActiveThreads);
            scheduledExecutorDetails.put("poolSize", scheduledPoolSize);
            scheduledExecutorDetails.put("queueSize", scheduledQueueSize);
            scheduledExecutorDetails.put("scheduledTaskCount", scheduledTaskCount);

            // Determine health status
            Health.Builder builder;

            if (!executorRunning || !scheduledExecutorRunning) {
                builder = Health.down();
                builder.withDetail("error", "One or more executors are terminated");
            } else {
                // Check for saturation
                double executorUtilization = executorPoolSize > 0 ? (double) executorActiveThreads / executorPoolSize : 0;
                double scheduledUtilization = scheduledPoolSize > 0 ? (double) scheduledActiveThreads / scheduledPoolSize : 0;

                if (executorUtilization >= SATURATION_THRESHOLD || scheduledUtilization >= SATURATION_THRESHOLD) {
                    builder = Health.status("DEGRADED");
                    builder.withDetail("warning", "Executor service is saturated");
                    builder.withDetail("executorUtilization", String.format("%.1f%%", executorUtilization * 100));
                    builder.withDetail("scheduledUtilization", String.format("%.1f%%", scheduledUtilization * 100));
                } else {
                    builder = Health.up();
                }
            }

            builder.withDetail("executorService", executorDetails);
            builder.withDetail("scheduledExecutorService", scheduledExecutorDetails);

            return builder.build();

        } catch (Exception e) {
            LOG.error("Executor health check failed", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
