package no.cantara.docsite.fetch;

import no.cantara.docsite.cache.CacheCantaraWikiKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.confluence.cantara.FetchCantaraWikiTask;
import no.cantara.docsite.domain.jenkins.QueueJenkinsStatusTask;
import no.cantara.docsite.domain.shields.QueueShieldsStatusTask;
import no.cantara.docsite.domain.snyk.QueueSnykTestTask;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.ScheduledExecutorService;
import no.cantara.docsite.executor.ScheduledWorker;
import no.ssb.config.DynamicConfiguration;

import java.util.concurrent.TimeUnit;

public class ScheduledFetchData {

    private final DynamicConfiguration configuration;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final CacheStore cacheStore;

    public ScheduledFetchData(DynamicConfiguration configuration, ExecutorService executorService, ScheduledExecutorService scheduledExecutorService, CacheStore cacheStore) {
        this.configuration = configuration;
        this.executorService = executorService;
        this.scheduledExecutorService = scheduledExecutorService;
        this.cacheStore = cacheStore;
    }

    public void run() {
        ScheduledWorker confluenceScheduledWorker = new ScheduledWorker("cantara-wiki", 0, configuration.evaluateToInt("scheduled.tasks.interval"), TimeUnit.SECONDS);
        confluenceScheduledWorker.queue(new FetchCantaraWikiTask(configuration, executorService, cacheStore, CacheCantaraWikiKey.of("xmas-beer", "46137493")));
        confluenceScheduledWorker.queue(new FetchCantaraWikiTask(configuration, executorService, cacheStore, CacheCantaraWikiKey.of("about", "16515095")));
        scheduledExecutorService.queue(confluenceScheduledWorker);

        ScheduledWorker jenkinsScheduledWorker = new ScheduledWorker("jenkins", 0, configuration.evaluateToInt("scheduled.check.jenkins.build.status.interval"), TimeUnit.MINUTES);
        jenkinsScheduledWorker.queue(new QueueJenkinsStatusTask(configuration, executorService, cacheStore));
        scheduledExecutorService.queue(jenkinsScheduledWorker);

        ScheduledWorker snykTestScheduledWorker = new ScheduledWorker("snyk", 0, configuration.evaluateToInt("scheduled.check.snyk.test.status.interval"), TimeUnit.MINUTES);
        snykTestScheduledWorker.queue(new QueueSnykTestTask(configuration, executorService, cacheStore));
        scheduledExecutorService.queue(snykTestScheduledWorker);

        ScheduledWorker shieldsScheduledWorker = new ScheduledWorker("shields", 0, configuration.evaluateToInt("scheduled.check.shields.status.interval"), TimeUnit.MINUTES);
        shieldsScheduledWorker.queue(new QueueShieldsStatusTask(configuration, executorService, cacheStore));
        scheduledExecutorService.queue(shieldsScheduledWorker);
    }
}
