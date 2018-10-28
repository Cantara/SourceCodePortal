package no.cantara.docsite.cache;

import no.cantara.docsite.domain.scm.RepositoryContents;
import org.jsr107.ri.RICache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.util.Random;
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

    @Ignore
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
