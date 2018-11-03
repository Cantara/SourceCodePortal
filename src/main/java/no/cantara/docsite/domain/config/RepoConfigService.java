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
    private final Config config;

    public RepoConfigService(String configResourceName) {
        this.configResourceName = configResourceName;
        config = load();
    }

    Config load() {
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

    public Config getConfig() {
        return config;
    }

    static class Loader implements BiConsumer<Deque<JsonTraversalElement>, JsonTraversalElement> {

        private Config.Builder builder;
        private Config.GroupBuilder groupBuilder;
        private Config.RepoBuilder currentRepoBuilder;

        @Override
        public void accept(Deque<JsonTraversalElement> ancestors, JsonTraversalElement jte) {
            if (jte.isRoot()) {
                builder = Config.newBuilder(jte.key);

            } else if ((Config.ScmProvider.GITHUB.provider() + "/organization").equals(jte.path(ancestors))) {
                groupBuilder = builder.withProvider(Config.ScmProvider.GITHUB, ((JsonString) jte.value).getString());

            } else if ((Config.ScmProvider.BITBUCKET.provider() + "/organization").equals(jte.path(ancestors))) {
                groupBuilder = builder.withProvider(Config.ScmProvider.BITBUCKET, ((JsonString) jte.value).getString());

            } else if (jte.isNewSibling()) {
                currentRepoBuilder = Config.newRepoBuilder();
                groupBuilder.withRepo(currentRepoBuilder); // TODO need a repo instance so we can map values onto map

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
