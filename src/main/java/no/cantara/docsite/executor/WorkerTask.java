package no.cantara.docsite.executor;

import no.ssb.config.DynamicConfiguration;

import java.util.concurrent.atomic.AtomicInteger;

abstract public class WorkerTask implements Task {

    private final AtomicInteger retryCount = new AtomicInteger(-1);
    private final DynamicConfiguration configuration;
    private final ExecutorThreadPool executor;

    public WorkerTask(DynamicConfiguration configuration, ExecutorThreadPool executor) {
        this.configuration = configuration;
        this.executor = executor;
    }

    public DynamicConfiguration getConfiguration() {
        return configuration;
    }

    public ExecutorThreadPool getExecutor() {
        return executor;
    }

    public int incrementCount() {
       return retryCount.incrementAndGet();
    }

}
