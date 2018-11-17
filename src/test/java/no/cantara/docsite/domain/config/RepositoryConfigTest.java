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

import static no.cantara.docsite.domain.config.RepositoryConfig.ScmProvider.GITHUB;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class RepositoryConfigTest {

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
        RepositoryConfig.RepositoryConfigBuilder repositoryConfigBuilder = RepositoryConfig.newBuilder("Title")
                .withProvider(
                        RepositoryConfig.newScmBuilder(GITHUB)
                            .organization("Cantara")
                            .matchRepository(
                                    RepositoryConfig.newMatchRepositoryBuilder().repositoryPattern("SourceCodePortal*").branch("master")
                            )
                            .matchRepository(
                                    RepositoryConfig.newMatchRepositoryBuilder().repositoryPattern("Whydah*").branch("master")
                            )
                )
                .withProvider(
                        RepositoryConfig.newScmBuilder(RepositoryConfig.ScmProvider.BITBUCKET)
                            .organization("Cantara")
                            .matchRepository(
                                    RepositoryConfig.newMatchRepositoryBuilder().repositoryPattern("Repo1*").branch("master")
                            )
                            .matchRepository(
                                    RepositoryConfig.newMatchRepositoryBuilder().repositoryPattern("Repo2*").branch("master")
                            )
                )
                .withRepositoryOverride(
                        RepositoryConfig.newScmRepositoryOverrideBuilder()
                            .repositoryId("SourceCodePortal")
                            .displayName("Source Code Portal")
                            .description("The SCP gathers")
                            .repository("github:Cantara/SourceCodePortal*")
                            .branch("master")
                            .withExternal(
                                    Jenkins.newJenkinsBuilder().prefix("Cantara-")
                            )
                            .withExternal(
                                    Snyk.newSnykBuilder().prefix("Cantara")
                            )
                )
                .withGroup(
                        RepositoryConfig.newGroupBuilder()
                                .groupId("SourceCodePortal")
                                .displayName("displayName")
                                .description("description")
                                .defaultEntryRepository("defaultEntryRepository")
                                .repositorySelector("github:Cantara/SourceCodePortal*")
                )
                .withGroup(
                        RepositoryConfig.newGroupBuilder()
                                .groupId("WhyDah")
                                .displayName("displayName")
                                .description("description")
                                .defaultEntryRepository("defaultEntryRepository")
                                .repositorySelector("github:Cantara/Whydah*").repositorySelector("github:Cantara/ACS*")
                )
                ;
        RepositoryConfig repositoryConfig = repositoryConfigBuilder.build();
        LOG.trace("config: {}", repositoryConfig);
        assertEquals(repositoryConfig.title, "Title");

        assertTrue(repositoryConfig.repositories.get(GITHUB).stream().anyMatch(m -> "Cantara".equals(m.organization)));
        assertTrue(repositoryConfig.repositories.get(GITHUB).stream().anyMatch(m -> "SourceCodePortal*".equals(m.repositoryPattern)));
        assertTrue(repositoryConfig.repositories.get(GITHUB).stream().anyMatch(m -> "Whydah*".equals(m.repositoryPattern)));
        assertTrue(repositoryConfig.repositories.get(RepositoryConfig.ScmProvider.BITBUCKET).stream().anyMatch(m -> "Repo1*".equals(m.repositoryPattern)));
        assertTrue(repositoryConfig.repositories.get(RepositoryConfig.ScmProvider.BITBUCKET).stream().anyMatch(m -> "Repo2*".equals(m.repositoryPattern)));

        assertTrue(repositoryConfig.repositoryOverrides.stream().anyMatch(m -> GITHUB.equals(m.provider)));
        assertTrue(repositoryConfig.repositoryOverrides.stream().anyMatch(m -> "Cantara".equals(m.organization)));
        assertTrue(repositoryConfig.repositoryOverrides.stream().anyMatch(m -> "SourceCodePortal*".equals(m.repositoryPattern)));
        assertTrue(repositoryConfig.repositoryOverrides.stream().anyMatch(m -> "master".equals(m.branch)));

        assertTrue(repositoryConfig.groups.stream().anyMatch(m -> "SourceCodePortal".equals(m.groupId)));
        assertTrue(repositoryConfig.groups.stream().anyMatch(m -> "WhyDah".equals(m.groupId)));
    }

}
