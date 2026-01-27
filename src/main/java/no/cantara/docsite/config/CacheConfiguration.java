package no.cantara.docsite.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import no.cantara.docsite.cache.CacheStore;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

/**
 * Spring Cache Configuration
 *
 * Configures Spring Boot's cache abstraction with Caffeine as the provider.
 * This configuration coexists with the legacy JSR-107 JCache setup during migration.
 *
 * Migration Strategy:
 * 1. Phase 4 (Current): Configure Spring Cache infrastructure
 * 2. Phase 4b: Create Spring Cache-based services alongside existing ones
 * 3. Phase 4c: Migrate controllers to use Spring Cache services
 * 4. Phase 4d: Remove JSR-107 JCache dependencies once fully migrated
 *
 * Cache Names:
 * - repositories: GitHub repository metadata
 * - commits: Git commit history
 * - contents: Repository file contents (README, etc.)
 * - buildStatus: Jenkins build status
 * - snykStatus: Snyk security scan results
 * - shields: Shields.io badge status
 * - releases: GitHub release/tag information
 * - mavenProjects: Maven POM metadata
 * - cantaraWiki: Confluence wiki content
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration - Task 4
 */
@Configuration
@EnableCaching
@Profile("!test") // Don't enable in test profile
public class CacheConfiguration {

    private final ApplicationProperties properties;

    public CacheConfiguration(ApplicationProperties properties) {
        this.properties = properties;
    }

    /**
     * Configure Caffeine cache manager with TTL from application properties
     */
    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Configure Caffeine cache builder
        cacheManager.setCaffeine(caffeineCacheBuilder());

        // Explicitly define cache names (matches application.yml)
        cacheManager.setCacheNames(java.util.Arrays.asList(
            "repositories",
            "commits",
            "contents",
            "buildStatus",
            "snykStatus",
            "shields",
            "releases",
            "mavenProjects",
            "cantaraWiki"
        ));

        return cacheManager;
    }

    /**
     * Caffeine cache builder with configuration from application properties
     */
    @Bean
    public Caffeine<Object, Object> caffeineCacheBuilder() {
        int ttlMinutes = properties.getCache().getTtlMinutes();

        return Caffeine.newBuilder()
            .maximumSize(10000) // Matches application.yml
            .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
            .recordStats(); // Enable statistics for metrics
    }

    /**
     * Customize cache manager (optional)
     * Can be used to set per-cache configurations if needed
     */
    @Bean
    public CacheManagerCustomizer<CaffeineCacheManager> cacheManagerCustomizer() {
        return cacheManager -> {
            // Future: Add per-cache customization if needed
            // Example:
            // cacheManager.registerCustomCache("commits",
            //     Caffeine.newBuilder()
            //         .maximumSize(50000)
            //         .expireAfterWrite(60, TimeUnit.MINUTES)
            //         .build());
        };
    }

    /**
     * Note: We're NOT creating a CacheStore bean here yet.
     * CacheStore still uses JSR-107 JCache during the migration.
     *
     * Once controllers are migrated to Spring MVC (Task 5),
     * we can create a Spring-managed CacheStore bean that can
     * optionally use Spring Cache abstraction.
     *
     * For now, CacheStore is created manually in Application.java (Undertow mode)
     * or can be created as a @Bean once controllers are migrated (Spring Boot mode).
     */
}
