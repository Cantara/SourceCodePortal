package no.cantara.docsite.domain.config;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;


public class Jenkins implements Serializable {
    private static final long serialVersionUID = 5882777423871329549L;

    public final @JsonbProperty("badge-prefix") String jenkinsPrefix;

    public Jenkins(String jenkinsPrefix) {
        this.jenkinsPrefix = jenkinsPrefix;
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
            return new Jenkins(jenkinsBuilderProps.get("badge-prefix"));
        }
    }

}
