package no.cantara.docsite.domain.github.contents;

import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.RepositoryConfigLoader;
import no.cantara.docsite.domain.github.commits.FetchCommitRevisionsTask;
import no.cantara.docsite.domain.github.pages.FetchPageTask;
import no.cantara.docsite.domain.maven.FetchMavenPOMTask;
import no.cantara.docsite.executor.ExecutorService;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreFetchRepositoryContents {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryConfigLoader.class);
    private final DynamicConfiguration configuration;
    private final ExecutorService executorService;
    private final CacheStore cacheStore;

    public PreFetchRepositoryContents(DynamicConfiguration configuration, ExecutorService executorService, CacheStore cacheStore) {
        this.configuration = configuration;
        this.executorService = executorService;
        this.cacheStore = cacheStore;
    }

    public void fetch() {
        LOG.info("Pre-fetch data..");
        cacheStore.getRepositoryGroups().forEach(rg -> {
            executorService.queue(new FetchMavenPOMTask(configuration, executorService, cacheStore, rg.getKey().asCacheKey(), rg.getValue().contentsURL));
            executorService.queue(new FetchPageTask(configuration, executorService, cacheStore, rg.getKey().asCacheKey(), rg.getValue().readmeURL));
            executorService.queue(new FetchCommitRevisionsTask(configuration, executorService, cacheStore, rg.getKey().asCacheKey()));
        });
        executorService.queue(new PreFetchDoneTask(configuration, executorService));
    }

}
