package no.cantara.docsite.executor;

import no.cantara.docsite.cache.CacheStore;
import no.ssb.config.DynamicConfiguration;

import java.util.Set;

public interface ScheduledExecutorService {

    long WAIT_FOR_TERMINATION = 100;

    void queue(ScheduledWorker scheduledWorker);

    void start();

    void shutdown();

    java.util.concurrent.ScheduledExecutorService getThreadPool();

    Set<ScheduledWorker> getScheduledWorkers();

    static ScheduledExecutorService create(DynamicConfiguration configuration, ExecutorService executorService, CacheStore cacheStore) {
        return new ScheduledExecutorThreadPool(configuration, executorService, cacheStore);
    }
}
