package no.cantara.docsite.domain.config;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.domain.links.JenkinsURL;
import no.cantara.docsite.domain.links.LinkURL;
import no.ssb.config.DynamicConfiguration;

import javax.json.bind.annotation.JsonbProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Jenkins implements ExternalService<Jenkins> {
    private static final long serialVersionUID = 5882777423871329549L;

    private final String id;
    public final @JsonbProperty("badge-prefix") String jenkinsPrefix;

    public Jenkins(String id, String jenkinsPrefix) {
        this.id = id;
        this.jenkinsPrefix = jenkinsPrefix;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPrefix() {
        return jenkinsPrefix;
    }

    @Override
    public Iterable<LinkURL<?>> getLinks(DynamicConfiguration configuration, String organization, String repoName, String branch) {
        List<LinkURL<?>> links = new ArrayList<>();
        links.add(new JenkinsURL(configuration, CacheKey.of(organization, repoName, branch), jenkinsPrefix));
        return Collections.unmodifiableList(links);
    }


    public static JenkinsBuilder newJenkinsBuilder() {
        return new JenkinsBuilder();
    }

    public static class JenkinsBuilder implements ExternalBuilder<Jenkins> {
        private final Map<String, String> jenkinsBuilderProps = new LinkedHashMap<>();

        @Override
        public String getConfigKey() {
            return "jenkins";
        }

        @Override
        public ExternalBuilder<Jenkins> set(String key, String value) {
            jenkinsBuilderProps.put(key, value);
            return this;
        }

        public JenkinsBuilder prefix(String jenkinsPrefix) {
            jenkinsBuilderProps.put("badge-prefix", jenkinsPrefix);
            return this;
        }

        @Override
        public Jenkins build() {
            return new Jenkins(getConfigKey(), jenkinsBuilderProps.get("badge-prefix"));
        }
    }

}

