package no.cantara.docsite.domain.maven;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.domain.github.contents.RepositoryContents;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.WorkerTask;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import java.net.http.HttpResponse;
import java.util.Optional;

public class FetchMavenPOMTask extends WorkerTask  {

    private static final Logger LOG = LoggerFactory.getLogger(FetchMavenPOMTask.class);
    private final CacheStore cacheStore;
    private final CacheKey cacheKey;
    private final String repoContentsURL;

    public FetchMavenPOMTask(DynamicConfiguration configuration, ExecutorService executor, CacheStore cacheStore, CacheKey cacheKey, String repoContentsURL) {
        super(configuration, executor);
        this.cacheStore = cacheStore;
        this.cacheKey = cacheKey;
        this.repoContentsURL = repoContentsURL;
    }

    @Override
    public void execute() {
        GetGitHubCommand<String> cmd = new GetGitHubCommand<>("githubPage", getConfiguration(), Optional.of(this), String.format(repoContentsURL, "pom.xml", cacheKey.branch), HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = cmd.execute();
        if (GetGitHubCommand.anyOf(response, 200)) {
            JsonbConfig config = new JsonbConfig();
            RepositoryContents mavenPOMContents = JsonbBuilder.create(config).fromJson(response.body(), RepositoryContents.class);
            MavenPOMParser parser = new MavenPOMParser();
            MavenPOM mavenPOM = parser.parse(mavenPOMContents.content);
            cacheStore.getProjects().put(cacheKey, mavenPOM);
        } else {
            LOG.warn("Resource not found: {} ({})", response.uri(), response.statusCode());
        }
    }

}
