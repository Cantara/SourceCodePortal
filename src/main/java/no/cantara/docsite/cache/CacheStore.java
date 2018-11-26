package no.cantara.docsite.cache;

import no.cantara.docsite.domain.config.RepoConfig;
import no.cantara.docsite.domain.config.RepoConfigService;
import no.cantara.docsite.domain.config.RepositoryConfig;
import no.cantara.docsite.domain.config.RepositoryConfigService;
import no.cantara.docsite.domain.github.releases.GitHubCreatedTagEvent;
import no.cantara.docsite.domain.jenkins.JenkinsBuildStatus;
import no.cantara.docsite.domain.maven.MavenPOM;
import no.cantara.docsite.domain.scm.ScmCommitRevision;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.cantara.docsite.domain.scm.ScmRepositoryContents;
import no.cantara.docsite.domain.shields.ShieldsStatus;
import no.cantara.docsite.domain.snyk.SnykTestStatus;
import no.cantara.docsite.json.JsonbFactory;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CacheStore {

    public static final String CACHE_KEYS = "cacheKeys";
    public static final String CACHE_GROUP_KEYS = "cacheGroupKeys";
    public static final String CACHE_REPOSITORY_KEYS = "cacheRepositoryKeys";
    public static final String REPOSITORIES = "repositories";
    public static final String MAVEN_PROJECTS = "mavenProject";
    public static final String CONTENTS = "contents";
    public static final String COMMITS = "commits";
    public static final String RELEASES = "releases";
    public static final String CANTARA_WIKI = "cantaraWiki";
    public static final String JENKINS_BUILD_STATUS = "jenkinsBuildStatus";
    public static final String SNYK_TEST_STATUS = "snykTestStatus";
    public static final String GITHUB_ISSUES_STATUS = "githubIssueStatus";
    public static final String GITHUB_COMMITS_STATUS = "githubCommitsStatus";
    public static final String GITHUB_RELEASE_STATUS = "gitHubReleaseStatus";

    private static final Logger LOG = LoggerFactory.getLogger(CacheStore.class);

    final DynamicConfiguration configuration;
    final CacheManager cacheManager;
    final RepoConfigService repositoryConfig;
    final RepositoryConfigService newRepositoryConfig;

    CacheStore(DynamicConfiguration configuration, CacheManager cacheManager) {
        this.configuration = configuration;
        this.cacheManager = cacheManager;
        this.repositoryConfig = load();
        this.newRepositoryConfig = new RepositoryConfigService(configuration.evaluateToString("cache.config"));
    }

    RepoConfigService load() {
        return new RepoConfigService("conf/config.json");
    }

    public static String asRepositoryPath(String organization, String repoName, String branch) {
        return String.format("/%s/%s/%s", organization, repoName, branch);
    }

    <V> Cache<String,V> createOrGetCache(String cacheName, Class<V> classValue) {
        if (cacheManager.getCache(cacheName) == null) {
            MutableConfiguration<String, V> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            return cacheManager.createCache(cacheName, cacheConfig);
        } else {
            return cacheManager.getCache(cacheName);
        }
    }

    void initialize() {
//        for(RepositoryConfig.ScmProvider scmProvider : RepositoryConfig.ScmProvider.values()) {
//            createOrGetCache(scmProvider.provider() + "/repository", ScmRepository.class);
//        }


        if (cacheManager.getCache(CACHE_KEYS) == null) {
            LOG.info("Creating CacheKeys cache");
            MutableConfiguration<CacheGroupKey, CacheRepositoryKey> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(CACHE_KEYS, cacheConfig);
        }

        if (cacheManager.getCache(CACHE_GROUP_KEYS) == null) {
            LOG.info("Creating CacheGroupKeys cache");
            MutableConfiguration<CacheGroupKey, String> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(CACHE_GROUP_KEYS, cacheConfig);
        }

        if (cacheManager.getCache(CACHE_REPOSITORY_KEYS) == null) {
            LOG.info("Creating CacheRepositoryKeys cache");
            MutableConfiguration<CacheRepositoryKey, CacheKey> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(CACHE_REPOSITORY_KEYS, cacheConfig);
        }

        if (cacheManager.getCache(REPOSITORIES) == null) {
            LOG.info("Creating Repositories cache");
            MutableConfiguration<CacheRepositoryKey, ScmRepository> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(REPOSITORIES, cacheConfig);
        }

        if (cacheManager.getCache(MAVEN_PROJECTS) == null) {
            LOG.info("Creating Maven Projects cache");
            MutableConfiguration<CacheKey, MavenPOM> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(MAVEN_PROJECTS, cacheConfig);
        }

        if (cacheManager.getCache(CONTENTS) == null) {
            LOG.info("Creating Contents cache");
            MutableConfiguration<CacheKey, ScmRepositoryContents> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(CONTENTS, cacheConfig);
        }

        if (cacheManager.getCache(COMMITS) == null) {
            LOG.info("Creating Commits cache");
            MutableConfiguration<CacheShaKey, ScmCommitRevision> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(COMMITS, cacheConfig);
        }

        if (cacheManager.getCache(RELEASES) == null) {
            LOG.info("Creating Releases cache");
            MutableConfiguration<CacheKey, GitHubCreatedTagEvent> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(RELEASES, cacheConfig);
        }

        if (cacheManager.getCache(CANTARA_WIKI) == null) {
            LOG.info("Creating Cantara Wiki cache");
            MutableConfiguration<CacheCantaraWikiKey, String> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(CANTARA_WIKI, cacheConfig);
        }

        if (cacheManager.getCache(JENKINS_BUILD_STATUS) == null) {
            LOG.info("Creating Jenkins Build Status cache");
            MutableConfiguration<CacheKey, JenkinsBuildStatus> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(JENKINS_BUILD_STATUS, cacheConfig);
        }

        if (cacheManager.getCache(SNYK_TEST_STATUS) == null) {
            LOG.info("Creating Snyk Test Status cache");
            MutableConfiguration<CacheKey, SnykTestStatus> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(SNYK_TEST_STATUS, cacheConfig);
        }

        if (cacheManager.getCache(GITHUB_ISSUES_STATUS) == null) {
            LOG.info("Creating Shields Issues Status cache");
            MutableConfiguration<CacheKey, ShieldsStatus> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(GITHUB_ISSUES_STATUS, cacheConfig);
        }

        if (cacheManager.getCache(GITHUB_COMMITS_STATUS) == null) {
            LOG.info("Creating Shields Commits Status cache");
            MutableConfiguration<CacheKey, ShieldsStatus> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(GITHUB_COMMITS_STATUS, cacheConfig);
        }

        if (cacheManager.getCache(GITHUB_RELEASE_STATUS) == null) {
            LOG.info("Creating Shields Releases Status cache");
            MutableConfiguration<CacheKey, ShieldsStatus> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(GITHUB_RELEASE_STATUS, cacheConfig);
        }
    }

    @Deprecated
    public List<ScmRepository> getRepositoryGroupsByGroupId(String groupId) {
        List<ScmRepository> repositories = new ArrayList<>();
        getRepositories().forEach(a -> {
            if (a.getKey().compareTo(groupId)) {
                repositories.add(a.getValue());
            }
        });
        return repositories;
    }

    // TODO fix this when groups repos is in place
    public String getConfiguredRepositories() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        for (RepoConfig.Repo repo : repositoryConfig.getConfig().repos.get(RepoConfig.ScmProvider.GITHUB)) {
            JsonArrayBuilder groupBuilder = Json.createArrayBuilder();
            List<ScmRepository> repositories = getRepositoryGroupsByGroupId(repo.groupId);
            for (ScmRepository repository : repositories) {
                groupBuilder.add(repository.cacheRepositoryKey.asIdentifier());
            }
            builder.add(repo.groupId, groupBuilder);
        }
        return JsonbFactory.instance().toJson(builder.build());
    }


    public CacheManager getCacheManager() {
        return cacheManager;
    }

    @Deprecated
    public RepoConfigService getOldRepositoryConfig() {
        return repositoryConfig;
    }

    public RepositoryConfigService getRepositoryConfig() {
        return newRepositoryConfig;
    }

    @Deprecated
    public Cache<CacheKey, CacheRepositoryKey> getCacheKeys() {
        return cacheManager.getCache(CACHE_KEYS);
    }

    @Deprecated
    public Cache<CacheGroupKey, String> getCacheGroupKeys() {
        return cacheManager.getCache(CACHE_GROUP_KEYS);
    }

    @Deprecated
    public Cache<CacheRepositoryKey, CacheKey> getCacheRepositoryKeys() {
        return cacheManager.getCache(CACHE_REPOSITORY_KEYS);
    }

    @Deprecated
    // used by FetchGitHubCommitRevision(s)
    public Set<CacheRepositoryKey> getCacheRepositoryKeys(CacheKey cacheKey) {
        return StreamSupport.stream(getCacheRepositoryKeys().spliterator(), true)
                .filter(entry -> entry.getValue().equals(cacheKey))
                .map(Cache.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Deprecated
    public Cache<CacheRepositoryKey, ScmRepository> getRepositories() {
        return cacheManager.getCache(REPOSITORIES);
    }

    public Cache<String, ScmRepository> getRepositories(RepositoryConfig.ScmProvider scmProvider) {
        return createOrGetCache(scmProvider.provider() + "/repository", ScmRepository.class);
    }

    public Cache<CacheKey, MavenPOM> getMavenProjects() {
        return cacheManager.getCache(MAVEN_PROJECTS);
    }

    public Cache<CacheKey, ScmRepositoryContents> getReadmeContents() {
        return cacheManager.getCache(CONTENTS);
    }

    public Cache<CacheShaKey, ScmCommitRevision> getCommits() {
        return cacheManager.getCache(COMMITS);
    }

    public Cache<CacheKey, GitHubCreatedTagEvent> getReleases() {
        return cacheManager.getCache(RELEASES);
    }

    public Cache<CacheCantaraWikiKey, String> getCantaraWiki() {
        return cacheManager.getCache(CANTARA_WIKI);
    }

    public Cache<CacheKey, JenkinsBuildStatus> getJenkinsBuildStatus() {
        return cacheManager.getCache(JENKINS_BUILD_STATUS);
    }

    public Cache<CacheKey, SnykTestStatus> getSnykTestStatus() {
        return cacheManager.getCache(SNYK_TEST_STATUS);
    }

    public Cache<CacheKey, ShieldsStatus> getSheildIssuesStatus() {
        return cacheManager.getCache(GITHUB_ISSUES_STATUS);
    }

    public Cache<CacheKey, ShieldsStatus> getSheildCommitsStatus() {
        return cacheManager.getCache(GITHUB_COMMITS_STATUS);
    }

    public Cache<CacheKey, ShieldsStatus> getShieldReleasesStatus() {
        return cacheManager.getCache(GITHUB_RELEASE_STATUS);
    }

}
