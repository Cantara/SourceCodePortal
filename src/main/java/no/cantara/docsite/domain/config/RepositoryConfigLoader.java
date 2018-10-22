package no.cantara.docsite.domain.config;

import no.cantara.docsite.cache.CacheGroupKey;
import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.domain.github.repos.GitHubRepository;
import no.cantara.docsite.domain.github.repos.RepositoryVisibility;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.JsonbBuilder;
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
            return Arrays.asList(JsonbBuilder.create().fromJson(response.body(), GitHubRepository[].class));
        }
        LOG.error("Error: HTTP-{} - {}", response.statusCode(), response.body());
        return Collections.emptyList();
    }


    public void load() {
        List<GitHubRepository> result = getOrganizationRepos(cacheStore.getRepositoryConfig().gitHub.organization);

        for(RepositoryConfig.Repo repoConfig : cacheStore.getRepositoryConfig().gitHub.repos) {
            Pattern pattern = Pattern.compile(repoConfig.repo);
            for (GitHubRepository repo : result) {
                String repoName = repo.name;
                Matcher matcher = pattern.matcher(repoName);
                if (matcher.find()) {
                    CacheKey cacheKey = CacheKey.of(cacheStore.getRepositoryConfig().gitHub.organization, repoName, repoConfig.branch);
                    String rawRepoURL = String.format("https://raw.githubusercontent.com/%s/%s/%s/", cacheKey.organization, cacheKey.repoName, cacheKey.branch);
                    String readmeURL = String.format("https://api.github.com/repos/%s/%s/readme?ref=%s", cacheKey.organization, cacheKey.repoName, cacheKey.branch);
                    String contentsURL = String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", cacheKey.organization, cacheKey.repoName, "%s", "%s");
                    CacheGroupKey cacheGroupKey = CacheGroupKey.of(cacheKey, repoConfig.groupId);
//                    LOG.info("Cache cacheGroupKey: {}", cacheGroupKey);
                    cacheStore.getCacheKeys().put(cacheKey, cacheGroupKey);
                    cacheStore.getCacheGroupKeys().put(cacheGroupKey, cacheKey);
                    // copy relevant repo info to Repository instance
                    Repository repository = new Repository(cacheKey, repo.id, repo.name, repo.description, repo.htmlUrl, rawRepoURL, readmeURL, contentsURL);
                    cacheStore.getRepositoryGroups().put(cacheGroupKey, repository);
                }
            }
        }
    }

}
