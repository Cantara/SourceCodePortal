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

        @Override
        public void accept(Deque<JsonTraversalElement> ancestors, JsonTraversalElement jte) {
            if (jte.isRoot()) {
                builder = RepositoryConfig.newBuilder(jte.key);

            } else if ((RepoConfig.ScmProvider.GITHUB.provider() + "/organization").equals(jte.path(ancestors))) {
//                currentGroupBuilder = builder.withProvider(RepoConfig.ScmProvider.GITHUB, ((JsonString) jte.value).getString());

            } else if ((RepoConfig.ScmProvider.BITBUCKET.provider() + "/organization").equals(jte.path(ancestors))) {
//                currentGroupBuilder = builder.withProvider(RepoConfig.ScmProvider.BITBUCKET, ((JsonString) jte.value).getString());

            } else if (jte.isNewSibling() && jte.depth(ancestors) == 1) {
//                currentRepoBuilder = RepoConfig.newRepoBuilder();
//                currentGroupBuilder.withRepo(currentRepoBuilder);

            } else {
//                if ("groupId".equals(jte.key)) currentRepoBuilder.groupId(((JsonString) jte.value).getString());
//                if (jte.parent.isArray() && "repo".equals(jte.parent.key)) currentRepoBuilder.repoPattern(((JsonString) jte.value).getString());
//                if ("branch".equals(jte.key)) currentRepoBuilder.branch(((JsonString) jte.value).getString());
//                if ("display-name".equals(jte.key))
//                    currentRepoBuilder.displayName(((JsonString) jte.value).getString());
//                if ("default-group-repo".equals(jte.key))
//                    currentRepoBuilder.defaultGroupRepo(((JsonString) jte.value).getString());
//
//                if ("jenkins".equalsIgnoreCase(jte.parent.key)) {
//                    currentRepoBuilder.withExternal("jenkins").set(jte.key, ((JsonString) jte.value).getString());
//                }
//                if ("snyk".equalsIgnoreCase(jte.parent.key)) {
//                    currentRepoBuilder.withExternal("snyk").set(jte.key, ((JsonString) jte.value).getString());
//                }
            }
        }
    }
}
