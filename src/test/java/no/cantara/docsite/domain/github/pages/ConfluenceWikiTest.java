package no.cantara.docsite.domain.github.pages;

import no.cantara.docsite.cache.CacheCantaraWikiKey;
import no.cantara.docsite.cache.CacheInitializer;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.confluence.cantara.FetchCantaraWikiTask;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.testng.annotations.Test;

import java.io.IOException;

public class ConfluenceWikiTest {

    static DynamicConfiguration configuration() {
        DynamicConfiguration configuration = new StoreBasedDynamicConfiguration.Builder()
                .propertiesResource("application-defaults.properties")
                .propertiesResource("security.properties")
                .propertiesResource("application.properties")
                .build();
        return configuration;
    }

    static CacheStore cacheStore() {
        return CacheInitializer.initialize(configuration());
    }

    @Test
    public void testConfluenceWikiRender() throws IOException {
        FetchCantaraWikiTask task = new FetchCantaraWikiTask(configuration(), null, cacheStore(), CacheCantaraWikiKey.of("46137421"));
        task.execute();
    }
}
