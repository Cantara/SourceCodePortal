package no.cantara.docsite.domain.github.repos;

import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.domain.github.contents.RepositoryContents;
import no.cantara.docsite.domain.maven.FetchMavenPOMTask;
import no.cantara.docsite.domain.maven.MavenPOM;
import no.cantara.docsite.util.JsonbFactory;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.net.http.HttpResponse;
import java.util.Optional;

import static java.net.HttpURLConnection.HTTP_OK;

public class GitHubCommandsTest {

    private static final Logger LOG = LoggerFactory.getLogger(GitHubCommandsTest.class);

    static DynamicConfiguration configuration() {
        DynamicConfiguration configuration = new StoreBasedDynamicConfiguration.Builder()
                .propertiesResource("application-defaults.properties")
                .propertiesResource("security.properties")
                .propertiesResource("application.properties")
                .build();
        return configuration;
    }

    @Ignore
    @Test
    public void testGitHubApiLimit() {
        DynamicConfiguration configuration = configuration();
        HttpResponse<String> response = new GetGitHubCommand<>("githubRateLimit", configuration, Optional.empty(), "https://api.github.com/rate_limit", HttpResponse.BodyHandlers.ofString()).execute();
        LOG.trace("GitHub API Limit: {}", JsonbFactory.prettyPrint(response.body()));
    }

    @Deprecated
    @Test(enabled = false)
    public void getMavenPOM() {
        GetGitHubCommand<String> command = new GetGitHubCommand<>("githubRepos", configuration(), Optional.empty(),
                String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", "Cantara", "SourceCodePortal", "pom.xml", "master"),
                HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = command.execute();
        if (response.statusCode() == HTTP_OK) {
            RepositoryContents repositoryContents = JsonbFactory.instance().fromJson(response.body(), RepositoryContents.class);
            MavenPOM mavenPom = FetchMavenPOMTask.parse(repositoryContents.content);
            LOG.trace("pom: {}\n{}", repositoryContents, mavenPom);
        }
    }

}
