package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.CacheGroupKey;
import no.cantara.docsite.cache.CacheInitializer;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.RepositoryConfigLoader;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

public class ScmRepositoryServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(ScmRepositoryServiceTest.class);

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

    @Ignore
    @Test
    public void testRepositoryService() {
        DynamicConfiguration configuration = configuration();
        CacheStore cacheStore = cacheStore(configuration);
        RepositoryConfigLoader loader = new RepositoryConfigLoader(configuration, cacheStore);

        cacheStore.getRepositoryConfig().getConfig().repos.forEach((k,v) -> {
            LOG.trace("configRepo: {}", v);
        });

        loader.load();

        ScmRepositoryService service = new ScmRepositoryService(cacheStore);
        service.keySet().forEach(k -> {
//            LOG.trace("key: {}", k);
        });
//        service.sortedEntrySet().entrySet().forEach(entry -> {
//            LOG.trace("group: {} -- count: {}", entry.getKey().groupId, entry.getValue().size());
//            entry.getValue().forEach(scmRepository -> {
//                LOG.trace("  {}", scmRepository.cacheRepositoryKey.repoName );
//            });
//        });

        service.groupedRepositories().forEach((k, v) -> {
            if (k.isGroup()) {
                LOG.trace("isGroup: {}", k.groupId);
                v.forEach(r -> {
                    LOG.trace("  {}", r);
//                    LOG.trace("  {}", r.cacheRepositoryKey.repoName);
                });
            }
        });
    }

//    @Ignore
    @Test
    public void testRepository() {
        DynamicConfiguration configuration = configuration();
        CacheStore cacheStore = cacheStore(configuration);

        CacheGroupKey groupKey1 = CacheGroupKey.of("Cantara", "g1");
        CacheGroupKey groupKey2 = CacheGroupKey.of("Cantara", "g2");
        CacheGroupKey groupKey3 = CacheGroupKey.of("Cantara", "g3");

        cacheStore.getCacheGroupKeys().put(groupKey3, groupKey3.groupId);
        cacheStore.getCacheGroupKeys().put(groupKey1, groupKey1.groupId);
        cacheStore.getCacheGroupKeys().put(groupKey2, groupKey2.groupId);

        CacheRepositoryKey key1 = CacheRepositoryKey.of("Cantara", "repo1", "master", "g1", false);
        cacheStore.getRepositories().put(key1, ScmRepository.of(configuration, key1, "dn", "cd", new LinkedHashMap<>(), "id1", "desc", "g1", "Apache-2.0", "http://example.com"));

        CacheRepositoryKey key2 = CacheRepositoryKey.of("Cantara", "repo2", "master", "g2", false);
        cacheStore.getRepositories().put(key2, ScmRepository.of(configuration, key2, "dn", "cd", new LinkedHashMap<>(),"id2", "desc", "g1", "Apache-2.0", "http://example.com"));

        CacheRepositoryKey key3 = CacheRepositoryKey.of("Cantara", "repo3", "master", "g3", false);
        cacheStore.getRepositories().put(key3, ScmRepository.of(configuration, key3, "dn", "cd", new LinkedHashMap<>(),"id3", "desc", "g1", "Apache-2.0", "http://example.com"));

        CacheRepositoryKey key4 = CacheRepositoryKey.of("Cantara", "repo4", "master", "g1", false);
        cacheStore.getRepositories().put(key4, ScmRepository.of(configuration, key4, "dn", "cd", new LinkedHashMap<>(),"id4", "desc", "g2", "Apache-2.0", "http://example.com"));

        ScmRepositoryService service = new ScmRepositoryService(cacheStore);
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
