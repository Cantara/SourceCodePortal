package no.cantara.docsite.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Bridge for Migration Period
 *
 * This class provides a bridge between the legacy DynamicConfiguration
 * (from no.ssb.config) and the new Spring Boot ApplicationProperties
 * during the migration.
 *
 * It provides the same API as DynamicConfiguration but delegates to
 * Spring Boot's type-safe @ConfigurationProperties.
 *
 * Usage during migration:
 * 1. Old code continues using DynamicConfiguration methods
 * 2. New code can inject ConfigurationBridge or ApplicationProperties directly
 * 3. Once all code is migrated, remove this bridge
 *
 * Example:
 * <pre>
 * // Old code pattern:
 * String org = dynamicConfiguration.evaluateToString("github.organization");
 *
 * // Bridge provides same API:
 * String org = configurationBridge.evaluateToString("github.organization");
 *
 * // Internally delegates to:
 * String org = applicationProperties.getGithub().getOrganization();
 * </pre>
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration - Task 3
 */
@Component
public class ConfigurationBridge {

    private final ApplicationProperties properties;

    @Autowired
    public ConfigurationBridge(ApplicationProperties properties) {
        this.properties = properties;
    }

    /**
     * Evaluate configuration key to String value
     * Mimics DynamicConfiguration.evaluateToString()
     */
    public String evaluateToString(String key) {
        return evaluateToString(key, null);
    }

    /**
     * Evaluate configuration key to String value with default
     * Mimics DynamicConfiguration.evaluateToString()
     */
    public String evaluateToString(String key, String defaultValue) {
        // Map old configuration keys to new ApplicationProperties structure
        return switch (key) {
            // Server configuration
            case "server.mode" -> properties.getServer().getMode();

            // HTTP configuration
            case "http.host", "server.host" -> properties.getHttp().getHost();
            case "http.port", "server.port" -> String.valueOf(properties.getHttp().getPort());
            case "cors.allow.origin" -> properties.getHttp().getCors().getAllowOrigin();
            case "cors.allow.header" -> properties.getHttp().getCors().getAllowHeader();
            case "cors.allow.origin.test" -> String.valueOf(properties.getHttp().getCors().isAllowOriginTest());

            // Cache configuration
            case "cache.config" -> properties.getCache().getConfig();
            case "cache.prefetch" -> String.valueOf(properties.getCache().isPrefetch());
            case "cache.management" -> String.valueOf(properties.getCache().isManagement());
            case "cache.statistics" -> String.valueOf(properties.getCache().isStatistics());
            case "cache.enabled" -> String.valueOf(properties.getCache().isEnabled());
            case "cache.ttl.minutes", "cache.ttl-minutes" -> String.valueOf(properties.getCache().getTtlMinutes());

            // GitHub configuration
            case "github.organization" -> properties.getGithub().getOrganization();
            case "github.oauth2.client.clientId", "github.client.id" -> properties.getGithub().getClientId();
            case "github.oauth2.client.clientSecret", "github.client.secret" -> properties.getGithub().getClientSecret();
            case "github.client.accessToken", "github.access.token" -> properties.getGithub().getAccessToken();
            case "github.repository.visibility" -> properties.getGithub().getRepository().getVisibility();

            // Render configuration
            case "render.max.group.commits", "max.group.commits" -> String.valueOf(properties.getRender().getMaxGroupCommits());

            // Scheduled tasks configuration
            case "scheduled.enabled" -> String.valueOf(properties.getScheduled().isEnabled());
            case "scheduled.tasks.interval" -> properties.getScheduled().getTasksInterval().toString();
            case "scheduled.repository.refresh.minutes" -> String.valueOf(properties.getScheduled().getRepositoryRefreshMinutes());
            case "scheduled.commit.fetch.minutes" -> String.valueOf(properties.getScheduled().getCommitFetchMinutes());
            case "scheduled.jenkins.interval.minutes" -> String.valueOf(properties.getScheduled().getJenkins().getIntervalMinutes());
            case "scheduled.snyk.interval.minutes" -> String.valueOf(properties.getScheduled().getSnyk().getIntervalMinutes());
            case "scheduled.shields.interval.minutes" -> String.valueOf(properties.getScheduled().getShields().getIntervalMinutes());

            // Jenkins configuration
            case "jenkins.baseUrl", "jenkins.base.url" -> properties.getJenkins().getBaseUrl();

            // Snyk configuration
            case "snyk.apiToken", "snyk.api.token" -> properties.getSnyk().getApiToken();
            case "snyk.organizationId", "snyk.organization.id" -> properties.getSnyk().getOrganizationId();

            // Shields configuration
            case "shields.baseUrl", "shields.base.url" -> properties.getShields().getBaseUrl();

            default -> defaultValue;
        };
    }

