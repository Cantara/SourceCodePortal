package no.cantara.docsite.executor;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerRunner implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerRunner.class);
    private final ExecutorService executor;
    private final Worker worker;

    public WorkerRunner(ExecutorService executor, Worker worker) {
        this.executor = executor;
        this.worker = worker;
    }

    @Override
    public void run() {
        try {
            boolean success = worker.getTask().execute();
            if (!success) {
                LOG.warn("Re-queue: {} ({})", worker.getTask(), worker.retryCount());
                executor.requeue(worker); // always requeue on failure
            }

        } catch (Throwable e) {
            LOG.warn("Re-queue: {} ({})", worker.getTask(), worker.retryCount());
            executor.requeue(worker); // always requeue on failure

            if ((e instanceof CallNotPermittedException)) {
                LOG.error("{} -- Circuit breaker open: {}", worker.getTask(), e.getMessage());

            } else if (!(e.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(e);
            }
        }
    }
}
