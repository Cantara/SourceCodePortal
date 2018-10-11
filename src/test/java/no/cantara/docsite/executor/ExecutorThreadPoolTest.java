package no.cantara.docsite.executor;

import no.cantara.docsite.task.FetchPageTask;
import no.cantara.docsite.test.server.TestServer;
import no.cantara.docsite.test.server.TestServerListener;
import no.ssb.config.DynamicConfiguration;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Inject;

@Listeners(TestServerListener.class)
public class ExecutorThreadPoolTest {

    static class DummyTask extends WorkerTask {
        public DummyTask(DynamicConfiguration configuration, ExecutorThreadPool executor) {
            super(configuration, executor);
        }

        @Override
        public void execute() {
            System.out.println("I am a dummy task -- "  + Thread.currentThread().getName());
        }
    }

    @Inject
    TestServer server;

    @Test
    public void testName() throws Exception {
        ExecutorThreadPool executorThreadPool = new ExecutorThreadPool();
        executorThreadPool.start();
        executorThreadPool.queue(new DummyTask(server.getConfiguration(), executorThreadPool));
        executorThreadPool.queue(new DummyTask(server.getConfiguration(), executorThreadPool));
        executorThreadPool.queue(new DummyTask(server.getConfiguration(), executorThreadPool));
        executorThreadPool.queue(new FetchPageTask(server.getConfiguration(), executorThreadPool));
        Thread.sleep(5000);
        executorThreadPool.shutdown();
        executorThreadPool.waitForWorkerCompletion();
    }
}
