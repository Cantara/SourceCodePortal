package no.cantara.docsite.domain.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExternalBuilders {
    private static final Map<String, Class<? extends ExternalBuilder<?>>> EXTERNAL_BUILDERS = new LinkedHashMap<>();

    static {
        ExternalBuilders.EXTERNAL_BUILDERS.put("jenkins", Jenkins.JenkinsBuilder.class);
        ExternalBuilders.EXTERNAL_BUILDERS.put("snyk", Snyk.SnykBuilder.class);
    }

    final Map<String, ExternalBuilder<?>> externalBuilderProps = new LinkedHashMap<>();

    public static ExternalBuilder<?> getExternalBuilder(String configKey) {
        return ExternalBuilder.create(EXTERNAL_BUILDERS.get(configKey));
    }
}
