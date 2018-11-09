package no.cantara.docsite.domain.shields;

import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.WorkerTask;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueShieldsStatusTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(QueueShieldsStatusTask.class);

    private final CacheStore cacheStore;

    public QueueShieldsStatusTask(DynamicConfiguration configuration, ExecutorService executor, CacheStore cacheStore) {
        super(configuration, executor);
        this.cacheStore = cacheStore;
    }

    @Override
    public void execute() {
        cacheStore.getCacheKeys().iterator().forEachRemaining(entry -> {
            for(FetchShieldsStatusTask.Fetch fetch : FetchShieldsStatusTask.Fetch.values()) {
                executor().queue(new FetchShieldsStatusTask(configuration(), executor(), cacheStore, entry.getKey(), fetch));
            }
        });
    }

    @Override
    public String toString() {
        return String.format("%s", getClass().getSimpleName());
    }

}
