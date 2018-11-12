package no.cantara.docsite.executor;

import java.util.concurrent.atomic.AtomicInteger;

public class Worker {

    private final AtomicInteger retryCount = new AtomicInteger(-1);
    private final Task task;

    public Worker(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    public int retryCount() {
        return retryCount.get();
    }

    public int incrementCount() {
        return retryCount.incrementAndGet();
    }

}
