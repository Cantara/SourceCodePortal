package no.cantara.docsite.cache;

import no.cantara.docsite.domain.config.RepositoryConfig;
import no.cantara.docsite.domain.config.RepositoryConfigService;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.cantara.docsite.test.TestData;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.util.List;

public class CachePoolTest {

    private static final Logger LOG = LoggerFactory.getLogger(CachePoolTest.class);

    private DynamicConfiguration configuration;
    private CacheManager cacheManager;
    private CachePool cachePool;

    static DynamicConfiguration configuration() {
        return new StoreBasedDynamicConfiguration.Builder()
                .propertiesResource("application-defaults.properties")
                .propertiesResource("security.properties")
                .propertiesResource("application.properties")
                .build();
    }

    @BeforeClass
    public void before() {
        configuration = configuration();
        CachingProvider cachingProvider = Caching.getCachingProvider();
        LOG.info("Initializing JCache ScmProvider: {}", cachingProvider.getDefaultURI());
        cacheManager = cachingProvider.getCacheManager();
        cachePool = new CachePool(configuration, cacheManager);
    }

    @Test
    public void testRepositoryConfig() {
        RepositoryConfigService configService = cachePool.getRepositoryConfigService();
        RepositoryConfig config = configService.getConfig();

        List<RepositoryConfig.Repository> repos = config.repositories.get(RepositoryConfig.ScmProvider.GITHUB);
        for(RepositoryConfig.Repository repo : repos) {
            LOG.trace("{}", repo.repositoryPattern.pattern());
        }

        List<RepositoryConfig.RepositoryOverride> repositoryOverrides = config.repositoryOverrides;

        List<RepositoryConfig.Group> groups = config.groups;
    }

    @Test
    public void testRepositoryCache() {
        Cache<String, ScmRepository> repositoryCache = cachePool.createCache("github/repository", ScmRepository.class);
        TestData.instance().repos(configuration, (key, repo) -> {
            repositoryCache.put(CachePool.asRepositoryPath(key.organization, key.repoName, key.branch), repo);
        });
        repositoryCache.forEach(entry -> {
            LOG.trace("{}={}", entry.getKey(), entry.getValue().id);
        });
    }
}
