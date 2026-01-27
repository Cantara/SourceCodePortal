package no.cantara.docsite.config;

import no.ssb.config.DynamicConfiguration;

import java.util.Map;

/**
 * Adapter to make ConfigurationBridge work with legacy code expecting DynamicConfiguration
 *
 * This adapter allows existing code that expects DynamicConfiguration to work
 * with Spring Boot's ApplicationProperties through the ConfigurationBridge.
 *
 * Purpose:
 * - Legacy code (RepositoryConfigLoader, PreFetchData, etc.) uses DynamicConfiguration
 * - Spring Boot uses ApplicationProperties (@ConfigurationProperties)
 * - ConfigurationBridge provides a bridge between the two
 * - This adapter wraps ConfigurationBridge to implement DynamicConfiguration interface
 *
 * Migration Path:
 * 1. Use this adapter to make legacy code work with Spring Boot
 * 2. Gradually migrate legacy code to use ApplicationProperties directly
 * 3. Remove this adapter once migration is complete
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration - Task 8
 */
public class DynamicConfigurationAdapter implements DynamicConfiguration {

    private final ConfigurationBridge bridge;

    public DynamicConfigurationAdapter(ConfigurationBridge bridge) {
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
    public Map<String, String> asMap() {
        return bridge.asMap();
    }
}
