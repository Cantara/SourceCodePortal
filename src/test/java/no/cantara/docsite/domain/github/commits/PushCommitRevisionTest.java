package no.cantara.docsite.domain.github.commits;

import no.cantara.docsite.cache.CacheInitializer;
import no.cantara.docsite.cache.CacheShaKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.util.CommonUtil;
import no.cantara.docsite.util.JsonbFactory;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class PushCommitRevisionTest {

    private static final Logger LOG = LoggerFactory.getLogger(PushCommitRevisionTest.class);

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
    public void thatCommitRevisionCacheCanGroupData() throws IOException {
        CacheStore cacheStore = cacheStore();
        String dummyCommits1 = null;
        String dummyCommits2 = null;
        try (InputStream json = ClassLoader.getSystemResourceAsStream("github/PullCommitsEvent.json")) {
            dummyCommits1 = CommonUtil.writeInputToOutputStream(json).toString();
        }
        try (InputStream json = ClassLoader.getSystemResourceAsStream("github/PullCommitsEvent2.json")) {
            dummyCommits2 = CommonUtil.writeInputToOutputStream(json).toString();
        }

        CommitRevisionBinding[] dummyCommitEvent1 = JsonbFactory.instance().fromJson(dummyCommits1, CommitRevisionBinding[].class);
//        LOG.trace("event1: {}", Arrays.stream(dummyCommitEvent1).map(CommitRevision::toString).collect(Collectors.joining("\n")));

        for(CommitRevisionBinding commitRevision : dummyCommitEvent1) {
            CacheShaKey cacheShaKey = CacheShaKey.of("Cantara", "dummyRepo", "Dummy", "master", commitRevision.sha);
            cacheStore.getCommits().put(cacheShaKey, commitRevision.asCommitRevision(cacheShaKey));
        }

        CommitRevisionBinding[] dummyCommitEvent2 = JsonbFactory.instance().fromJson(dummyCommits2, CommitRevisionBinding[].class);
//        LOG.trace("event2: {}", Arrays.stream(dummyCommitEvent2).map(CommitRevision::toString).collect(Collectors.joining("\n")));

        for(CommitRevisionBinding commitRevision : dummyCommitEvent2) {
            CacheShaKey cacheShaKey = CacheShaKey.of("Cantara", "dummyRepo", "Dummy2", "master", commitRevision.sha);
            cacheStore.getCommits().put(cacheShaKey, commitRevision.asCommitRevision(cacheShaKey));
        }

        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger countDymmy1 = new AtomicInteger(0);
        AtomicInteger countDymmy2 = new AtomicInteger(0);
        cacheStore.getCommits().iterator().forEachRemaining(a -> {
            count.incrementAndGet();
            if (a.getKey().compareToUsingRepoName("Cantara", "Dummy")) {
                countDymmy1.incrementAndGet();
            }
            if (a.getKey().compareToUsingRepoName("Cantara", "Dummy2")) {
                countDymmy2.incrementAndGet();
            }
        });


        LOG.trace("Cache-size: {} -- {} -- {}", count.get(), countDymmy1.get(), countDymmy2.get());

    }
}
