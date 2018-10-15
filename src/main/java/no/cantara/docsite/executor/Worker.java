package no.cantara.docsite.executor;

public class Worker implements Runnable {

    private final Task task;

    public Worker(Task task) {
        this.task = task;
    }

    @Override
    public void run() {
        try {
            task.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
