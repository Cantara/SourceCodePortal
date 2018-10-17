package no.cantara.docsite.executor;

import no.ssb.config.DynamicConfiguration;

import java.util.concurrent.atomic.AtomicInteger;

abstract public class WorkerTask implements Task {

    private final AtomicInteger retryCount = new AtomicInteger(-1);
    private final DynamicConfiguration configuration;
    private final ExecutorService executor;

    public WorkerTask(DynamicConfiguration configuration, ExecutorService executor) {
        this.configuration = configuration;
        this.executor = executor;
    }

    public DynamicConfiguration getConfiguration() {
        return configuration;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public int incrementCount() {
       return retryCount.incrementAndGet();
    }

}
