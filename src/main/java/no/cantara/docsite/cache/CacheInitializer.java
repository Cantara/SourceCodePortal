package no.cantara.docsite.cache;

import no.ssb.config.DynamicConfiguration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

public class CacheInitializer {

    // TODO: evaluate configuration for hazelcast provider
    public static CacheManager initialize(DynamicConfiguration dynamicConfiguration) {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();
        return cacheManager;
    }
}
