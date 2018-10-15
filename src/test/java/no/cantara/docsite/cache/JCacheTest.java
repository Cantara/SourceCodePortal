package no.cantara.docsite.cache;

import no.cantara.docsite.domain.github.contents.RepositoryContents;
import no.cantara.docsite.test.server.TestServerListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.cache.Cache;
import javax.inject.Inject;

@Listeners(TestServerListener.class)
public class JCacheTest {

    @Inject
    CacheStore cacheStore;

    @Test
    public void testRICacheProvider() {
        Cache<CacheKey, RepositoryContents> cache = cacheStore.getPages();
//        cache.put("key1", "value1");
//        cache.put("key2", "value2");
    }

}
