package no.cantara.docsite.task;

import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.domain.config.RepositoryConfigLoader;
import no.cantara.docsite.executor.ExecutorThreadPool;
import no.cantara.docsite.executor.WorkerTask;
import no.cantara.docsite.model.github.push.CommitRevision;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.JsonbBuilder;
import java.net.http.HttpResponse;
import java.util.Optional;

public class FetchCommitRevisionTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(FetchCommitRevisionTask.class);
    private final CacheStore cacheStore;
    private final String organization;
    private final RepositoryConfigLoader.Repository repository;

    public FetchCommitRevisionTask(DynamicConfiguration configuration, ExecutorThreadPool executor, CacheStore cacheStore, String organization, RepositoryConfigLoader.Repository repository) {
        super(configuration, executor);

        this.cacheStore = cacheStore;
        this.organization = organization;
        this.repository = repository;
    }

    @Override
    public void execute() {
        String url = String.format("https://api.github.com/repos/%s/%s/commits?client_id=%s&client_secret=%s",
                organization,
                repository.repoName,
                getConfiguration().evaluateToString("github.oauth2.client.clientId"),
                getConfiguration().evaluateToString("github.oauth2.client.clientSecret")
        );
        LOG.trace("URL: {}", url);
        GetGitHubCommand<String> cmd = new GetGitHubCommand<>("githubPage", getConfiguration(), Optional.of(this), url, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = cmd.execute();
        if (GetGitHubCommand.anyOf(response, 200)) {
            LOG.trace("COMMIT_REV: {}", response.body());
            CommitRevision[] commitRevision = JsonbBuilder.create().fromJson(response.body(), CommitRevision[].class);
            for(int n=0; n<commitRevision.length; n++) {
                cacheStore.getCommits().put(repository.repoName, commitRevision[n]);
            }
        } else {
            LOG.error("Received empty payload (http:{} - {})", response.statusCode(), response.body());
        }
    }
}
