package no.cantara.docsite.executor;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Worker implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Worker.class);
    private final Task task;

    public Worker(Task task) {
        this.task = task;
    }

    @Override
    public void run() {
        try {
            task.execute();
        } catch (Throwable e) {
            if ((e instanceof HystrixRuntimeException)) {
                LOG.error("{} -- {}", task, e.getMessage());

            } else if (!(e.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(e);

            }
        }
    }
}
