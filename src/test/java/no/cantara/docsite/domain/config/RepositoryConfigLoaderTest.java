package no.cantara.docsite.domain.config;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.domain.github.commits.FetchCommitRevisionTask;
import no.cantara.docsite.domain.github.pages.FetchPageTask;
import no.cantara.docsite.domain.maven.FetchMavenPOMTask;
import no.cantara.docsite.test.server.TestServer;
import no.cantara.docsite.test.server.TestServerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Map;

@Listeners(TestServerListener.class)
public class RepositoryConfigLoaderTest {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryConfigLoaderTest.class);

    @Inject
    TestServer server;

    @Test
    public void testRepositoryConfig() throws Exception {
        server.getApplication().enableExecutorService();
        RepositoryConfigLoader service = new RepositoryConfigLoader(server.getConfiguration());
        RepositoryConfig config = service.repositoryConfig;
        LOG.trace("Repo-Config: {}", config);
        Map<String, RepositoryConfigLoader.Group> discoveredRepositoryGroups = service.build();
        discoveredRepositoryGroups.values().forEach(g -> {
            LOG.trace("group: {}", g.groupId);
            g.getRepositories().values().forEach(r -> {
                LOG.trace("  repo: {} - {} - {} - {} - {}", r.repoName, r.repoURL, r.rawRepoURL, r.readmeURL, r.contentsURL);
                CacheKey cacheKey = CacheKey.of(g.organization, r.repoName, r.branch);
                server.getExecutorService().queue(new FetchMavenPOMTask(server.getConfiguration(), server.getExecutorService(), server.getCacheStore(), cacheKey, r.contentsURL));
                server.getExecutorService().queue(new FetchPageTask(server.getConfiguration(), server.getExecutorService(), server.getCacheStore(), cacheKey, r.readmeURL));
                server.getExecutorService().queue(new FetchCommitRevisionTask(server.getConfiguration(), server.getExecutorService(), server.getCacheStore(), cacheKey));
            });
        });
        Thread.sleep(5000);
        server.getCacheStore().getPages().forEach(repo -> {
//            LOG.trace("{} => {}", repo.getKey(), repo.getValue().getDecodedContent());
            LOG.trace("CacheKey: {}", repo.getKey());
        });
    }
}
