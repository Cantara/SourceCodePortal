package no.cantara.docsite.executor;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerRunner implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerRunner.class);
    private final Worker worker;

    public WorkerRunner(Worker worker) {
        this.worker = worker;
    }

    @Override
    public void run() {
        try {
            worker.getTask().execute();
        } catch (Throwable e) {
            if ((e instanceof HystrixRuntimeException)) {
                LOG.error("{} -- {}", worker.getTask(), e.getMessage());

            } else if (!(e.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(e);

            }
        }
    }
}
