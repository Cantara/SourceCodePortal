package no.cantara.docsite.actuator;

import no.cantara.docsite.config.ApplicationProperties;
import no.cantara.docsite.health.HealthResource;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Application Info Contributor
 *
 * Adds custom application information to the /actuator/info endpoint.
 * Complements the default info from build-info.properties and git.properties.
 *
 * Information Provided:
 * - application: Name, description, version
 * - runtime: Uptime, start time, Java version
 * - configuration: GitHub org, scheduling enabled, cache TTL
 * - integration: Jenkins URL, Snyk configured
 *
 * Configuration:
 * - management.info.env.enabled=true (enable environment info)
 * - management.info.build.enabled=true (enable build info)
 * - management.info.git.enabled=true (enable git info)
 *
 * Example Response:
 * <pre>
 * {
 *   "application": {
 *     "name": "Source Code Portal",
 *     "description": "GitHub repository dashboard and documentation portal",
 *     "version": "0.10.17-SNAPSHOT"
 *   },
 *   "runtime": {
 *     "uptime": "PT2H15M32S",
 *     "startTime": "2026-01-27T16:00:00Z",
 *     "javaVersion": "21.0.1"
 *   },
 *   "configuration": {
 *     "githubOrganization": "Cantara",
 *     "schedulingEnabled": true,
 *     "cacheTtlMinutes": 30
 *   },
 *   "integration": {
 *     "jenkinsUrl": "https://jenkins.quadim.ai",
 *     "snykConfigured": true
 *   },
 *   "build": {
 *     "version": "0.10.17-SNAPSHOT",
 *     "time": "2026-01-27T15:00:00Z"
 *   },
 *   "git": {
 *     "branch": "master",
 *     "commit": {
 *       "id": "4f47af7",
 *       "time": "2026-01-27T14:00:00Z"
 *     }
 *   }
 * }
 * </pre>
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration - Task 7
 */
@Component
@Profile("!test")
public class ApplicationInfoContributor implements InfoContributor {

    private final ApplicationProperties properties;

    public ApplicationInfoContributor(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void contribute(Info.Builder builder) {
        // Application info
        Map<String, Object> application = new HashMap<>();
        application.put("name", "Source Code Portal");
        application.put("description", "GitHub repository dashboard and documentation portal");
        application.put("version", HealthResource.instance().getVersion());
        builder.withDetail("application", application);

        // Runtime info
        Map<String, Object> runtime = new HashMap<>();
        Instant startTime = Instant.parse(HealthResource.instance().getRunningSince());
        Duration uptime = Duration.between(startTime, Instant.now());
        runtime.put("uptime", uptime.toString());
        runtime.put("startTime", startTime.toString());
        runtime.put("javaVersion", System.getProperty("java.version"));
        runtime.put("javaVendor", System.getProperty("java.vendor"));
        builder.withDetail("runtime", runtime);

        // Configuration info
        Map<String, Object> configuration = new HashMap<>();
        configuration.put("githubOrganization", properties.getGithub().getOrganization());
        configuration.put("schedulingEnabled", properties.getScheduled().isEnabled());
        configuration.put("cacheTtlMinutes", properties.getCache().getTtlMinutes());
        configuration.put("cacheEnabled", properties.getCache().isEnabled());
        configuration.put("repositoryRefreshMinutes", properties.getScheduled().getRepositoryRefreshMinutes());
        configuration.put("commitFetchMinutes", properties.getScheduled().getCommitFetchMinutes());
        builder.withDetail("configuration", configuration);

        // Integration info
        Map<String, Object> integration = new HashMap<>();
        integration.put("jenkinsUrl", properties.getJenkins().getBaseUrl());
        integration.put("snykConfigured", properties.getSnyk().getApiToken() != null && !properties.getSnyk().getApiToken().isEmpty());
        integration.put("shieldsUrl", properties.getShields().getBaseUrl());
        builder.withDetail("integration", integration);

        // Server mode
        Map<String, Object> server = new HashMap<>();
        server.put("mode", properties.getServer().getMode());
        server.put("port", properties.getHttp().getPort());
        builder.withDetail("server", server);
    }
}
