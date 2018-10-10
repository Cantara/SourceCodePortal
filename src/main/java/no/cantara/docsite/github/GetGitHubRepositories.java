package no.cantara.docsite.github;

import no.cantara.docsite.client.GitHubClient;
import no.ssb.config.DynamicConfiguration;

import javax.json.bind.JsonbBuilder;
import java.util.Arrays;
import java.util.List;

public class GetGitHubRepositories {

    private final DynamicConfiguration configuration;
    private final GitHubClient githubClient;

    public GetGitHubRepositories(DynamicConfiguration configuration) {
        this.configuration = configuration;
        githubClient = new GitHubClient(configuration);
    }

    public List<GitHubRepository> getOrganizationRepos() {
        String payload = githubClient.get("/orgs/Cantara/repos").body();
        return Arrays.asList(JsonbBuilder.create().fromJson(payload, GitHubRepository[].class));

    }
}
