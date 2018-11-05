package no.cantara.docsite.executor;

import java.util.concurrent.ThreadPoolExecutor;

public interface ExecutorService {

    int MAX_RETRIES = 3;
    int BLOCKING_QUEUE_SIZE = 5000;
    long WAIT_FOR_THREAD_POOL = 50;
    long WAIT_FOR_TERMINATION = 100;
    long SLEEP_INTERVAL = 100;

    void start();

    void shutdown();

    void waitForWorkerCompletion() throws InterruptedException;

    ThreadPoolExecutor getThreadPool();

    void queue(WorkerTask workerTask);

    int queued();

    int countActiveThreads();

    int countRemainingWorkerTasks();

    static ExecutorService create() {
        return new ExecutorThreadPool();
    }
}
