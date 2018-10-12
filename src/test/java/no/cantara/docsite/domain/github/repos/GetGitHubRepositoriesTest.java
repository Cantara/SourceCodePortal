package no.cantara.docsite.domain.github.repos;

import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.model.github.pull.GitHubRepository;
import no.cantara.docsite.util.JsonUtil;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

public class GetGitHubRepositoriesTest {

    private static final Logger LOG = LoggerFactory.getLogger(GetGitHubRepositoriesTest.class);

    static DynamicConfiguration configuration() {
        DynamicConfiguration configuration = new StoreBasedDynamicConfiguration.Builder()
                .propertiesResource("security.properties")
                .build();
        return configuration;
    }

    @Ignore
    @Test
    public void testGitHubApiLimit() {
        DynamicConfiguration configuration = configuration();
        HttpResponse<String> response = new GetGitHubCommand<>("githubRateLimit", configuration, Optional.empty(), "/rate_limit", HttpResponse.BodyHandlers.ofString()).execute();
        LOG.trace("GitHub API Limit: {}", JsonUtil.prettyPrint(JsonUtil.asJsonObject(response.body())));
    }

    @Test
    public void findOrgGitHubRepos() {
        DynamicConfiguration configuration = configuration();
        GetGitHubRepositories repos = new GetGitHubRepositories(configuration, "Cantara");
        List<GitHubRepository> result = repos.getOrganizationRepos();
        LOG.trace("repos: {}", result);
    }

}
