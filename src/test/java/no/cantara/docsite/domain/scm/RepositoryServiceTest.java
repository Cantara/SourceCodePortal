package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.CacheGroupKey;
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

        CacheGroupKey groupKey1 = CacheGroupKey.of("Cantara", "g1");
        CacheGroupKey groupKey2 = CacheGroupKey.of("Cantara", "g2");
        CacheGroupKey groupKey3 = CacheGroupKey.of("Cantara", "g3");

        cacheStore.getCacheGroupKeys().put(groupKey3, groupKey3.groupId);
        cacheStore.getCacheGroupKeys().put(groupKey1, groupKey1.groupId);
        cacheStore.getCacheGroupKeys().put(groupKey2, groupKey2.groupId);

        CacheRepositoryKey key1 = CacheRepositoryKey.of("Cantara", "repo1", "master", "g1");
        cacheStore.getRepositories().put(key1, RepositoryDefinition.of(configuration, key1, "id1", "desc", "g1", "http://example.com"));

        CacheRepositoryKey key2 = CacheRepositoryKey.of("Cantara", "repo2", "master", "g2");
        cacheStore.getRepositories().put(key2, RepositoryDefinition.of(configuration, key2, "id2", "desc", "g1", "http://example.com"));

        CacheRepositoryKey key3 = CacheRepositoryKey.of("Cantara", "repo3", "master", "g3");
        cacheStore.getRepositories().put(key3, RepositoryDefinition.of(configuration, key3, "id3", "desc", "g1", "http://example.com"));

        CacheRepositoryKey key4 = CacheRepositoryKey.of("Cantara", "repo4", "master", "g1");
        cacheStore.getRepositories().put(key4, RepositoryDefinition.of(configuration, key4, "id4", "desc", "g2", "http://example.com"));

        RepositoryService service = new RepositoryService(cacheStore);
        LOG.trace("{}", service.get(key1));

        service.groupKeySet().forEach(key -> LOG.trace("groupId: {}", key.groupId));

        service.keySet().forEach(key -> LOG.trace("keySet: {}", key.groupId));

        service.sortedEntrySet().forEach((k, v) -> {
            v.forEach(r -> {
                LOG.trace("k: {} -- v: {}", k.groupId, r.cacheRepositoryKey.repoName);
            });
        });

    }

}
