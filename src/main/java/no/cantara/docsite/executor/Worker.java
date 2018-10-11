package no.cantara.docsite.executor;

public class Worker implements Runnable {

    private final Task task;

    public Worker(Task task) {
        this.task = task;
    }

    @Override
    public void run() {
        task.execute();
    }

}
