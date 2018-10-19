package no.cantara.docsite.domain.github.contents;

import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.WorkerTask;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class PreFetchDoneTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(PreFetchDoneTask.class);
    public PreFetchDoneTask(DynamicConfiguration configuration, ExecutorService executor) {
        super(configuration, executor);
    }

    @Override
    public void execute() {
        while(getExecutor().getThreadPool().getActiveCount() > 2) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        LOG.info("PreFetch is completed!");
    }
}
