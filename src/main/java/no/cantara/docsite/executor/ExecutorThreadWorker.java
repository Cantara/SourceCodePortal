package no.cantara.docsite.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorThreadWorker {

    static int BLOCKING_QUEUE_SIZE = 250;
    static long WAIT_FOR_THREAD_POOL = 50;
    static long WAIT_FOR_TERMINATION = 100;
    static long SLEEP_INTERVAL = 100;
    private static Logger LOG = LoggerFactory.getLogger(ExecutorThreadWorker.class);
    private final BlockingQueue internalEventsQueue;
    private ThreadPoolExecutor executorThreadPool;
    private boolean isRunning;
    private ExecutorThreadCallback<Boolean> callback;

    public ExecutorThreadWorker() {
        this.internalEventsQueue = new ArrayBlockingQueue(BLOCKING_QUEUE_SIZE);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void start() {
        if (isRunning) {
            return;
        }

        executorThreadPool = new ThreadPoolExecutor(
                10, // core size
                50, // max size
                10 * 60, // idle timeout
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(BLOCKING_QUEUE_SIZE)); // queue with a size;

        try {
            executorThreadPool.execute(new Runnable() {
                public void run() {
                    isRunning = true;
                    while (!executorThreadPool.isTerminated()) {
                        try {
                            WorkerTask<?> event = (WorkerTask<?>) internalEventsQueue.take();
                            if (callback.call(event)) {
                                isRunning = false;
                                break;
                            }
                        } catch (InterruptedException e) {
                            LOG.trace("Exiting thread: {}", Thread.currentThread());
                        } catch (Exception e) {
                            LOG.error("Error or interrupted:", e);
                        }
                    }
                    LOG.trace("Exiting thread: {}", Thread.currentThread());
                }
            });
            TimeUnit.MILLISECONDS.sleep(WAIT_FOR_THREAD_POOL);

        } catch (InterruptedException e) {
            LOG.error("Error or interrupted:", e);
            isRunning = false;
        }
    }

    public ExecutorService getThreadPool() {
        return executorThreadPool;
    }

    public void shutdown() {
        executorThreadPool.shutdown();
        try {
            if (!executorThreadPool.awaitTermination(WAIT_FOR_TERMINATION, TimeUnit.MILLISECONDS)) {
                executorThreadPool.shutdownNow();
                isRunning = false;
            }
            LOG.info("shutdown success");
        } catch (InterruptedException e) {
            LOG.error("shutdown failed", e);
        }
    }

    public void callback(ExecutorThreadCallback<Boolean> callback) {
        this.callback = callback;
    }

    public void waitForWorkerCompletion() throws InterruptedException {
        while (isRunning()) {
            try {
                TimeUnit.MILLISECONDS.sleep(SLEEP_INTERVAL);
            } catch (Exception e) {
                throw new InterruptedException(e.getMessage());
            }
        }
    }

    public void queue(WorkerTask<?> workerTask) {
        internalEventsQueue.add(workerTask);
    }

    public int queued() {
        return internalEventsQueue.size();
    }

    public int countActiveThreads() {
        return executorThreadPool.getActiveCount();
    }

}
