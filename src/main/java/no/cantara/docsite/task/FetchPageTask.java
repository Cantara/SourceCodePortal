package no.cantara.docsite.task;

import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.executor.ExecutorThreadPool;
import no.cantara.docsite.executor.WorkerTask;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.util.Optional;

public class FetchPageTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(FetchPageTask.class);

    public FetchPageTask(DynamicConfiguration configuration, ExecutorThreadPool executor) {
        super(configuration, executor);
    }

    @Override
    public void execute() {
        GetGitHubCommand<String> cmd = new GetGitHubCommand<>("githubRateLimit", getConfiguration(), Optional.of(this), "/rate_limit", HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = cmd.execute();
        if (GetGitHubCommand.anyOf(response, 200)) {
            LOG.trace("result: {}", response.body());
        } else {
            LOG.error("Received empty payload ({})", response.statusCode());
        }
    }
}
