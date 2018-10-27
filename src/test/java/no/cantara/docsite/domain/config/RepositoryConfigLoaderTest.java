package no.cantara.docsite.domain.config;

import no.cantara.docsite.cache.CacheInitializer;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.github.repos.RepositoryBinding;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.util.JsonbFactory;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertTrue;

public class RepositoryConfigLoaderTest {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryConfigLoaderTest.class);

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


    @Test(enabled = false)
    public void findOrgGitHubRepos() {
        DynamicConfiguration configuration = configuration();
        RepositoryConfigLoader loader = new RepositoryConfigLoader(configuration, cacheStore(configuration));
        List<RepositoryBinding> result = loader.getOrganizationRepos("Cantara");
        LOG.trace("repos: {}\nsize:{}", result, result.size());
    }

    @Test(enabled = false)
    public void testRepositoryConfig() throws Exception {
        ExecutorService executorService = ExecutorService.create();
        executorService.start();
        DynamicConfiguration configuration = configuration();
        CacheStore cacheStore = cacheStore(configuration);

        RepositoryConfigLoader service = new RepositoryConfigLoader(configuration, cacheStore);
        service.load();

        assertTrue(cacheStore.getRepositoryGroupsByGroupId("SourceCodePortal-t").size() > 1);
        assertTrue(cacheStore.getRepositoryGroupsByGroupId("Whydah-t").size() > 15);

        LOG.trace(JsonbFactory.prettyPrint(cacheStore.getConfiguredRepositories()));
    }
}
