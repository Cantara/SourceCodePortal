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
    private final Deque<ScheduledWorker> scheduledWorkers;

    ScheduledExecutorThreadPool(DynamicConfiguration configuration, ExecutorService executorService, CacheStore cacheStore) {
        this.configuration = configuration;
        this.executorService = executorService;
        this.cacheStore = cacheStore;
        this.scheduledWorkers = new ConcurrentLinkedDeque<>();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void queue(ScheduledWorker scheduledWorker) {
        scheduledWorkers.add(scheduledWorker);
    }

    @Override
    public void start() {
        LOG.info("Starting ScheduledExecutorService..");
        for (ScheduledWorker scheduledWorker : scheduledWorkers) {
            scheduledExecutorService.scheduleAtFixedRate(new ScheduledThread(configuration, executorService, cacheStore, scheduledWorker), scheduledWorker.initialDelay, scheduledWorker.period, scheduledWorker.timeUnit);
        }
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
        private final ScheduledWorker scheduledWorkers;

        public ScheduledThread(DynamicConfiguration configuration, ExecutorService executorService, CacheStore cacheStore, ScheduledWorker scheduledWorkers) {
            this.configuration = configuration;
            this.executorService = executorService;
            this.cacheStore = cacheStore;
            this.scheduledWorkers = scheduledWorkers;
        }

        @Override
        public void run() {
            for (WorkerTask workerTask : scheduledWorkers.workerTaskList) {
                workerTask.getExecutor().queue(workerTask);
            }
        }
    }
}
