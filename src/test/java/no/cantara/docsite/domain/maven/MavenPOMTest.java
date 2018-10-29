package no.cantara.docsite.domain.maven;

import no.cantara.docsite.client.HttpRequests;
import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.domain.github.contents.GitHubRepositoryContents;
import no.cantara.docsite.util.JsonbFactory;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.net.http.HttpResponse;
import java.util.Optional;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.testng.Assert.assertNotNull;

public class MavenPOMTest {

    private static final Logger LOG = LoggerFactory.getLogger(MavenPOMTest.class);

    static DynamicConfiguration configuration() {
        DynamicConfiguration configuration = new StoreBasedDynamicConfiguration.Builder()
                .propertiesResource("application-defaults.properties")
                .propertiesResource("security.properties")
                .propertiesResource("application.properties")
                .build();
        return configuration;
    }

    @Test
    public void testMavenPOM() {
        HttpResponse<String> response = HttpRequests.get("https://raw.githubusercontent.com/statisticsnorway/distributed-saga/master/pom.xml");

        MavenPOM mavenPom = FetchMavenPOMTask.parse(response.body());
        assertNotNull(mavenPom);
    }

    @Test(enabled = false)
    public void getMavenPOM() {
        GetGitHubCommand<String> command = new GetGitHubCommand<>("githubRepos", configuration(), Optional.empty(),
                String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", "Cantara", "SourceCodePortal", "pom.xml", "master"),
                HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = command.execute();
        if (response.statusCode() == HTTP_OK) {
            GitHubRepositoryContents repositoryContents = JsonbFactory.instance().fromJson(response.body(), GitHubRepositoryContents.class);
            MavenPOM mavenPom = FetchMavenPOMTask.parse(repositoryContents.content);
            LOG.trace("pom: {}\n{}", repositoryContents, mavenPom);
        }
    }

}
