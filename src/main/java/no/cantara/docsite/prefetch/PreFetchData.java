package no.cantara.docsite.prefetch;

import no.cantara.docsite.cache.CacheCantaraWikiKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.RepositoryConfigLoader;
import no.cantara.docsite.domain.confluence.cantara.FetchCantaraWikiTask;
import no.cantara.docsite.domain.github.commits.FetchGitHubCommitRevisionsTask;
import no.cantara.docsite.domain.github.contents.FetchGitHubReadmeTask;
import no.cantara.docsite.domain.maven.FetchMavenPOMTask;
import no.cantara.docsite.executor.ExecutorService;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreFetchData {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryConfigLoader.class);
    private final DynamicConfiguration configuration;
    private final ExecutorService executorService;
    private final CacheStore cacheStore;

    public PreFetchData(DynamicConfiguration configuration, ExecutorService executorService, CacheStore cacheStore) {
        this.configuration = configuration;
        this.executorService = executorService;
        this.cacheStore = cacheStore;
    }

    // TODO repositories should be maintained in a repo only and not bound to a group.
    public void fetch() {
        LOG.info("Pre-fetch data..");
        cacheStore.getRepositories().forEach(rg -> {
            //LOG.trace("PreFetch: {} - {}", rg.getKey().groupId, rg.getKey().repoName);
            executorService.queue(new FetchMavenPOMTask(configuration, executorService, cacheStore, rg.getKey().asCacheKey(), rg.getValue().apiContentsURL));
            executorService.queue(new FetchGitHubReadmeTask(configuration, executorService, cacheStore, rg.getKey().asCacheKey(), rg.getValue().apiReadmeURL));
            executorService.queue(new FetchGitHubCommitRevisionsTask(configuration, executorService, cacheStore, rg.getKey().asCacheKey()));
        });
        executorService.queue(new FetchCantaraWikiTask(configuration, executorService, cacheStore, CacheCantaraWikiKey.of("xmas-beer", "46137421")));
        executorService.queue(new FetchCantaraWikiTask(configuration, executorService, cacheStore, CacheCantaraWikiKey.of("about", "16515095")));
        executorService.queue(new PreFetchDoneTask(configuration, executorService));
    }

}
