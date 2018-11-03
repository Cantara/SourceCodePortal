package no.cantara.docsite.domain.config;

import no.cantara.docsite.json.JsonDocumentTraversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.testng.Assert.assertNotNull;

public class ConfigTest {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigTest.class);

    @Ignore
    @Test
    public void testJsonDocumentTraversal() throws IOException {
        JsonObject jsonDocument;
        try (InputStream in = ClassLoader.getSystemResourceAsStream("conf/config.json")) {
            try (JsonReader reader = Json.createReader(new InputStreamReader(in))) {
                jsonDocument = reader.readObject();
            }
        }

        JsonDocumentTraversal.walk(jsonDocument, (ancestors, jte) -> LOG.trace("{}[{}] {} isArray: {} - isNewSibling: {} - isArrayElement: {} -> {}: {} -> {}",
                ancestors.stream().map(m -> " ").collect(Collectors.joining()), jte.uri(ancestors).length, jte.value.getValueType(),
                jte.isArray(), jte.isNewSibling(), jte.isArrayElement(),
                jte.path(ancestors), jte.key, jte.value));
    }

    @Test
    public void testConfigBuilder() {
        Config config = Config.newBuilder("Title")
                .withProvider(Config.ScmProvider.GITHUB, "Cantara")
                .withRepo(Config.newRepoBuilder().groupId("SourceCodePortal").repo("SourceCodePortal*").displayName("heading").description("desc").defaultGroupRepo("SourceCodePortal").branch("master"))
                .withRepo(Config.newRepoBuilder().groupId("Whydah").repo("Whydah*").displayName("heading").description("desc").defaultGroupRepo("Whydah").branch("master"))
                .build();
        assertNotNull(config);
        LOG.trace("config: {}", config);
    }

    @Test
    public void testLoadConfig() throws IOException {
        RepoConfigService configService = new RepoConfigService("conf/config.json");
        LOG.trace("config: {}", configService.getConfig());
    }
}
