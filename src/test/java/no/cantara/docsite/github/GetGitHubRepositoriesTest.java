package no.cantara.docsite.github;

import no.cantara.docsite.client.GitHubClient;
import no.cantara.docsite.test.server.TestServer;
import no.cantara.docsite.test.server.TestServerListener;
import no.cantara.docsite.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.net.http.HttpResponse;
import java.util.List;

@Listeners(TestServerListener.class)
public class GetGitHubRepositoriesTest {

    private static final Logger LOG = LoggerFactory.getLogger(GetGitHubRepositoriesTest.class);

    @Inject
    TestServer server;

    @Ignore
    @Test
    public void testGitHubApiLimit() {
        HttpResponse<String> response = new GitHubClient(server.getConfiguration()).get("/rate_limit");
        LOG.trace("GitHub API Limit: {}", JsonUtil.prettyPrint(JsonUtil.asJsonObject(response.body())));
    }

    @Test
    public void findOrgGitHubRepos() {
        GetGitHubRepositories repos = new GetGitHubRepositories(server.getConfiguration());
        List<GitHubRepository> result = repos.getOrganizationRepos();
        LOG.trace("repos: {}", result);
    }

}
