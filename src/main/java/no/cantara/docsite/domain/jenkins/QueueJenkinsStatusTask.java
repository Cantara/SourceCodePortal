package no.cantara.docsite.domain.jenkins;

import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.WorkerTask;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueJenkinsStatusTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(QueueJenkinsStatusTask.class);

    private final CacheStore cacheStore;

    public QueueJenkinsStatusTask(DynamicConfiguration configuration, ExecutorService executor, CacheStore cacheStore) {
        super(configuration, executor);
        this.cacheStore = cacheStore;
    }

    @Override
    public void execute() {
        cacheStore.getCacheKeys().iterator().forEachRemaining(entry -> {
            getExecutor().queue(new FetchJenkinsStatusTask(getConfiguration(), getExecutor(), cacheStore, entry.getKey()));
        });
    }

    @Override
    public String toString() {
        return String.format("%s", getClass().getSimpleName());
    }

}
