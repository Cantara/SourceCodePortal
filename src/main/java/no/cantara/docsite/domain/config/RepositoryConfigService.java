package no.cantara.docsite.domain.config;

import no.cantara.docsite.json.JsonDocumentTraversal;
import no.cantara.docsite.json.JsonTraversalElement;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Deque;
import java.util.List;
import java.util.function.BiConsumer;

public class RepositoryConfigService {

    private final String configResourceName;
    private final RepositoryConfig repoConfig;

    public RepositoryConfigService(String configResourceName) {
        this.configResourceName = configResourceName;
        repoConfig = load();
    }

    RepositoryConfig load() {
        JsonObject jsonDocument;
        try (InputStream in = ClassLoader.getSystemResourceAsStream(configResourceName)) {
            try (JsonReader reader = Json.createReader(new InputStreamReader(in))) {
                jsonDocument = reader.readObject();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        RepositoryConfigService.Loader loader = new RepositoryConfigService.Loader();
        JsonDocumentTraversal.walk(jsonDocument, loader);
        return loader.builder.build();
    }

    public RepositoryConfig getConfig() {
        return repoConfig;
    }

    public List<RepositoryConfig> getRepositories(RepositoryConfig.ScmProvider provider) {
//        return repoConfig.repos.get(provider);
        return null;
    }

    public String getOrganization(RepositoryConfig.ScmProvider provider) {
//        if (repoConfig.repos.containsKey(provider) && repoConfig.repos.get(provider).iterator().hasNext()) {
//            return repoConfig.repos.get(provider).iterator().next().organization;
//        }
        return null;
    }

    static class Loader implements BiConsumer<Deque<JsonTraversalElement>, JsonTraversalElement> {
        private RepositoryConfig.RepositoryConfigBuilder builder;
        private RepositoryConfig.ScmProvider scmProvider;
        private RepositoryConfig.RepositoryBuilder scmBuilder;
        private RepositoryConfig.MatchRepositoryBuilder matchRepositoryBuilder;
        private RepositoryConfig.RepositoryOverrideBuilder repositoryOverrideBuilder;
        private RepositoryConfig.GroupBuilder groupBuilder;
        private ExternalBuilder<?> externalBuilder;

        @Override
        public void accept(Deque<JsonTraversalElement> ancestors, JsonTraversalElement jte) {

            // create repository builder
            if (jte.isRoot() && "title".equals(jte.key)) {
                builder = RepositoryConfig.newBuilder(jte.asStringValue());

            // set scm provider
            } else if (jte.isRoot() && jte.isArray() && RepositoryConfig.ScmProvider.isValid(jte.key)) {
                scmProvider = RepositoryConfig.ScmProvider.valueOf(jte.key.toUpperCase());

            // create scm builder
            } else if (jte.isArrayElement() && jte.isNewSibling() && jte.path(ancestors).startsWith(scmProvider.provider()) && jte.depth(ancestors) == 0) {
                scmBuilder = RepositoryConfig.newScmBuilder(scmProvider);
                builder.withProvider(scmBuilder);

            // set organization to scm builder
            } else if (jte.isArrayElement() && jte.depth(ancestors) == 1 && "organization".equals(jte.key)) {
                scmBuilder.organization(jte.asStringValue());

            // create match repository builder
            } else if (jte.parent != null && "match-repositories".equals(jte.parent.key) && jte.isNewSibling()) {
                matchRepositoryBuilder = RepositoryConfig.newMatchRepositoryBuilder();
                scmBuilder.matchRepository(matchRepositoryBuilder);

            // set match repository properties
            } else if (jte.parent != null && jte.parent.parent != null && "match-repositories".equals(jte.parent.parent.key) && jte.isArrayElement()) {
                matchRepositoryBuilder.matchRepositoryBuilderProps.put(jte.key, jte.asStringValue());

            // create repository override builder
            } else if (jte.parent != null && jte.parent.isRoot() && jte.isNewSibling() && "repository-overrides".equals(jte.parent.key)) {
                repositoryOverrideBuilder = RepositoryConfig.newScmRepositoryOverrideBuilder();
                builder.withRepositoryOverride(repositoryOverrideBuilder);

            // add repository override
            } else if (jte.parent != null && jte.parent.parent != null && "repository-overrides".equals(jte.parent.parent.key) && jte.isArrayElement()) {
                repositoryOverrideBuilder.overrideBuilderProps.put(jte.key, jte.asStringValue());

            // add external service to repository override
            } else if (jte.parent != null && jte.parent.parent != null && jte.parent.parent.parent != null && "repository-overrides".equals(jte.parent.parent.parent.key)
                    && "external".equals(jte.parent.key) && jte.parent.isArrayElement() && jte.depth(ancestors) == 2) {
                externalBuilder = ExternalBuilders.getExternalBuilder(jte.key);
                repositoryOverrideBuilder.withExternal(externalBuilder);

            // set external badge prefix
            } else if (jte.parent != null && jte.parent.parent != null && "external".equals(jte.parent.parent.key) && jte.depth(ancestors) == 3) {
                externalBuilder.set(jte.key, jte.asStringValue());

            // create group builder
            } else if (jte.parent != null && jte.parent.isRoot() && jte.isNewSibling() && "groups".equals(jte.parent.key)) {
                groupBuilder = RepositoryConfig.newGroupBuilder();
                builder.withGroup(groupBuilder);

            // set group key/values
            } else if (jte.parent != null && jte.parent.parent != null && "groups".equals(jte.parent.parent.key) && jte.isArrayElement()) {
                groupBuilder.groupBuilderProps.put(jte.key, jte.asStringValue());

            // set group repositorySelector
            } else if (jte.parent != null && jte.parent.parent != null && jte.parent.parent.parent != null && "groups".equals(jte.parent.parent.parent.key) && jte.isArrayElement() && "repository-selector".equals(jte.parent.key)) {
                groupBuilder.repositorySelector(jte.asStringValue());

            }
        }
    }
}
