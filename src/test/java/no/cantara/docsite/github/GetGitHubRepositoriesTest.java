package no.cantara.docsite.github;

import no.cantara.docsite.test.server.TestServer;
import no.cantara.docsite.test.server.TestServerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.List;

@Listeners(TestServerListener.class)
public class GetGitHubRepositoriesTest {

    private static final Logger LOG = LoggerFactory.getLogger(GetGitHubRepositoriesTest.class);

    @Inject
    TestServer server;

    @Test(enabled = false)
    public void findOrgGitHubRepos() {
        GetGitHubRepositories repos = new GetGitHubRepositories(server.getConfiguration());
        List<GitHubRepository> result = repos.getOrganizationRepos();
        LOG.trace("repos: {}", result);
    }

}
