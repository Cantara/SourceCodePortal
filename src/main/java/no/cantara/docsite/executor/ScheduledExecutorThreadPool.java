package no.cantara.docsite.executor;

import no.cantara.docsite.cache.CacheStore;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutorThreadPool implements ScheduledExecutorService {

    private static Logger LOG = LoggerFactory.getLogger(ScheduledExecutorService.class);

    private final java.util.concurrent.ScheduledExecutorService scheduledExecutorService;
    private final DynamicConfiguration configuration;
    private final ExecutorService executorService;
    private final CacheStore cacheStore;
    private final Deque<WorkerTask> workerTasks;

    ScheduledExecutorThreadPool(DynamicConfiguration configuration, ExecutorService executorService, CacheStore cacheStore) {
        this.configuration = configuration;
        this.executorService = executorService;
        this.cacheStore = cacheStore;
        this.workerTasks = new ConcurrentLinkedDeque<>();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void queue(WorkerTask workerTask) {
        workerTasks.add(workerTask);
    }

    @Override
    public void start() {
        LOG.info("Starting ScheduledExecutorService..");
        scheduledExecutorService.scheduleAtFixedRate(new ScheduledThread(configuration, executorService, cacheStore, workerTasks), 0, configuration.evaluateToInt("scheduled.tasks.interval"), TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
        scheduledExecutorService.shutdown();
        try {
            if (!scheduledExecutorService.awaitTermination(WAIT_FOR_TERMINATION, TimeUnit.MILLISECONDS)) {
                scheduledExecutorService.shutdownNow();
            }
            LOG.info("ScheduledExecutorService shutdown success");
        } catch (InterruptedException e) {
            LOG.error("ScheduledExecutorService shutdown failed", e);
        }
    }

    @Override
    public java.util.concurrent.ScheduledExecutorService getThreadPool() {
        return scheduledExecutorService;
    }

    static class ScheduledThread implements Runnable {

        private final DynamicConfiguration configuration;
        private final ExecutorService executorService;
        private final CacheStore cacheStore;
        private final Deque<WorkerTask> workerTasks;

        public ScheduledThread(DynamicConfiguration configuration, ExecutorService executorService, CacheStore cacheStore, Deque<WorkerTask> workerTasks) {
            this.configuration = configuration;
            this.executorService = executorService;
            this.cacheStore = cacheStore;
            this.workerTasks = workerTasks;
        }

        @Override
        public void run() {
            workerTasks.forEach(workerTask -> workerTask.getExecutor().queue(workerTask));
        }
    }
}
