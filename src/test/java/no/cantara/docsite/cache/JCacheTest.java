package no.cantara.docsite.cache;

import no.cantara.docsite.domain.github.contents.RepositoryContents;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import javax.cache.Cache;

//@Listeners(TestServerListener.class)
public class JCacheTest {

//    @Inject
    CacheStore cacheStore;

    @Ignore
    @Test
    public void testRICacheProvider() {
        Cache<CacheKey, RepositoryContents> cache = cacheStore.getPages();
//        cache.put("key1", "value1");
//        cache.put("key2", "value2");
    }

}
