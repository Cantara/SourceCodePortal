package no.cantara.docsite.executor;

import no.cantara.docsite.cache.CacheStore;
import no.ssb.config.DynamicConfiguration;

public interface ScheduledExecutorService {

    long WAIT_FOR_TERMINATION = 100;

    void start();

    void shutdown();

    java.util.concurrent.ScheduledExecutorService getThreadPool();

    static ScheduledExecutorService create(DynamicConfiguration configuration, ExecutorService executorService, CacheStore cacheStore) {
        return new ScheduledExecutorThreadPool(configuration, executorService, cacheStore);
    }
}
