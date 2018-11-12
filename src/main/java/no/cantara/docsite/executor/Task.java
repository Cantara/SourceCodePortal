package no.cantara.docsite.executor;

public interface Task {

    /**
     * Executes a task
     *
     * @return true is success and otherwise failure
     */
    boolean execute();

}
