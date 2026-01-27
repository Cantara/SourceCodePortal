package no.cantara.docsite.actuator;

import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.config.ApplicationProperties;
import no.cantara.docsite.health.GitHubRateLimit;
import no.cantara.docsite.health.HealthResource;
import no.cantara.docsite.json.JsonbFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * GitHub API Health Indicator
 *
 * Checks connectivity and rate limit status of GitHub API.
 * Exposed via Spring Boot Actuator at /actuator/health.
 *
 * Health Status:
 * - UP: GitHub API accessible, rate limit > 10% remaining
 * - DOWN: GitHub API unreachable or authentication failed
 * - DEGRADED: Rate limit < 10% remaining (may fail soon)
 *
 * Health Details:
 * - status: UP/DOWN/DEGRADED
 * - rateLimit: Current rate limit info
 *   - limit: Maximum requests per hour
 *   - remaining: Remaining requests
 *   - reset: Timestamp when limit resets
 * - lastSeen: Last successful GitHub API call
 * - organization: Configured GitHub organization
 *
 * Configuration:
 * - management.endpoint.health.show-details=always (show details)
 * - scp.github.organization (GitHub org)
 * - scp.github.access-token (authentication)
 *
 * Example Response:
 * <pre>
 * {
 *   "status": "UP",
 *   "components": {
 *     "github": {
 *       "status": "UP",
 *       "details": {
 *         "rateLimit": {
 *           "limit": 5000,
 *           "remaining": 4850,
 *           "reset": "2026-01-27T19:00:00Z"
 *         },
 *         "lastSeen": "2026-01-27T18:15:32Z",
 *         "organization": "Cantara"
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration - Task 7
 */
@Component("github")
@Profile("!test")
public class GitHubHealthIndicator implements HealthIndicator {

    private static final Logger LOG = LoggerFactory.getLogger(GitHubHealthIndicator.class);
    private static final int RATE_LIMIT_WARNING_THRESHOLD = 10; // Warn if < 10% remaining

    private final ApplicationProperties properties;

    public GitHubHealthIndicator(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Override
    public Health health() {
        try {
            // Fetch GitHub rate limit
            GitHubRateLimit rateLimit = fetchGitHubRateLimit();

            if (rateLimit == null) {
                return Health.down()
                    .withDetail("error", "Failed to fetch GitHub rate limit")
                    .withDetail("organization", properties.getGithub().getOrganization())
                    .build();
            }

            // Check rate limit status
            int limit = rateLimit.rate.limit;
            int remaining = rateLimit.rate.remaining;
            int percentRemaining = (remaining * 100) / limit;

            // Add details
            Health.Builder builder = Health.up();
            builder.withDetail("rateLimit", rateLimit.rate);
            builder.withDetail("lastSeen", Instant.ofEpochMilli(HealthResource.instance().getGitHubLastSeen()));
            builder.withDetail("organization", properties.getGithub().getOrganization());
            builder.withDetail("percentRemaining", percentRemaining + "%");

            // Warn if rate limit is low
            if (percentRemaining < RATE_LIMIT_WARNING_THRESHOLD) {
                builder.status("DEGRADED");
                builder.withDetail("warning", "Rate limit low: " + remaining + " of " + limit + " remaining");
            }

            return builder.build();

        } catch (Exception e) {
            LOG.error("GitHub health check failed", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("organization", properties.getGithub().getOrganization())
                .build();
        }
    }

    /**
     * Fetch GitHub rate limit from API
     */
    private GitHubRateLimit fetchGitHubRateLimit() {
        try {
            GetGitHubCommand<String> cmd = new GetGitHubCommand<>(
                "gitHubRateLimit",
                null, // Configuration not needed for this call
                Optional.empty(),
                "https://api.github.com/rate_limit",
                HttpResponse.BodyHandlers.ofString()
            );

            Future<HttpResponse<String>> future = cmd.queue();
            HttpResponse<String> response = future.get(5, TimeUnit.SECONDS);

            if (response.statusCode() == 200) {
                return JsonbFactory.instance().fromJson(response.body(), GitHubRateLimit.class);
            }

            LOG.warn("GitHub rate limit check returned status: {}", response.statusCode());
            return null;

        } catch (Exception e) {
            LOG.error("Error fetching GitHub rate limit", e);
            return null;
        }
    }
}
