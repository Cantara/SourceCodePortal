package no.cantara.docsite.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;

/**
 * Cache Metrics Configuration
 *
 * Exposes Caffeine cache metrics to Micrometer/Prometheus for monitoring.
 *
 * Metrics exposed per cache:
 * - cache_size: Current number of entries
 * - cache_gets{result=hit}: Cache hit count
 * - cache_gets{result=miss}: Cache miss count
 * - cache_evictions: Number of evictions
 * - cache_puts: Number of puts
 *
 * Access metrics via:
 * - /actuator/metrics/cache.size?tag=cache:repositories
 * - /actuator/metrics/cache.gets?tag=cache:repositories
 * - /actuator/prometheus (for Prometheus scraping)
 *
 * Example Prometheus queries:
 * - cache_size{cache="repositories"}
 * - rate(cache_gets_total{result="hit"}[5m])
 * - cache_gets_total{result="miss"} / cache_gets_total
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration - Task 4
 */
@Configuration
@Profile("xxx-disabled")
public class CacheMetricsConfiguration {

    private final CacheManager cacheManager;
    private final MeterRegistry meterRegistry;

    public CacheMetricsConfiguration(CacheManager cacheManager, MeterRegistry meterRegistry) {
        this.cacheManager = cacheManager;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Bind Caffeine cache metrics to Micrometer after initialization
     */
    @PostConstruct
    public void bindCacheMetrics() {
        // Bind metrics for each cache
        cacheManager.getCacheNames().forEach(cacheName -> {
            CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(cacheName);
            if (caffeineCache != null) {
                CaffeineCacheMetrics.monitor(meterRegistry, caffeineCache.getNativeCache(), cacheName);
            }
        });
    }
}
