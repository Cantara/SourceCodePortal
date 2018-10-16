package no.cantara.docsite.domain.github.commits;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheShaKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.executor.ExecutorThreadPool;
import no.cantara.docsite.executor.WorkerTask;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.JsonbBuilder;
import java.net.http.HttpResponse;
import java.util.Optional;

public class FetchCommitRevisionTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(FetchCommitRevisionTask.class);
    private final CacheStore cacheStore;
    private final CacheKey cacheKey;

    public FetchCommitRevisionTask(DynamicConfiguration configuration, ExecutorThreadPool executor, CacheStore cacheStore, CacheKey cacheKey) {
        super(configuration, executor);
        this.cacheStore = cacheStore;
        this.cacheKey = cacheKey;
    }

    @Override
    public void execute() {
        String url = String.format("https://api.github.com/repos/%s/%s/commits", cacheKey.organization, cacheKey.repoName);
        GetGitHubCommand<String> cmd = new GetGitHubCommand<>("githubPage", getConfiguration(), Optional.of(this), url, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = cmd.execute();
        if (GetGitHubCommand.anyOf(response, 200)) {
            CommitRevision[] commitRevision = JsonbBuilder.create().fromJson(response.body(), CommitRevision[].class);
            for (int n = 0; n < commitRevision.length; n++) {
                CacheShaKey cacheShaKey = CacheShaKey.of(cacheKey, commitRevision[n].sha);
                cacheStore.getCommits().put(cacheShaKey, commitRevision[n]);
            }
        } else {
            LOG.warn("Resource not found: {} ({})", response.uri(), response.statusCode());
        }
    }
}
