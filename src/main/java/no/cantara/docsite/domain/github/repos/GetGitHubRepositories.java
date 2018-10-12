package no.cantara.docsite.domain.github.repos;

import no.cantara.docsite.client.GitHubClient;
import no.cantara.docsite.model.github.pull.GitHubRepository;
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
