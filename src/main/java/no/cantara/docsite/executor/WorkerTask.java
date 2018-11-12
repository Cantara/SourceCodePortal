package no.cantara.docsite.executor;

import no.ssb.config.DynamicConfiguration;

abstract public class WorkerTask implements Task {

    private final DynamicConfiguration configuration;
    private final ExecutorService executor;

    public WorkerTask(DynamicConfiguration configuration, ExecutorService executor) {
        this.configuration = configuration;
        this.executor = executor;
    }

    public DynamicConfiguration configuration() {
        return configuration;
    }

    public ExecutorService executor() {
        return executor;
    }

}
