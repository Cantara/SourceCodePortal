package no.cantara.docsite.cache;

import no.cantara.docsite.domain.config.RepositoryConfig;
import no.cantara.docsite.domain.config.RepositoryConfigService;
import no.cantara.docsite.domain.github.commits.GitHubCommitRevision;
import no.cantara.docsite.domain.github.contents.GitHubRepositoryContents;
import no.cantara.docsite.domain.maven.MavenPOM;
import no.cantara.docsite.domain.scm.ScmCommitRevision;
import no.cantara.docsite.domain.scm.ScmGroup;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.cantara.docsite.domain.scm.ScmRepositoryContents;
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
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
            LOG.trace("repoMatch: {}", repo.repositoryPattern.pattern());
        }

        List<RepositoryConfig.RepositoryOverride> repositoryOverrides = config.repositoryOverrides;
        for(RepositoryConfig.RepositoryOverride repositoryOverride : repositoryOverrides) {
            LOG.trace("override: {}", repositoryOverride.repositoryPattern);
        }

        List<RepositoryConfig.Group> groups = config.groups;
        for(RepositoryConfig.Group group : groups) {
            LOG.trace("group: {}", group.repositorySelectors.stream().map(m -> m.repositorySelector.pattern()).collect(Collectors.toList()));
        }
    }

    public Cache<String, ScmRepository> getRepositoryCache() {
        Cache<String, ScmRepository> repositoryCache = (cacheManager.getCache("github/repository") == null ? cachePool.createCache("github/repository", ScmRepository.class) : cacheManager.getCache("github/repository"));

        // refactor so that the ScmRepos i create here and not in test data
        // build repo cache and override
        TestData.instance().repos(configuration, (key, repo) -> {
            if (cachePool.getRepositoryConfigService().isRepositoryMatch(RepositoryConfig.ScmProvider.GITHUB, key.repoName)) {

                ScmRepositoryBuilder scmRepositoryBuilder = ScmRepositoryBuilder.newBuilder();
                scmRepositoryBuilder.path(key.organization, key.repoName, key.branch);
                scmRepositoryBuilder.id(repo.id);
                scmRepositoryBuilder.description(repo.description);
                scmRepositoryBuilder.licenseSpdxId(repo.license != null ? repo.license.spdxId : null);
                scmRepositoryBuilder.htmlUrl(repo.htmlUrl);

                // match repo override
                AtomicInteger c = new AtomicInteger(0);
                cachePool.getRepositoryConfigService().onRepositoryOverrideMatch(RepositoryConfig.ScmProvider.GITHUB, key.organization, key.repoName, key.branch, (visitor) -> {
                    scmRepositoryBuilder.configDisplayName(visitor.displayName);
                    scmRepositoryBuilder.configDescription(visitor.description);
                    c.incrementAndGet();

                    scmRepositoryBuilder.externalServices(visitor.getExternalServices());

                    visitor.getExternalServices().forEach((k,v) -> {
                        v.getLinks(configuration, key.organization, key.repoName, key.branch).forEach(l -> {
                            scmRepositoryBuilder.externalLinks.put(v.getId(), l);
                        });
                    });

                });

                repositoryCache.put(CachePool.asRepositoryPath(key.organization, key.repoName, key.branch), scmRepositoryBuilder.build(configuration));
            }
        });

        return repositoryCache;
    }

    @Test
    public void testRepositoryCache() {
        Cache<String, ScmRepository> repositoryCache = getRepositoryCache();

        // show all repos
        repositoryCache.forEach(entry -> {
            LOG.trace("{}={}", entry.getKey(), entry.getValue().id);
            entry.getValue().externalLinks.forEach((k,v) -> LOG.trace("  link: {}", v.getExternalURL()));
        });
    }

    @Test
    public void testMavenProjects() {
        Cache<String, MavenPOM> mavenProjectsCache = (cacheManager.getCache("github/mavenProjects") == null ?
                cachePool.createCache("github/mavenProjects", MavenPOM.class) :
                cacheManager.getCache("github/mavenProjects"));
        Cache<String, ScmRepository> repositoryCache = getRepositoryCache();
        repositoryCache.forEach(entry -> {
            String repoPath = entry.getKey();
            MavenPOM mavenPOM = TestData.instance().mavenPOM(CacheKey.of(entry.getValue().cacheRepositoryKey.organization, entry.getValue().cacheRepositoryKey.repoName, entry.getValue().cacheRepositoryKey.branch));
            if (mavenPOM != null)
                mavenProjectsCache.put(repoPath, mavenPOM);

        });

        mavenProjectsCache.forEach(entry -> {
            LOG.trace("{} -> {}", entry.getKey(), entry.getValue().artifactId);
        });
    }

    @Test
    public void testContents() {
        Cache<String, ScmRepositoryContents> readmeContentsCache = (cacheManager.getCache("github/contents") == null ?
                cachePool.createCache("github/contents", ScmRepositoryContents.class) :
                cacheManager.getCache("github/contents"));

        Cache<String, ScmRepository> repositoryCache = getRepositoryCache();
        repositoryCache.forEach(entry -> {
            String repoPath = entry.getKey();
            CacheKey cacheKey = CacheKey.of(entry.getValue().cacheRepositoryKey.organization, entry.getValue().cacheRepositoryKey.repoName, entry.getValue().cacheRepositoryKey.branch);
            GitHubRepositoryContents readmeContents = TestData.instance().readmeContent(cacheKey);
            readmeContentsCache.put(repoPath, readmeContents.asRepositoryContents(cacheKey));
        });

        readmeContentsCache.forEach(entry -> {
            LOG.trace("{} -> {}", entry.getKey(), entry.getValue().contentUrl);
        });
    }

    @Test
    public void testCommits() {
        Cache<String, ScmRepository> repositoryCache = getRepositoryCache();
        for (Cache.Entry<String, ScmRepository> entry : repositoryCache) {
            Cache<String, ScmCommitRevision> repoCommitRevisionsCache = cachePool.createCache(entry.getKey(), ScmCommitRevision.class);
            CacheKey cacheKey = entry.getValue().cacheRepositoryKey.asCacheKey();
            List<GitHubCommitRevision> commitRevisions = TestData.instance().commitRevisions(cacheKey);
            commitRevisions.forEach(cr -> {
                Date date = cr.commit.commitAuthor.date;
                repoCommitRevisionsCache.put(String.format("/%s/%s", date.toInstant().toString(), cr.sha), cr.asCommitRevision(CacheShaKey.of(cacheKey, "groupId", cr.sha)));
            });
        }

        for (Cache.Entry<String, ScmRepository> entry : repositoryCache) {
            Cache<String, ScmCommitRevision> cache = cacheManager.getCache(entry.getKey());
            LOG.trace("{}:", entry.getKey());
            cache.forEach(cr -> {
                LOG.trace("  {} -> {}", cr.getKey(), cr.getValue().message);
            });

        }
    }

    public Cache<String,ScmGroup> getGroups() {
        Cache<String, ScmGroup> groupCache = (cacheManager.getCache("groups") == null ?
                cachePool.createCache("groups", ScmGroup.class) :
                cacheManager.getCache("groups"));

        cachePool.getRepositoryConfigService().getConfig().groups.forEach(group -> {
            ScmGroup scmGroup = new ScmGroup(group.groupId, group.displayName, group.description, group.defaultEntryRepository);
            groupCache.putIfAbsent(group.groupId, scmGroup);
        });

        Cache<String, ScmRepository> repositoryCache = getRepositoryCache();

        repositoryCache.forEach(entry -> {
            cachePool.getRepositoryConfigService().onGroupMatch(entry.getValue(), visitor -> {
                if (groupCache.containsKey(visitor.groupId)) {
                    ScmGroup scmGroup = groupCache.get(visitor.groupId);
                    scmGroup.addRepository(entry.getKey());
                    groupCache.replace(visitor.groupId, scmGroup);
                } else {
                    ScmGroup scmGroup = new ScmGroup(visitor.groupId, visitor.displayName, visitor.description, visitor.defaultEntryRepository);
                    scmGroup.addRepository(entry.getKey());
                    groupCache.put(visitor.groupId, scmGroup);
                }
            });
        });
        return groupCache;
    }

    @Test
    public void testGroups() {
        getGroups().forEach(entry -> LOG.trace("{} -> {}", entry.getKey(), entry.getValue().repositoryKeys()));
    }

    /*
        Cache plan:
        - Repository (/org/repo/branch) (*)
        - MavenProjects (/org/repo/branch/(subrepo) (sub modules contains pom and is part of a project. Should this be dealt with with in MavemPom or as subcontexts
        - Contents (repo/branch)
        - Commits (org/repo:/commitDate/sha) <- lookups commits per cache and add CacheListener that adds latest commits to Group


        Build Groups:
        - ScmGroup:

     */



}
