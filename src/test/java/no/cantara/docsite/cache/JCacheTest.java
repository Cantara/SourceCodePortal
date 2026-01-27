package no.cantara.docsite.cache;

import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.jsr107.ri.RICache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.util.Random;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//@Listeners(TestServerListener.class)
public class JCacheTest {

    private static final Logger LOG = LoggerFactory.getLogger(JCacheTest.class);

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
    public void testHashCode() {
        CacheStore cacheStore = cacheStore(configuration());
        CacheRepositoryKey k1 = CacheRepositoryKey.of("org", "repo","branch", "groupId", false);
        CacheRepositoryKey k2 = CacheRepositoryKey.of("org", "repo", "branch", "groupId", false);
        assertTrue(k1.equals(k2));
        assertEquals(k1, k2);
        assertEquals(k1.hashCode(), k2.hashCode());
    }

    @Disabled
    @Test
    public void testName() {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();

        MutableConfiguration<String, String> cacheConfig = new MutableConfiguration<>();

        Cache<String, String> cache = cacheManager.createCache("test", cacheConfig);

        Random r = new Random();
        for(int n=0; n<10000; n++) {
            cache.put(String.valueOf(n), String.valueOf(r.nextInt(26) + 'a'));
        }

        Spliterator<Cache.Entry<String, String>> spliterator = cache.spliterator();
//        new LinkedHashMap<>().putAll();


//        TreeMap<String, String> map = new TreeMap<>();
//        map.putAll(cache.);


//        int x = spliterator.getComparator().compare(null, null);

        RICache<String,String> ri = (RICache<String, String>) cache;

        LOG.trace("ri size: {}", ri.getSize());

        LOG.trace("ri helper size: {}", CacheHelper.cacheSize(cache));


//        int countA = 0;
//        int countB = 0;
//        int countC = 0;
//        int countD = 0;
//
//        int n=0;
//        for(; n<1000; n++) {
//            {
//                long now = System.currentTimeMillis();
//                long count = StreamSupport.stream(cache.spliterator(), false).count();
//                long future = System.currentTimeMillis() - now;
//                countA += future;
//            }
//            {
//                long now = System.currentTimeMillis();
//                long count = StreamSupport.stream(cache.spliterator(), true).count();
//                long future = System.currentTimeMillis() - now;
//                countB += future;
//            }
//            {
//                long now = System.currentTimeMillis();
//                AtomicInteger count = new AtomicInteger(0);
//                cache.iterator().forEachRemaining(a -> count.incrementAndGet());
//                long future = System.currentTimeMillis() - now;
//                countC += future;
//            }
//            {
//                long now = System.currentTimeMillis();
//                long count = ri.getSize();
//                long future = System.currentTimeMillis() - now;
//                countD += future;
//            }
//        }
//        LOG.trace("n: {} -- a: {} -- b: {} -- c: {} -- d: {}", n, (countA/n), (countB/n), (countC/n), (countD/n));

    }
}
