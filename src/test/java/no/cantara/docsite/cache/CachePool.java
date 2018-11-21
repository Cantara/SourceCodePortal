package no.cantara.docsite.cache;

import no.cantara.docsite.domain.config.RepositoryConfigService;
import no.ssb.config.DynamicConfiguration;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;

public class CachePool {

    private final DynamicConfiguration configuration;
    private final CacheManager cacheManager;
    private final RepositoryConfigService repositoryConfig;

    CachePool(DynamicConfiguration configuration, CacheManager cacheManager) {
        this.configuration = configuration;
        this.cacheManager = cacheManager;
        this.repositoryConfig = new RepositoryConfigService(configuration.evaluateToString("cache.config"));
    }

    public RepositoryConfigService getRepositoryConfigService() {
        return repositoryConfig;
    }

    public <V> Cache<String,V> createCache(String cacheName, Class<V> classValue) {
        MutableConfiguration<String, V> cacheConfig = new MutableConfiguration<>();
        cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
        cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
        return cacheManager.createCache(cacheName, cacheConfig);
    }

    public static String asRepositoryPath(String organization, String repoName, String branch) {
        return String.format("/%s/%s/%s", organization, repoName, branch);
    }
}
