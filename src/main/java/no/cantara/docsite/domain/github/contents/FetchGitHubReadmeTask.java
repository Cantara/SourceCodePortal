package no.cantara.docsite.domain.github.contents;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.domain.links.GitHubApiReadmeURL;
import no.cantara.docsite.domain.renderer.DocumentRenderer;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.WorkerTask;
import no.cantara.docsite.json.JsonbFactory;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * This task is used during pull
 */
public class FetchGitHubReadmeTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(FetchGitHubReadmeTask.class);
    private final CacheStore cacheStore;
    private final CacheKey cacheKey;
    private final GitHubApiReadmeURL repoReadmeURL;

    public FetchGitHubReadmeTask(DynamicConfiguration configuration, ExecutorService executor, CacheStore cacheStore, CacheKey cacheKey, GitHubApiReadmeURL repoReadmeURL) {
        super(configuration, executor);
        this.cacheStore = cacheStore;
        this.cacheKey = cacheKey;
        this.repoReadmeURL = repoReadmeURL;
    }

    @Override
    public void execute() {
        GetGitHubCommand<String> cmd = new GetGitHubCommand<>("githubPage", configuration(), Optional.of(this), repoReadmeURL.getExternalURL(), HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = cmd.execute();
        if (GetGitHubCommand.anyOf(response, 200)) {
            GitHubRepositoryContents readmeContents = JsonbFactory.instance().fromJson(response.body(), GitHubRepositoryContents.class);
            readmeContents.renderedHtml = DocumentRenderer.render(readmeContents.name, readmeContents.content);
            cacheStore.getReadmeContents().put(cacheKey, readmeContents.asRepositoryContents(cacheKey));
        } else {
            LOG.warn("Resource not found: {} ({})", response.uri(), response.statusCode());
        }
    }
}
