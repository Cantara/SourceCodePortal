package no.cantara.docsite.domain.github.contents;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.domain.links.GitHubApiContentsURL;
import no.cantara.docsite.domain.renderer.DocumentRenderer;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.WorkerTask;
import no.cantara.docsite.util.JsonbFactory;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * This task is used during push
 */
public class FetchGitHubContentsTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(FetchGitHubContentsTask.class);
    private final CacheStore cacheStore;
    private final CacheKey cacheKey;
    private final GitHubApiContentsURL contentsURL;
    private final String relativeFilePath;
    private final String commitId;

    public FetchGitHubContentsTask(DynamicConfiguration configuration, ExecutorService executor, CacheStore cacheStore, CacheKey cacheKey, GitHubApiContentsURL contentsURL, String relativeFilePath, String commitId) {
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
                contentsURL.getExternalGroupURL(relativeFilePath, commitId), HttpResponse.BodyHandlers.ofString());
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
