package no.cantara.docsite.domain.github.commits;

import no.cantara.docsite.cache.CacheInitializer;
import no.cantara.docsite.cache.CacheShaKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.util.CommonUtil;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyNamingStrategy;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class PushCommitRevisionTest {

    private static final Logger LOG = LoggerFactory.getLogger(PushCommitRevisionTest.class);

    static DynamicConfiguration configuration() {
        DynamicConfiguration configuration = new StoreBasedDynamicConfiguration.Builder()
                .propertiesResource("application-defaults.properties")
                .propertiesResource("security.properties")
                .build();
        return configuration;
    }

    static CacheStore cacheStore() {
        return CacheInitializer.initialize(configuration());
    }

    @Test(enabled = false)
    public void thatCommitRevisionCacheCanGroupData() throws IOException {
        CacheStore cacheStore = cacheStore();
        String dummyCommits1 = null;
        String dummyCommits2 = null;
        try (InputStream json = ClassLoader.getSystemResourceAsStream("github/PushCommitsEvent.json")) {
            dummyCommits1 = CommonUtil.writeInputToOutputStream(json).toString();
        }
        try (InputStream json = ClassLoader.getSystemResourceAsStream("github/PushCommitsEvent2.json")) {
            dummyCommits2 = CommonUtil.writeInputToOutputStream(json).toString();
        }

        JsonbConfig config = new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES);
        Jsonb jsonb = JsonbBuilder.create(config);


        CommitRevision[] dummyCommitEvent1 = jsonb.fromJson(dummyCommits1, CommitRevision[].class);
//        LOG.trace("event1: {}", Arrays.stream(dummyCommitEvent1).map(CommitRevision::toString).collect(Collectors.joining("\n")));

        for(CommitRevision commitRevision : dummyCommitEvent1) {
            cacheStore.getCommits().put(CacheShaKey.of("Cantara", "Dummy", commitRevision.sha), commitRevision);
        }

        CommitRevision[] dummyCommitEvent2 = jsonb.fromJson(dummyCommits2, CommitRevision[].class);
//        LOG.trace("event2: {}", Arrays.stream(dummyCommitEvent2).map(CommitRevision::toString).collect(Collectors.joining("\n")));

        for(CommitRevision commitRevision : dummyCommitEvent2) {
            cacheStore.getCommits().put(CacheShaKey.of("Cantara", "Dummy2", commitRevision.sha), commitRevision);
        }

        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger countDymmy1 = new AtomicInteger(0);
        AtomicInteger countDymmy2 = new AtomicInteger(0);
        cacheStore.getCommits().iterator().forEachRemaining(a -> {
            count.incrementAndGet();
            if (a.getKey().compareTo("Cantara", "Dummy")) {
                countDymmy1.incrementAndGet();
            }
            if (a.getKey().compareTo("Cantara", "Dummy2")) {
                countDymmy2.incrementAndGet();
            }
        });


        LOG.trace("Cache-size: {} -- {} -- {}", count.get(), countDymmy1.get(), countDymmy2.get());
        cacheStore.getCacheManager().close();

    }
}
