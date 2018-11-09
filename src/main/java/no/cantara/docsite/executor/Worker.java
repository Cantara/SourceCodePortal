package no.cantara.docsite.executor;

import com.netflix.hystrix.exception.HystrixRuntimeException;

public class Worker implements Runnable {

    private final Task task;

    public Worker(Task task) {
        this.task = task;
    }

    @Override
    public void run() {
        try {
            task.execute();
        } catch (Throwable e) {
            if (!((e instanceof HystrixRuntimeException) || (e.getCause() instanceof InterruptedException))) {
                throw new RuntimeException(e);
            }
        }
    }
}
