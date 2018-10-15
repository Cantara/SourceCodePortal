package no.cantara.docsite.task;

import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.domain.config.RepositoryConfigLoader;
import no.cantara.docsite.executor.ExecutorThreadPool;
import no.cantara.docsite.executor.WorkerTask;
import no.cantara.docsite.model.github.pull.RepositoryContents;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.JsonbBuilder;
import java.net.http.HttpResponse;
import java.util.Optional;

public class FetchPageTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(FetchPageTask.class);
    private final CacheStore cacheStore;
    private final RepositoryConfigLoader.Repository repository;

    public FetchPageTask(DynamicConfiguration configuration, ExecutorThreadPool executor, CacheStore cacheStore, RepositoryConfigLoader.Repository repository) {
        super(configuration, executor);
        this.cacheStore = cacheStore;
        this.repository = repository;
    }

    @Override
    public void execute() {
        GetGitHubCommand<String> cmd = new GetGitHubCommand<>("githubPage", getConfiguration(), Optional.of(this), repository.readmeURL, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = cmd.execute();
        if (GetGitHubCommand.anyOf(response, 200)) {
            RepositoryContents readmeContents = JsonbBuilder.create().fromJson(response.body(), RepositoryContents.class);
            cacheStore.getPages().put(repository.repoName, readmeContents);
        } else {
            LOG.error("Resource not found: {} ({})", response.uri(), response.statusCode());
        }
    }
}
