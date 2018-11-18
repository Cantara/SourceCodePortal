package no.cantara.docsite.domain.config;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class Snyk implements Serializable {
    private static final long serialVersionUID = 846214117299226186L;

    public final @JsonbProperty("badge-prefix") String snykTestPrefix;

    public Snyk(String snykTestPrefix) {
        this.snykTestPrefix = snykTestPrefix;
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
            return new Snyk(snykBuilderProps.get("badge-prefix"));
        }
    }

}
