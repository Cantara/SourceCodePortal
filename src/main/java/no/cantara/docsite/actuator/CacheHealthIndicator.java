package no.cantara.docsite.actuator;

import no.cantara.docsite.cache.CacheHelper;
import no.cantara.docsite.cache.CacheStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache Health Indicator
 *
 * Monitors cache manager and individual cache health.
 * Exposed via Spring Boot Actuator at /actuator/health.
 *
 * Health Status:
 * - UP: Cache manager open, all caches accessible
 * - DOWN: Cache manager closed or inaccessible
 * - DEGRADED: Cache manager open but some caches empty
 *
 * Health Details:
 * - status: UP/DOWN/DEGRADED
 * - cacheManager: open/closed
 * - caches: Map of cache names to sizes
 *   - repositories: Count of cached repositories
 *   - commits: Count of cached commits
 *   - contents: Count of cached contents
 *   - buildStatus: Count of Jenkins build statuses
 *   - snykStatus: Count of Snyk test statuses
 * - totalEntries: Sum of all cache entries
 * - emptyCount: Number of empty caches
 *
 * Configuration:
 * - management.endpoint.health.show-details=always
 *
 * Example Response:
 * <pre>
 * {
 *   "status": "UP",
 *   "components": {
 *     "cache": {
 *       "status": "UP",
 *       "details": {
 *         "cacheManager": "open",
 *         "caches": {
 *           "repositories": 42,
 *           "commits": 1523,
 *           "contents": 38,
 *           "buildStatus": 35,
 *           "snykStatus": 28
 *         },
 *         "totalEntries": 1666,
 *         "emptyCount": 0
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration - Task 7
 */
@Component("cache")
@Profile("!test")
public class CacheHealthIndicator implements HealthIndicator {

    private static final Logger LOG = LoggerFactory.getLogger(CacheHealthIndicator.class);

    private final CacheStore cacheStore;

    public CacheHealthIndicator(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Override
    public Health health() {
        try {
            // Check if cache manager is open
            boolean cacheManagerOpen = !cacheStore.getCacheManager().isClosed();

            if (!cacheManagerOpen) {
                return Health.down()
                    .withDetail("cacheManager", "closed")
                    .withDetail("error", "Cache manager is closed")
                    .build();
            }

            // Get cache sizes
            Map<String, Long> cacheSizes = new HashMap<>();
            cacheSizes.put("repositories", getCacheSize(CacheStore.REPOSITORIES));
            cacheSizes.put("commits", getCacheSize(CacheStore.COMMITS));
            cacheSizes.put("contents", getCacheSize(CacheStore.CONTENTS));
            cacheSizes.put("releases", getCacheSize(CacheStore.RELEASES));
            cacheSizes.put("mavenProjects", getCacheSize(CacheStore.MAVEN_PROJECTS));
            cacheSizes.put("jenkinsBuildStatus", getCacheSize(CacheStore.JENKINS_BUILD_STATUS));
            cacheSizes.put("snykTestStatus", getCacheSize(CacheStore.SNYK_TEST_STATUS));
            cacheSizes.put("cantaraWiki", getCacheSize(CacheStore.CANTARA_WIKI));

            // Calculate statistics
            long totalEntries = cacheSizes.values().stream().mapToLong(Long::longValue).sum();
            long emptyCount = cacheSizes.values().stream().filter(size -> size == 0).count();

            // Build health status
            Health.Builder builder = Health.up();
            builder.withDetail("cacheManager", "open");
            builder.withDetail("caches", cacheSizes);
            builder.withDetail("totalEntries", totalEntries);
            builder.withDetail("emptyCount", emptyCount);

            // Warn if many caches are empty
            if (emptyCount > 4) {
                builder.status("DEGRADED");
                builder.withDetail("warning", emptyCount + " of " + cacheSizes.size() + " caches are empty");
            }

            // Warn if no data at all
            if (totalEntries == 0) {
                builder.status("DEGRADED");
                builder.withDetail("warning", "All caches are empty - data may not be loaded yet");
            }

            return builder.build();

        } catch (Exception e) {
            LOG.error("Cache health check failed", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }

    /**
     * Get size of a specific cache
     */
    private long getCacheSize(String cacheName) {
        try {
            return CacheHelper.cacheSize(cacheStore.getCacheManager().getCache(cacheName));
        } catch (Exception e) {
            LOG.warn("Failed to get size of cache {}: {}", cacheName, e.getMessage());
            return 0;
        }
    }
}
