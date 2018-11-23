package no.cantara.docsite.cache;

import no.cantara.docsite.domain.config.ExternalService;
import no.cantara.docsite.domain.links.LinkURL;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.ssb.config.DynamicConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

public class ScmRepositoryBuilder {

    final Map<String, String> props = new LinkedHashMap<>();
    final Map<String, LinkURL> externalLinks = new LinkedHashMap<>();
    final Map<Class<?>, ExternalService<?>> externalServices = new LinkedHashMap<>();

    ScmRepositoryBuilder() {
    }

    public static ScmRepositoryBuilder newBuilder() {
        return new ScmRepositoryBuilder();
    }

    public ScmRepositoryBuilder path(String organization, String repoName, String branch) {
        props.put("organization", organization);
        props.put("repoName", repoName);
        props.put("branch", branch);
        return this;
    }

    public void id(String id) {
        props.put("id", id);
    }

    public ScmRepositoryBuilder configDisplayName(String configDisplayName) {
        props.put("config-display-name", configDisplayName);
        return this;
    }

    public ScmRepositoryBuilder configDescription(String configDescription) {
        props.put("config-description", configDescription);
        return this;
    }

    public ScmRepositoryBuilder description(String description) {
        props.put("description", description);
        return this;
    }

    public ScmRepositoryBuilder licenseSpdxId(String licenseSpdxId) {
        props.put("licenseSpdxId", licenseSpdxId);
        return this;
    }

    public ScmRepositoryBuilder htmlUrl(String htmlUrl) {
        props.put("htmlUrl", htmlUrl);
        return this;
    }

    public ScmRepositoryBuilder externalServices(Map<Class<?>, ExternalService<?>> externalServices) {
        this.externalServices.putAll(externalServices);
        return this;
    }

    public ScmRepository build(DynamicConfiguration configuration) {
        CacheRepositoryKey key = CacheRepositoryKey.of(props.get("organization"), props.get("repoName"), props.get("branch"), "", false);
        ScmRepository scmRepository = ScmRepository.of(configuration, key, props.get("config-display-name"), props.get("config-description"), new LinkedHashMap<>(),
                props.get("id"),
                props.get("description"),
                "groupIdShoudlBeRemoved",
                props.get("licenseSpdxId"),
                props.get("htmlUrl"));
        scmRepository.externalLinks.putAll(externalLinks);
        return scmRepository;

    }
}
