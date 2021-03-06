package no.cantara.docsite.domain.snyk;

import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.WorkerTask;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueSnykTestTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(QueueSnykTestTask.class);

    private final CacheStore cacheStore;

    public QueueSnykTestTask(DynamicConfiguration configuration, ExecutorService executor, CacheStore cacheStore) {
        super(configuration, executor);
        this.cacheStore = cacheStore;
    }

    @Override
    public boolean execute() {
        cacheStore.getCacheKeys().iterator().forEachRemaining(entry -> executor().queue(new FetchSnykTestTask(configuration(), executor(), cacheStore, entry.getKey())));
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s", getClass().getSimpleName());
    }

}