    /**
     * Evaluate configuration key to int value
     * Mimics DynamicConfiguration.evaluateToInt()
     */
    public int evaluateToInt(String key) {
        String value = evaluateToString(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    /**
     * Evaluate configuration key to boolean value
     * Mimics DynamicConfiguration.evaluateToBoolean()
     */
    public boolean evaluateToBoolean(String key) {
        String value = evaluateToString(key);
        return value != null && Boolean.parseBoolean(value);
    }

    /**
     * Check if configuration contains key
     */
    public boolean containsKey(String key) {
        return evaluateToString(key) != null;
    }

    /**
     * Get all configuration as a Map
     * Mimics DynamicConfiguration.asMap()
     */
    public Map<String, String> asMap() {
        Map<String, String> map = new HashMap<>();

        // Server
        map.put("server.mode", properties.getServer().getMode());

        // HTTP
        map.put("http.host", properties.getHttp().getHost());
        map.put("http.port", String.valueOf(properties.getHttp().getPort()));
        map.put("http.cors.allow.origin", properties.getHttp().getCors().getAllowOrigin());
        map.put("http.cors.allow.header", properties.getHttp().getCors().getAllowHeader());
        map.put("http.cors.allow.origin.test", String.valueOf(properties.getHttp().getCors().isAllowOriginTest()));

        // Cache
        map.put("cache.config", properties.getCache().getConfig());
        map.put("cache.prefetch", String.valueOf(properties.getCache().isPrefetch()));
        map.put("cache.management", String.valueOf(properties.getCache().isManagement()));
        map.put("cache.statistics", String.valueOf(properties.getCache().isStatistics()));
        map.put("cache.enabled", String.valueOf(properties.getCache().isEnabled()));
        map.put("cache.ttl.minutes", String.valueOf(properties.getCache().getTtlMinutes()));

        // GitHub
        map.put("github.organization", properties.getGithub().getOrganization());
        if (properties.getGithub().getClientId() != null) {
            map.put("github.oauth2.client.clientId", properties.getGithub().getClientId());
        }
        if (properties.getGithub().getClientSecret() != null) {
            map.put("github.oauth2.client.clientSecret", properties.getGithub().getClientSecret());
        }
        if (properties.getGithub().getAccessToken() != null) {
            map.put("github.client.accessToken", properties.getGithub().getAccessToken());
        }
        map.put("github.repository.visibility", properties.getGithub().getRepository().getVisibility());

        // Render
        map.put("render.max.group.commits", String.valueOf(properties.getRender().getMaxGroupCommits()));

        // Scheduled
        map.put("scheduled.enabled", String.valueOf(properties.getScheduled().isEnabled()));
        map.put("scheduled.tasks.interval", properties.getScheduled().getTasksInterval().toString());
        map.put("scheduled.repository.refresh.minutes", String.valueOf(properties.getScheduled().getRepositoryRefreshMinutes()));
        map.put("scheduled.commit.fetch.minutes", String.valueOf(properties.getScheduled().getCommitFetchMinutes()));
        map.put("scheduled.jenkins.interval.minutes", String.valueOf(properties.getScheduled().getJenkins().getIntervalMinutes()));
        map.put("scheduled.snyk.interval.minutes", String.valueOf(properties.getScheduled().getSnyk().getIntervalMinutes()));
        map.put("scheduled.shields.interval.minutes", String.valueOf(properties.getScheduled().getShields().getIntervalMinutes()));

        // Jenkins
        map.put("jenkins.baseUrl", properties.getJenkins().getBaseUrl());

        // Snyk
        if (properties.getSnyk().getApiToken() != null) {
            map.put("snyk.apiToken", properties.getSnyk().getApiToken());
        }
        if (properties.getSnyk().getOrganizationId() != null) {
            map.put("snyk.organizationId", properties.getSnyk().getOrganizationId());
        }

        // Shields
        map.put("shields.baseUrl", properties.getShields().getBaseUrl());

        return map;
    }

    /**
     * Helper method to get ApplicationProperties directly
     * For code that needs to migrate to using ApplicationProperties directly
     */
    public ApplicationProperties getApplicationProperties() {
        return properties;
    }
}
