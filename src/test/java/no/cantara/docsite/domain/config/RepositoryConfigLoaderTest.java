package no.cantara.docsite.domain.config;

import no.cantara.docsite.cache.CacheInitializer;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.executor.ExecutorThreadPool;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
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

    @Test
    public void testRepositoryConfig() throws Exception {
        ExecutorThreadPool executorService = new ExecutorThreadPool();
        executorService.start();
        DynamicConfiguration configuration = configuration();
        CacheStore cacheStore = cacheStore(configuration);

        RepositoryConfigLoader service = new RepositoryConfigLoader(configuration, cacheStore);
        service.load();

        assertEquals(cacheStore.getRepositoryGroupsByGroupId("SourceCodePortal-t").size(), 1);
        assertTrue(cacheStore.getRepositoryGroupsByGroupId("Whydah-t").size() > 15);
    }
}
