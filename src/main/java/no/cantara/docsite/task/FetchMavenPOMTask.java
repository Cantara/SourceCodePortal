package no.cantara.docsite.task;

import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.domain.config.RepositoryConfigLoader;
import no.cantara.docsite.domain.maven.MavenPOMParser;
import no.cantara.docsite.executor.ExecutorThreadPool;
import no.cantara.docsite.executor.WorkerTask;
import no.cantara.docsite.model.github.pull.RepositoryContents;
import no.cantara.docsite.model.maven.MavenPOM;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import java.net.http.HttpResponse;
import java.util.Optional;

public class FetchMavenPOMTask extends WorkerTask  {

    private static final Logger LOG = LoggerFactory.getLogger(FetchPageTask.class);
    private final CacheStore cacheStore;
    private final RepositoryConfigLoader.Repository repository;

    public FetchMavenPOMTask(DynamicConfiguration configuration, ExecutorThreadPool executor, CacheStore cacheStore, RepositoryConfigLoader.Repository repository) {
        super(configuration, executor);
        this.cacheStore = cacheStore;
        this.repository = repository;
    }

    @Override
    public void execute() {
        LOG.trace("----> {}", String.format(repository.contentsURL, "pom.xml"));
        GetGitHubCommand<String> cmd = new GetGitHubCommand<>("githubPage", getConfiguration(), Optional.of(this), String.format(repository.contentsURL, "pom.xml"), HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = cmd.execute();
        if (GetGitHubCommand.anyOf(response, 200)) {
            JsonbConfig config = new JsonbConfig();
            RepositoryContents mavenPOMContents = JsonbBuilder.create(config).fromJson(response.body(), RepositoryContents.class);
            MavenPOMParser parser = new MavenPOMParser();
            MavenPOM mavenPOM = parser.parse(mavenPOMContents.getDecodedContent());
            cacheStore.getProjects().put(repository.repoName, mavenPOM);
        } else {
            LOG.error("Resource not found: {} ({})", response.uri(), response.statusCode());
        }
    }

}
