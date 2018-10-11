package no.cantara.docsite.executor;

import org.testng.annotations.Test;

public class ExecutorThreadWorkerTest {

    static class DummyTask implements Runnable {
        @Override
        public void run() {
            System.out.println("I am a dummy task -- "  + Thread.currentThread().getName());
        }
    }

    @Test
    public void testName() throws Exception {
        ExecutorThreadWorker executorThreadWorker = new ExecutorThreadWorker();
        executorThreadWorker.queue(new DummyTask());
        executorThreadWorker.queue(new DummyTask());
        executorThreadWorker.queue(new DummyTask());
        executorThreadWorker.start();
        Thread.sleep(250);
        executorThreadWorker.shutdown();
        executorThreadWorker.waitForWorkerCompletion();

    }
}
