package no.cantara.docsite.cache;

import no.cantara.docsite.domain.github.commits.GitHubCommitRevision;
import no.cantara.docsite.domain.github.contents.GitHubRepositoryContents;
import no.cantara.docsite.domain.github.repos.GitHubRepository;
import no.cantara.docsite.test.TestData;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TestDataTest {

    static DynamicConfiguration configuration() {
        return new StoreBasedDynamicConfiguration.Builder()
                .propertiesResource("application-defaults.properties")
                .propertiesResource("security.properties")
                .propertiesResource("application.properties")
                .build();
    }

    static CacheStore cacheStore(DynamicConfiguration configuration) {
        return CacheInitializer.initialize(configuration);
    }

    @Test
    public void testRepos() {
        List<CacheKey> repoKeys = TestData.instance().repoKeys("Cantara");
        List<GitHubRepository> repos = TestData.instance().repos("Cantara");
        assertEquals(repoKeys.size(), repos.size());
    }

    @Test
    public void testCommitRevisions() {
        Map<CacheShaKey,GitHubCommitRevision> map = TestData.instance().commitRevisions("Cantara");
        assertTrue(map.size() > 1000);
    }

    @Test
    public void testReadmeContents() {
        Map<CacheKey, GitHubRepositoryContents> map = TestData.instance().readmeContents("Cantara");
        assertTrue(map.size() > 70);
    }

    @Test
    public void testMavenPOMContents() {
        Map<CacheKey, GitHubRepositoryContents> map = TestData.instance().mavenPOMContents("Cantara");
        assertTrue(map.size() > 50);
    }

    @Test
    public void testPopulateCacheStore() {
        DynamicConfiguration configuration = configuration();
        CacheStore cacheStore = cacheStore(configuration);
        TestData.instance().populateCacheStore(configuration, cacheStore);
        assertTrue(CacheHelper.cacheSize(cacheStore.getRepositories()) > 70);
        assertTrue(CacheHelper.cacheSize(cacheStore.getCommits()) > 1000);
        assertTrue(CacheHelper.cacheSize(cacheStore.getReadmeContents()) > 70);
        assertTrue(CacheHelper.cacheSize(cacheStore.getMavenProjects()) > 50);
    }
}
