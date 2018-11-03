package no.cantara.docsite.domain.config;

import no.cantara.docsite.json.JsonDocumentTraversal;
import no.cantara.docsite.json.JsonTraversalElement;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Deque;
import java.util.function.BiConsumer;

public class RepoConfigService {

    private final String configResourceName;
    private final RepoConfig repoConfig;

    public RepoConfigService(String configResourceName) {
        this.configResourceName = configResourceName;
        repoConfig = load();
    }

    RepoConfig load() {
        JsonObject jsonDocument;
        try (InputStream in = ClassLoader.getSystemResourceAsStream(configResourceName)) {
            try (JsonReader reader = Json.createReader(new InputStreamReader(in))) {
                jsonDocument = reader.readObject();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Loader loader = new Loader();
        JsonDocumentTraversal.walk(jsonDocument, loader);
        return loader.builder.build();
    }

    public RepoConfig getConfig() {
        return repoConfig;
    }

    public String getOrganization(RepoConfig.ScmProvider provider) {
        if (repoConfig.repos.containsKey(provider) && repoConfig.repos.get(provider).iterator().hasNext()) {
            return repoConfig.repos.get(provider).iterator().next().organization;
        }
        return null;
    }

    static class Loader implements BiConsumer<Deque<JsonTraversalElement>, JsonTraversalElement> {

        private RepoConfig.Builder builder;
        private RepoConfig.GroupBuilder currentGroupBuilder;
        private RepoConfig.RepoBuilder currentRepoBuilder;

        @Override
        public void accept(Deque<JsonTraversalElement> ancestors, JsonTraversalElement jte) {
            if (jte.isRoot()) {
                builder = RepoConfig.newBuilder(jte.key);

            } else if ((RepoConfig.ScmProvider.GITHUB.provider() + "/organization").equals(jte.path(ancestors))) {
                currentGroupBuilder = builder.withProvider(RepoConfig.ScmProvider.GITHUB, ((JsonString) jte.value).getString());

            } else if ((RepoConfig.ScmProvider.BITBUCKET.provider() + "/organization").equals(jte.path(ancestors))) {
                currentGroupBuilder = builder.withProvider(RepoConfig.ScmProvider.BITBUCKET, ((JsonString) jte.value).getString());

            } else if (jte.isNewSibling()) {
                currentRepoBuilder = RepoConfig.newRepoBuilder();
                currentGroupBuilder.withRepo(currentRepoBuilder);

            } else {
                if ("groupId".equals(jte.key)) currentRepoBuilder.groupId(((JsonString) jte.value).getString());
                if ("repo".equals(jte.key)) currentRepoBuilder.repo(((JsonString) jte.value).getString());
                if ("branch".equals(jte.key)) currentRepoBuilder.branch(((JsonString) jte.value).getString());
                if ("display-name".equals(jte.key))
                    currentRepoBuilder.displayName(((JsonString) jte.value).getString());
                if ("default-group-repo".equals(jte.key))
                    currentRepoBuilder.defaultGroupRepo(((JsonString) jte.value).getString());
            }
        }
    }
}
