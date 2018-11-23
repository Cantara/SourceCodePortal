package no.cantara.docsite.domain.config;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.domain.links.LinkURL;
import no.cantara.docsite.domain.links.SnykIOTestBadgeURL;
import no.cantara.docsite.domain.links.SnykIOTestURL;
import no.ssb.config.DynamicConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Snyk implements ExternalService<Snyk> {
    private static final long serialVersionUID = 846214117299226186L;

    private final String id;
    private final String snykTestPrefix;

    public Snyk(String id, String snykTestPrefix) {
        this.id = id;
        this.snykTestPrefix = snykTestPrefix;
    }

    public SnykIOTestURL toTestURL(CacheKey cacheKey) {
        return new SnykIOTestURL(null);
    }

    public SnykIOTestBadgeURL toTestBadgeURL(CacheKey cacheKey) {
        return new SnykIOTestBadgeURL(cacheKey);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPrefix() {
        return snykTestPrefix;
    }

    @Override
    public Iterable<LinkURL<?>> getLinks(DynamicConfiguration configuration, String organization, String repoName, String branch) {
        List<LinkURL<?>> links = new ArrayList<>();
//        links.add(new SnykIOTestURL(configuration, CacheKey.of(organization, repoName, branch), getPrefix()));
//        links.add(new SnykIOTestBadgeURL(configuration, CacheKey.of(organization, repoName, branch), getPrefix()));
        return Collections.unmodifiableList(links);
    }


    public static SnykBuilder newSnykBuilder() {
        return new SnykBuilder();
    }

    public static class SnykBuilder implements ExternalBuilder<Snyk> {
        private final Map<String, String> snykBuilderProps = new LinkedHashMap<>();

        @Override
        public String getConfigKey() {
            return "snyk";
        }

        @Override
        public ExternalBuilder<Snyk> set(String key, String value) {
            snykBuilderProps.put(key, value);
            return this;
        }

        public SnykBuilder prefix(String snykTestPrefix) {
            snykBuilderProps.put("badge-prefix", snykTestPrefix);
            return this;
        }

        @Override
        public Snyk build() {
            return new Snyk(getConfigKey(), snykBuilderProps.get("badge-prefix"));
        }
    }

}
