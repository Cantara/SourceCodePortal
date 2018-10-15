package no.cantara.docsite.domain.github.repos;

import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.domain.maven.MavenPOMParser;
import no.cantara.docsite.model.github.pull.GitHubRepository;
import no.cantara.docsite.model.github.pull.RepositoryContents;
import no.cantara.docsite.model.maven.MavenPOM;
import no.cantara.docsite.util.JsonUtil;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import static java.net.HttpURLConnection.HTTP_OK;

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
        HttpResponse<String> response = new GetGitHubCommand<>("githubRateLimit", configuration, Optional.empty(), "https://api.github.com/rate_limit", HttpResponse.BodyHandlers.ofString()).execute();
        LOG.trace("GitHub API Limit: {}", JsonUtil.prettyPrint(response.body()));
    }

    @Test
    public void findOrgGitHubRepos() {
        DynamicConfiguration configuration = configuration();
        GetGitHubRepositories repos = new GetGitHubRepositories(configuration, "Cantara");
        List<GitHubRepository> result = repos.getOrganizationRepos();
        LOG.trace("repos: {}\nsize:{}", result, result.size());
    }

    RepositoryContents getMavenPOM(String repoName, String fileNamePath, String branch) {
        GetGitHubCommand<String> command = new GetGitHubCommand<>("githubRepos", configuration(), Optional.empty(),
                String.format("https://api.github.com/repos/%s/%s/contents/%s?client_id=%s&client_secret=%s&ref=%s",
                        "Cantara",
                        repoName,
                        fileNamePath,
                        configuration().evaluateToString("github.oauth2.client.clientId"),
                        configuration().evaluateToString("github.oauth2.client.clientSecret"),
                        branch),
                HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = command.execute();
        if (response.statusCode() == HTTP_OK) {
            JsonbConfig config = new JsonbConfig();
            return JsonbBuilder.create(config).fromJson(response.body(), RepositoryContents.class);
        }
        LOG.error("Error: {}", response.statusCode());
        return null;
    }

    @Test
    public void getMavenPOM() {
        DynamicConfiguration configuration = configuration();
        RepositoryContents repositoryContents = getMavenPOM("SourceCodePortal", "pom.xml", "master");
        MavenPOM mavenPom = new MavenPOMParser().parse(repositoryContents.getDecodedContent());
        LOG.trace("pom: {}\n{}", repositoryContents, mavenPom);
    }

}
