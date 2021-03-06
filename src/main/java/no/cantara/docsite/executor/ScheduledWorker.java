package no.cantara.docsite.executor;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public class ScheduledWorker {

    final Deque<WorkerTask> workerTaskList = new ConcurrentLinkedDeque<>();
    public final String id;
    public final long initialDelay;
    public final long period;
    public final TimeUnit timeUnit;

    public ScheduledWorker(String id, long initialDelay, long period, TimeUnit timeUnit) {
        this.id = id;
        this.initialDelay = initialDelay;
        this.period = period;
        this.timeUnit = timeUnit;
    }

    public void queue(WorkerTask workerTask) {
        workerTaskList.add(workerTask);
    }

    public int getTaskCount() {
        return workerTaskList.size();
    }

}
