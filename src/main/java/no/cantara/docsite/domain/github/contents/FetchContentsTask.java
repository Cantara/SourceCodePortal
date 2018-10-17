package no.cantara.docsite.domain.github.contents;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.WorkerTask;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.JsonbBuilder;
import java.net.http.HttpResponse;
import java.util.Optional;

public class FetchContentsTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(FetchContentsTask.class);
    private final CacheStore cacheStore;
    private final CacheKey cacheKey;
    private final String contentsURL;
    private final String relativeFilePath;
    private final String commitId;

    public FetchContentsTask(DynamicConfiguration configuration, ExecutorService executor, CacheStore cacheStore, CacheKey cacheKey, String contentsURL, String relativeFilePath, String commitId) {
        super(configuration, executor);
        this.cacheStore = cacheStore;
        this.cacheKey = cacheKey;
        this.contentsURL = contentsURL;
        this.relativeFilePath = relativeFilePath;
        this.commitId = commitId;
    }

    @Override
    public void execute() {
        GetGitHubCommand<String> cmd = new GetGitHubCommand<>("githubPage", getConfiguration(), Optional.of(this),
                String.format(contentsURL, relativeFilePath, commitId), HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = cmd.execute();
        if (GetGitHubCommand.anyOf(response, 200)) {
            RepositoryContents readmeContents = JsonbBuilder.create().fromJson(response.body(), RepositoryContents.class);
            cacheStore.getPages().put(cacheKey, readmeContents);
        } else {
            LOG.warn("Resource not found: {} ({})", response.uri(), response.statusCode());
        }
    }
}
