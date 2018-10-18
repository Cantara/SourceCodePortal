package no.cantara.docsite.domain.github.repos;

import no.cantara.docsite.commands.GetGitHubCommand;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.JsonbBuilder;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.net.HttpURLConnection.HTTP_OK;

public class GetGitHubRepositories {

    private static final Logger LOG = LoggerFactory.getLogger(GetGitHubRepositories.class);
    private final DynamicConfiguration configuration;
    private final String organization;

    public GetGitHubRepositories(DynamicConfiguration configuration, String organization) {
        this.configuration = configuration;
        this.organization = organization;
    }

    public List<GitHubRepository> getOrganizationRepos() {
        String repositoryVisibility = (configuration.evaluateToString("github.repository.visibility") == null ? "public" :
                configuration.evaluateToString("github.repository.visibility"));
        GetGitHubCommand<String> command = new GetGitHubCommand<>("githubRepos", configuration, Optional.empty(),
                String.format("https://api.github.com/orgs/%s/repos?type=%s&per_page=500",
                        organization,
                        RepositoryVisibility.valueOf(repositoryVisibility.toUpperCase())),
                HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = command.execute();
        if (response.statusCode() == HTTP_OK) {
            return Arrays.asList(JsonbBuilder.create().fromJson(response.body(), GitHubRepository[].class));
        }
        LOG.error("Error: HTTP-{} - {}", response.statusCode(), response.body());
        return Collections.emptyList();
    }

}
