package no.cantara.docsite.config;

import no.cantara.docsite.cache.CacheInitializer;
import no.cantara.docsite.cache.CacheStore;
import no.ssb.config.DynamicConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * CacheStore Bean Configuration
 *
 * Creates a Spring-managed CacheStore bean when running in Spring Boot mode.
 * This allows dependency injection of CacheStore into Spring components.
 *
 * Migration Notes:
 * - In Undertow mode: CacheStore is created manually in Application.java
 * - In Spring Boot mode: CacheStore is created as a Spring @Bean
 * - CacheStore still uses JSR-107 JCache internally (for now)
 * - Future: Migrate CacheStore to use Spring Cache abstraction
 *
 * Configuration Source:
 * - Uses ConfigurationBridge to get configuration values
 * - ConfigurationBridge delegates to ApplicationProperties
 * - This maintains compatibility with existing DynamicConfiguration API
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration - Task 4
 */
@Configuration
public class CacheStoreConfiguration {

    /**
     * Create CacheStore bean for Spring Boot mode
     *
     * Note: This uses the legacy CacheInitializer which still uses JSR-107 JCache.
     * The ConfigurationBridge provides a DynamicConfiguration-compatible API
     * backed by Spring Boot's ApplicationProperties.
     *
     * Future enhancement: Create a Spring Cache-based CacheStore implementation.
     */
    @Bean
    public CacheStore cacheStore(ConfigurationBridge configurationBridge) {
        // Use DynamicConfigurationAdapter to bridge ConfigurationBridge to DynamicConfiguration
        return CacheInitializer.initialize(new DynamicConfigurationAdapter(configurationBridge));
    }
}
