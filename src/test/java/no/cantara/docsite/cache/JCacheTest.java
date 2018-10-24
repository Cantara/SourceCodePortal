package no.cantara.docsite.cache;

import no.cantara.docsite.domain.github.contents.RepositoryContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.util.Spliterator;

//@Listeners(TestServerListener.class)
public class JCacheTest {

    private static final Logger LOG = LoggerFactory.getLogger(JCacheTest.class);

    //    @Inject
    CacheStore cacheStore;

    @Ignore
    @Test
    public void testRICacheProvider() {
        Cache<CacheKey, RepositoryContents> cache = cacheStore.getPages();
//        cache.put("key1", "value1");
//        cache.put("key2", "value2");
    }

    @Test
    public void testName() {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();

        MutableConfiguration<String, String> cacheConfig = new MutableConfiguration<>();

        Cache<String, String> cache = cacheManager.createCache("test", cacheConfig);

        cache.put("1", "a");;
        cache.put("2", "b");;

        Spliterator<Cache.Entry<String, String>> spliterator = cache.spliterator();

        int x = spliterator.getComparator().compare(null, null);

        LOG.trace("{}", cache.spliterator());

    }
}
