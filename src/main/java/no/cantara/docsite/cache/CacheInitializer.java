package no.cantara.docsite.cache;

import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

public class CacheInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(CacheInitializer.class);

    public static CacheStore initialize(DynamicConfiguration configuration) {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        LOG.info("Initializing JCache Provider: {}", cachingProvider.getDefaultURI());
        CacheManager cacheManager = cachingProvider.getCacheManager();
        CacheStore cacheStore = new CacheStore(configuration, cacheManager);
        cacheStore.initialize();
        return cacheStore;
    }

}
