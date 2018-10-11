package no.cantara.docsite.task;

import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.executor.ExecutorThreadPool;
import no.cantara.docsite.executor.WorkerTask;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchPageTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(FetchPageTask.class);

    public FetchPageTask(DynamicConfiguration configuration, ExecutorThreadPool executor) {
        super(configuration, executor);
    }

    @Override
    public void execute() {
        GetGitHubCommand<String> cmd = new GetGitHubCommand<>(getConfiguration(), this);
        String result = cmd.execute();
        LOG.trace("result: {}", result);
    }
}
