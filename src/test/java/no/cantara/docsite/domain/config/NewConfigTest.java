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

public class NewConfigTest {

    private static final Logger LOG = LoggerFactory.getLogger(RepoConfigTest.class);

    @Ignore
    @Test
    public void testJsonDocumentTraversal() throws IOException {
        JsonObject jsonDocument;
        try (InputStream in = ClassLoader.getSystemResourceAsStream("conf/new_repo_config.json")) {
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
        NewRepoConfig.Builder builder = NewRepoConfig.newBuilder("Title")
                .withProvider(
                        NewRepoConfig.newScmBuilder(NewRepoConfig.ScmProvider.GITHUB)
                            .matchRepository(NewRepoConfig.newMatchRepositoryBuilder().repositoryPattern("SourceCodePortal*").branch("master"))
                            .matchRepository(NewRepoConfig.newMatchRepositoryBuilder().repositoryPattern("Whydah*").branch("master"))
                )
                .withRepositoryOverride(
                        NewRepoConfig.newScmRepositoryOverrideBuilder()
                            .repositoryId("SourceCodePortal")
                            .displayName("Source Code Portal")
                            .description("The SCP gathers")
                            .repository("github:Cantara/SourceCodePortal*")
                            .branch("master")
                            .withExternal(NewRepoConfig.newJenkinsBuilder().prefix("Cantara-"))
                            .withExternal(NewRepoConfig.newSnykBuilder().prefix("Cantara"))
                )
                .withGroup(
                        NewRepoConfig.newGroupBuilder().groupId("SourceCodePortal").displayName("displayName").description("description")
                                .defaultEntryRepository("defaultEntryRepository")
                                .repositorySelector("github:Cantara/SourceCodePortal*")
                )
                .withGroup(
                        NewRepoConfig.newGroupBuilder().groupId("WhyDah").displayName("displayName").description("description")
                                .defaultEntryRepository("defaultEntryRepository")
                                .repositorySelector("Whydah*").repositorySelector("ACS*")
                )
                ;
    }

}
