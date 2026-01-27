package no.cantara.docsite.domain.config;

import no.cantara.docsite.json.JsonDocumentTraversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RepoConfigTest {

    private static final Logger LOG = LoggerFactory.getLogger(RepoConfigTest.class);

//    @Ignore
    @Test
    public void testJsonDocumentTraversal() throws IOException {
        JsonObject jsonDocument;
        try (InputStream in = ClassLoader.getSystemResourceAsStream("conf/config.json")) {
            try (JsonReader reader = Json.createReader(new InputStreamReader(in))) {
                jsonDocument = reader.readObject();
            }
        }

        JsonDocumentTraversal.walk(jsonDocument, (ancestors, jte) -> LOG.trace("{}[{}] {} isArray: {} - isNewSibling: {} - isArrayElement: {} -> {}: {} -> {}",
                ancestors.stream().map(m -> " ").collect(Collectors.joining()), jte.depth(ancestors), jte.value.getValueType(),
                jte.isArray(), jte.isNewSibling(), jte.isArrayElement(),
                jte.path(ancestors), jte.key, jte.value));
    }

    @Test
    public void testConfigBuilder() {
        RepoConfig repoConfig = RepoConfig.newBuilder("Title")
                .withProvider(RepoConfig.ScmProvider.GITHUB, "Cantara")
                .withRepo(RepoConfig.newRepoBuilder().groupId("SourceCodePortal").repoPattern("SourceCodePortal*").displayName("heading").description("desc").defaultGroupRepo("SourceCodePortal").branch("master"))
                .withRepo(
                        RepoConfig.newRepoBuilder()
                                .groupId("Whydah").repoPattern("Whydah*").displayName("heading").description("desc").defaultGroupRepo("Whydah").branch("master")
                                .withExternal(RepoConfig.newJenkinsBuilder().prefix("Cantara-"))
                                .withExternal(RepoConfig.newSnykBuilder().prefix("Cantara"))
                )
                .build();
        assertNotNull(repoConfig);
        LOG.trace("config: {}", repoConfig);
    }

    @Test
    public void testLoadConfig() {
        RepoConfigService configService = new RepoConfigService("conf/config.json");
        LOG.trace("config: {}", configService.getConfig());
        RepoConfig.Jenkins jenkins = configService.getRepositories(RepoConfig.ScmProvider.GITHUB).get(0).getService(RepoConfig.Jenkins.class);
        LOG.trace("--> {}", jenkins.jenkinsPrefix);
    }
}
