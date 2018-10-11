package no.cantara.docsite.cache;

import no.cantara.docsite.test.server.TestServer;
import no.cantara.docsite.test.server.TestServerListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.inject.Inject;

@Listeners(TestServerListener.class)
public class JCacheTest {

    @Inject
    TestServer server;

    @Test
    public void testRICacheProvider() {
        CacheManager cacheManager = server.getCacheManager();
        MutableConfiguration<String, String> config = new MutableConfiguration<>();
        Cache<String, String> cache = cacheManager.createCache("simpleCache", config);
        cache.put("key1", "value1");
        cache.put("key2", "value2");
    }

}
