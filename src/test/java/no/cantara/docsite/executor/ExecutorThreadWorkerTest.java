package no.cantara.docsite.executor;

import org.testng.annotations.Test;

public class ExecutorThreadWorkerTest {

    @Test
    public void testName() throws Exception {
        ExecutorThreadWorker executorThreadWorker = new ExecutorThreadWorker();
        executorThreadWorker.callback(new ExecutorThreadCallback<Boolean>() {
            @Override
            public Boolean call(WorkerTask<?> event) throws Exception {
                return false;
            }
        });
        executorThreadWorker.queue(new WorkerTask<>(null, null));
        executorThreadWorker.start();
        Thread.sleep(250);
        executorThreadWorker.shutdown();
        executorThreadWorker.waitForWorkerCompletion();

    }
}
