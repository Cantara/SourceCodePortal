package no.cantara.docsite.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorThreadPool implements ExecutorService {

    private static Logger LOG = LoggerFactory.getLogger(ExecutorService.class);
    private final BlockingQueue internalEventsQueue;
    private final ThreadPoolExecutor executorThreadPool;

    ExecutorThreadPool() {
        this.internalEventsQueue = new ArrayBlockingQueue(BLOCKING_QUEUE_SIZE);
        this.executorThreadPool = new ThreadPoolExecutor(
                8, // core size
                50, // max size
                10 * 60, // idle timeout
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(BLOCKING_QUEUE_SIZE)); // queue with a size;
    }

    @Override
    public void start() {
        try {
//            executorThreadPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
//                @Override
//                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
//                    LOG.trace("Rejected: {}", r.getClass().getName());
//                }
//            });
            executorThreadPool.execute(() -> {
                LOG.info("Starting ExecutorService..");
                while (!executorThreadPool.isTerminated()) {
                    try {
                        WorkerTask workerTask = (WorkerTask) internalEventsQueue.take();
                        int retryCount = workerTask.incrementCount();
                        if (retryCount < MAX_RETRIES) {
                            if (retryCount > 0) LOG.warn("RetryCount: {} for {}", retryCount, workerTask.getClass().getName());
                            executorThreadPool.execute(new Worker(workerTask));
                        }

                    } catch (InterruptedException e) {
                        LOG.trace("Exiting thread: {}", Thread.currentThread());
                    } catch (Exception e) {
                        LOG.error("Error or interrupted:", e);
                    }
                }
                LOG.trace("Exiting thread: {}", Thread.currentThread());
            });
            TimeUnit.MILLISECONDS.sleep(WAIT_FOR_THREAD_POOL);

        } catch (InterruptedException e) {
            LOG.error("Error or interrupted:", e);
        }
    }

    @Override
    public void shutdown() {
        executorThreadPool.shutdown();
        try {
            if (!executorThreadPool.awaitTermination(WAIT_FOR_TERMINATION, TimeUnit.MILLISECONDS)) {
                executorThreadPool.shutdownNow();
            }
            LOG.info("ExecutorService shutdown success");
        } catch (InterruptedException e) {
            LOG.error("ExecutorService shutdown failed", e);
        }
    }

    @Override
    public void waitForWorkerCompletion() throws InterruptedException {
        LOG.trace("thradCOunt: {}", countActiveThreads());
        while (countActiveThreads() > 1) {
            try {
                TimeUnit.MILLISECONDS.sleep(SLEEP_INTERVAL);
            } catch (Exception e) {
                throw new InterruptedException(e.getMessage());
            }
        }
    }

    @Override
    public void queue(WorkerTask workerTask) {
        internalEventsQueue.add(workerTask);
    }

    @Override
    public ThreadPoolExecutor getThreadPool() {
        return executorThreadPool;
    }

    @Override
    public int queued() {
        return internalEventsQueue.size();
    }

    @Override
    public int countActiveThreads() {
        return executorThreadPool.getActiveCount();
    }

}
