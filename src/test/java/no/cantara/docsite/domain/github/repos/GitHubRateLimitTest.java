package no.cantara.docsite.domain.github.repos;

import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.json.JsonbFactory;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.Optional;

public class GitHubRateLimitTest {

    private static final Logger LOG = LoggerFactory.getLogger(GitHubRateLimitTest.class);

    static DynamicConfiguration configuration() {
        DynamicConfiguration configuration = new StoreBasedDynamicConfiguration.Builder()
                .propertiesResource("application-defaults.properties")
                .propertiesResource("security.properties")
                .propertiesResource("application.properties")
                .build();
        return configuration;
    }

    @Disabled
    @Test
    public void testGitHubApiLimit() {
        DynamicConfiguration configuration = configuration();
        HttpResponse<String> response = new GetGitHubCommand<>("githubRateLimit", configuration, Optional.empty(), "https://api.github.com/rate_limit", HttpResponse.BodyHandlers.ofString()).execute();
        LOG.trace("GitHub API Limit: {}", JsonbFactory.prettyPrint(response.body()));
    }

}
