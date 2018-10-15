package no.cantara.docsite.domain.github.pages;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.domain.config.RepositoryConfigLoader;
import no.cantara.docsite.domain.config.RepositoryConfigLoaderTest;
import no.cantara.docsite.test.server.TestServer;
import no.cantara.docsite.test.server.TestServerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Map;

@Listeners(TestServerListener.class)
public class GitHubPageServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryConfigLoaderTest.class);

    @Inject TestServer server;

    @Test
    public void testName() {
        server.getApplication().enableExecutorService();
        RepositoryConfigLoader service = new RepositoryConfigLoader(server.getConfiguration());
        Map<String, RepositoryConfigLoader.Group> discoveredRepositoryGroups = service.build();
        discoveredRepositoryGroups.values().forEach(g -> {
            LOG.trace("group: {}", g.groupId);
            g.getRepositories().values().forEach(r -> {
                server.getExecutorService().queue(new FetchPageTask(server.getConfiguration(), server.getExecutorService(), server.getCacheStore(), CacheKey.of(g.organization, r.repoName, r.branch), r.readmeURL));
                LOG.trace("  repo: {} - {} - {} - {} - {}", r.repoName, r.repoURL, r.rawRepoURL, r.readmeURL, r.contentsURL);
            });
        });
    }
}
