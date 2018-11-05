package no.cantara.docsite.prefetch;

import no.cantara.docsite.cache.CacheCantaraWikiKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.RepositoryConfigLoader;
import no.cantara.docsite.domain.confluence.cantara.FetchCantaraWikiTask;
import no.cantara.docsite.domain.github.commits.FetchGitHubCommitRevisionsTask;
import no.cantara.docsite.domain.github.contents.FetchGitHubReadmeTask;
import no.cantara.docsite.domain.links.GitHubApiContentsURL;
import no.cantara.docsite.domain.links.GitHubApiReadmeURL;
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

    public void fetch() {
        LOG.info("Pre-fetch data..");
        cacheStore.getCacheKeys().iterator().forEachRemaining(entry -> {
            executorService.queue(new FetchMavenPOMTask(configuration, executorService, cacheStore, entry.getKey(), new GitHubApiContentsURL(entry.getKey())));
            executorService.queue(new FetchGitHubReadmeTask(configuration, executorService, cacheStore, entry.getKey(), new GitHubApiReadmeURL(entry.getKey())));
            executorService.queue(new FetchGitHubCommitRevisionsTask(configuration, executorService, cacheStore, entry.getKey()));

        });
        LOG.info("Pre-fetch done queuing tasks - remaining: {}", executorService.countRemainingWorkerTasks());
        executorService.queue(new FetchCantaraWikiTask(configuration, executorService, cacheStore, CacheCantaraWikiKey.of("xmas-beer", "46137421")));
        executorService.queue(new FetchCantaraWikiTask(configuration, executorService, cacheStore, CacheCantaraWikiKey.of("about", "16515095")));
        executorService.queue(new PreFetchDoneTask(configuration, executorService));
    }
}
