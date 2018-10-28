package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.CacheInitializer;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.cache.CacheStore;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class RepositoryServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryServiceTest.class);

    static DynamicConfiguration configuration() {
        DynamicConfiguration configuration = new StoreBasedDynamicConfiguration.Builder()
                .propertiesResource("application-defaults.properties")
                .propertiesResource("security.properties")
                .propertiesResource("application.properties")
                .build();
        return configuration;
    }

    static CacheStore cacheStore(DynamicConfiguration configuration) {
        return CacheInitializer.initialize(configuration);
    }

    @Test
    public void testRepositoryService() {
        DynamicConfiguration configuration = configuration();
        CacheStore cacheStore = cacheStore(configuration);

        CacheRepositoryKey key1 = CacheRepositoryKey.of("Cantara", "repo1", "master", "group1");
        cacheStore.getRepositories().put(key1, RepositoryDefinition.of(configuration, key1, "id", "desc", "group", "http://example.com"));

        CacheRepositoryKey key2 = CacheRepositoryKey.of("Cantara", "repo2", "master", "group1");
        cacheStore.getRepositories().put(key2, RepositoryDefinition.of(configuration, key2, "id", "desc", "group", "http://example.com"));

        CacheRepositoryKey key3 = CacheRepositoryKey.of("Cantara", "repo3", "master", "group1");
        cacheStore.getRepositories().put(key3, RepositoryDefinition.of(configuration, key3, "id", "desc", "group", "http://example.com"));

        RepositoryService service = new RepositoryService(cacheStore);
        LOG.trace("{}", service.get(key1));
    }

}
