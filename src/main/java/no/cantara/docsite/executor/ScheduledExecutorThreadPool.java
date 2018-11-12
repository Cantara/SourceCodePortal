package no.cantara.docsite.executor;

import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.health.HealthResource;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Deque;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1, runnable -> new Thread(new ThreadGroup("ScheduledThreadGroup"), runnable));
    }

    @Override
    public void queue(ScheduledWorker scheduledWorker) {
        scheduledWorkers.add(scheduledWorker);
    }

    @Override
    public void start() {
        LOG.info("Starting ScheduledExecutorService..");
        for (ScheduledWorker scheduledWorker : scheduledWorkers) {
            LOG.info("Scheduled worker task: {}", scheduledWorker.workerTaskList.stream().map(m -> m.getClass().getSimpleName()).collect(Collectors.toList()));
            ScheduledThread thread = new ScheduledThread(scheduledWorker);
            scheduledExecutorService.scheduleAtFixedRate(thread, scheduledWorker.initialDelay, scheduledWorker.period, scheduledWorker.timeUnit);
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

    @Override
    public Set<ScheduledWorker> getScheduledWorkers() {
        SortedSet<ScheduledWorker> sortedSet = new TreeSet<>(Comparator.comparing((k -> k.id)));
        sortedSet.addAll(scheduledWorkers);
        return sortedSet;
    }

    static class ScheduledThread implements Runnable {

        private final ScheduledWorker scheduledWorkers;

        public ScheduledThread(ScheduledWorker scheduledWorkers) {
            this.scheduledWorkers = scheduledWorkers;
        }

        @Override
        public void run() {
            LOG.info("Running scheduled worker: {}", scheduledWorkers.id);
            for (WorkerTask workerTask : scheduledWorkers.workerTaskList) {
                workerTask.executor().queue((workerTask));
            }
            HealthResource.instance().markScheduledWorkerLastSeen(scheduledWorkers.id);
        }
    }
}
