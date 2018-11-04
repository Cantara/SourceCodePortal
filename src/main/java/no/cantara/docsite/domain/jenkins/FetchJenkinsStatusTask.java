package no.cantara.docsite.domain.jenkins;

import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetCommand;
import no.cantara.docsite.domain.links.JenkinsURL;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.WorkerTask;
import no.cantara.docsite.json.JsonbFactory;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.util.Optional;

import static java.net.HttpURLConnection.HTTP_OK;

public class FetchJenkinsStatusTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(FetchJenkinsStatusTask.class);

    private final CacheStore cacheStore;
    private final ScmRepository repository;

    public FetchJenkinsStatusTask(DynamicConfiguration configuration, ExecutorService executor, CacheStore cacheStore, ScmRepository repository) {
        super(configuration, executor);
        this.cacheStore = cacheStore;
        this.repository = repository;
    }

    @Override
    public void execute() {
        JenkinsURL jenkinsURL = new JenkinsURL(getConfiguration(), repository);
        GetCommand<String> cmd = new GetCommand<>("jenkinsStatus", getConfiguration(), Optional.of(this),
                jenkinsURL.getExternalURL(), HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = cmd.execute();
        if (response.statusCode() == HTTP_OK) {
            // TODO implement jenkins cache
        } else {
            LOG.trace("{} -- {} -- {}", jenkinsURL.getExternalURL(), response.statusCode(), response.body());
        }

    }

    @Override
    public String toString() {
        return String.format("%s: %s", getClass().getSimpleName(), JsonbFactory.asCompactString(JsonbFactory.asJsonObject(repository.cacheRepositoryKey)));
    }

}
