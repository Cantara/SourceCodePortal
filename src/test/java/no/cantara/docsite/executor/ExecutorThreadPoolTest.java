package no.cantara.docsite.executor;

import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.testng.annotations.Test;

public class ExecutorThreadPoolTest {

    static DynamicConfiguration configuration() {
        DynamicConfiguration configuration = new StoreBasedDynamicConfiguration.Builder()
                .propertiesResource("security.properties")
                .build();
        return configuration;
    }

    static class DummyTask extends WorkerTask {
        public DummyTask(DynamicConfiguration configuration, ExecutorThreadPool executor) {
            super(configuration, executor);
        }

        @Override
        public void execute() {
            System.out.println("I am a dummy task -- "  + Thread.currentThread().getName());
        }
    }

    @Test
    public void testName() throws Exception {
        DynamicConfiguration configuration = configuration();
        ExecutorThreadPool executorThreadPool = new ExecutorThreadPool();
        executorThreadPool.start();
        executorThreadPool.queue(new DummyTask(configuration, executorThreadPool));
        executorThreadPool.queue(new DummyTask(configuration, executorThreadPool));
        executorThreadPool.queue(new DummyTask(configuration, executorThreadPool));
//        executorThreadPool.queue(new FetchPageTask(configuration, executorThreadPool, "https://api.github.com/repos/Cantara/SourceCodePortal/readme?ref=master"));
        Thread.sleep(5000);
        executorThreadPool.shutdown();
        executorThreadPool.waitForWorkerCompletion();
    }
}
