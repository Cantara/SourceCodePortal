package no.cantara.docsite.domain.github.pages;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.domain.github.contents.RepositoryContents;
import no.cantara.docsite.executor.ExecutorThreadPool;
import no.cantara.docsite.executor.WorkerTask;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.JsonbBuilder;
import java.net.http.HttpResponse;
import java.util.Optional;

public class FetchPageTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(FetchPageTask.class);
    private final CacheStore cacheStore;
    private final CacheKey cacheKey;
    private final String repoReadmeURL;

    public FetchPageTask(DynamicConfiguration configuration, ExecutorThreadPool executor, CacheStore cacheStore, CacheKey cacheKey, String repoReadmeURL) {
        super(configuration, executor);
        this.cacheStore = cacheStore;
        this.cacheKey = cacheKey;
        this.repoReadmeURL = repoReadmeURL;
    }

    @Override
    public void execute() {
        GetGitHubCommand<String> cmd = new GetGitHubCommand<>("githubPage", getConfiguration(), Optional.of(this), repoReadmeURL, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = cmd.execute();
        if (GetGitHubCommand.anyOf(response, 200)) {
            RepositoryContents readmeContents = JsonbBuilder.create().fromJson(response.body(), RepositoryContents.class);
            cacheStore.getPages().put(cacheKey, readmeContents);
        } else {
            LOG.warn("Resource not found: {} ({})", response.uri(), response.statusCode());
        }
    }
}
