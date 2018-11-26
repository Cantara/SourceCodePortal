package no.cantara.docsite.domain.config;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.domain.github.repos.GitHubRepository;
import no.cantara.docsite.domain.github.repos.RepositoryVisibility;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.cantara.docsite.domain.scm.ScmRepositoryBuilder;
import no.cantara.docsite.json.JsonbFactory;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.net.HttpURLConnection.HTTP_OK;

public class RepositoryConfigLoader {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryConfigLoader.class);

    private final DynamicConfiguration configuration;
    private final CacheStore cacheStore;

    public RepositoryConfigLoader(DynamicConfiguration configuration, CacheStore cacheStore) {
        this.configuration = configuration;
        this.cacheStore = cacheStore;
    }

    List<GitHubRepository> getOrganizationRepos(String organization) {
        String repositoryVisibility = (configuration.evaluateToString("github.repository.visibility") == null ? "public" :
                configuration.evaluateToString("github.repository.visibility"));
        GetGitHubCommand<String> command = new GetGitHubCommand<>("githubRepos", configuration, Optional.empty(),
                String.format("https://api.github.com/orgs/%s/repos?type=%s&per_page=500",
                        organization,
                        RepositoryVisibility.valueOf(repositoryVisibility.toUpperCase())),
                HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = command.execute();
        if (response.statusCode() == HTTP_OK) {
            return Arrays.asList(JsonbFactory.instance().fromJson(response.body(), GitHubRepository[].class));
        }
        LOG.error("Error: HTTP-{} - {}", response.statusCode(), response.body());
        return Collections.emptyList();
    }


    public void OldLoad() {
        List<GitHubRepository> result = getOrganizationRepos(cacheStore.getOldRepositoryConfig().getOrganization(RepoConfig.ScmProvider.GITHUB));
        for (RepoConfig.Repo repoConfig : cacheStore.getOldRepositoryConfig().getConfig().repos.get(RepoConfig.ScmProvider.GITHUB)) {

            /*
                issue: group all repos -- not all repos has defaultGroupRepo. What to do?

                reositories<CacheRepositoryKey, ScmRepository> <- key contains isGroup. A set needs to be built the ScmRepositoryService
                in order to use Map<CacheRepositoryKey, Set<ScmRepository>> groupedRepositories
             */

            for (GitHubRepository repo : result) {
                String repoName = repo.name;
                boolean isGroup = repoName.equalsIgnoreCase(repoConfig.defaultGroupRepo);
                for (Pattern repoPattern : repoConfig.repoPatterns) {
                    Matcher matcher = repoPattern.matcher(repoName);
                    if (matcher.find()) {
                        CacheKey cacheKey = CacheKey.of(repoConfig.organization, repoName, repoConfig.branchPattern);
                        CacheRepositoryKey cacheRepositoryKey = CacheRepositoryKey.of(repoConfig.organization, repoName, repoConfig.branchPattern, repoConfig.groupId, isGroup);
                        cacheStore.getCacheKeys().put(cacheKey, cacheRepositoryKey);
                        cacheStore.getCacheRepositoryKeys().put(cacheRepositoryKey, cacheKey);
                        cacheStore.getCacheGroupKeys().put(cacheRepositoryKey.asCacheGroupKey(), cacheRepositoryKey.groupId);
                        ScmRepository scmRepository = ScmRepository.of(configuration, cacheRepositoryKey, repoConfig.displayName, repoConfig.description, repoConfig.getExternalServices(),
                                repo.id, repo.description, repoConfig.defaultGroupRepo, (repo.license != null ? repo.license.spdxId : null), repo.htmlUrl);
                        cacheStore.getRepositories().put(cacheRepositoryKey, scmRepository);
                    }
                }
            }
        }
    }

    public void load() {
        for(RepositoryConfig.Repository repositoryConfig : cacheStore.getRepositoryConfig().getConfig().repositories.get(RepositoryConfig.ScmProvider.GITHUB)) {
            List<GitHubRepository> result = getOrganizationRepos(repositoryConfig.organization);
            for (GitHubRepository gitHubRepository : result) {
                if (cacheStore.getRepositoryConfig().isRepositoryMatch(RepositoryConfig.ScmProvider.GITHUB, gitHubRepository.name)) {

                    ScmRepositoryBuilder scmRepositoryBuilder = ScmRepositoryBuilder.newBuilder();
                    scmRepositoryBuilder.path(repositoryConfig.organization, gitHubRepository.name, repositoryConfig.branch);
                    scmRepositoryBuilder.id(gitHubRepository.id);
                    scmRepositoryBuilder.description(gitHubRepository.description);
                    scmRepositoryBuilder.licenseSpdxId(gitHubRepository.license != null ? gitHubRepository.license.spdxId : null);
                    scmRepositoryBuilder.htmlUrl(gitHubRepository.htmlUrl);

                    cacheStore.getRepositoryConfig().onRepositoryOverrideMatch(RepositoryConfig.ScmProvider.GITHUB, repositoryConfig.organization, gitHubRepository.name, repositoryConfig.branch, (visitor) -> {
                        scmRepositoryBuilder.configDisplayName(visitor.displayName);
                        scmRepositoryBuilder.configDescription(visitor.description);

                        scmRepositoryBuilder.externalServices(visitor.getExternalServices());

                        visitor.getExternalServices().forEach((k,v) -> {
                            v.getLinks(configuration, repositoryConfig.organization, gitHubRepository.name, repositoryConfig.branch).forEach(l -> {
                                scmRepositoryBuilder.externalLink(v.getId(), l);
                            });
                        });

                    });

                    cacheStore.getRepositories(RepositoryConfig.ScmProvider.GITHUB).put(CacheStore.asRepositoryPath(repositoryConfig.organization, gitHubRepository.name, repositoryConfig.branch), scmRepositoryBuilder.build(configuration));

                }
            }
        }
    }
}
