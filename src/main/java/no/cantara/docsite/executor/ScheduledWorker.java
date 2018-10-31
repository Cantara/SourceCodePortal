package no.cantara.docsite.executor;

import com.sun.istack.NotNull;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public class ScheduledWorker {

    final Deque<WorkerTask> workerTaskList = new ConcurrentLinkedDeque<>();
    public final long initialDelay;
    public final long period;
    public final TimeUnit timeUnit;

    public ScheduledWorker(long initialDelay, long period, @NotNull TimeUnit timeUnit) {
        this.initialDelay = initialDelay;
        this.period = period;
        this.timeUnit = timeUnit;
    }

    public void queue(WorkerTask workerTask) {
        workerTaskList.add(workerTask);
    }

}
