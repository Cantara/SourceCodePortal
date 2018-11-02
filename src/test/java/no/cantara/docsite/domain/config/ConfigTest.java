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

        JsonDocumentTraversal.walk(jsonDocument, (ancestors, jte) -> LOG.trace("{}[{}] {} isNewSibling: {} - isArray: {} - isArrayElement: {} -> {}: {} -> {}",
                ancestors.stream().map(m -> " ").collect(Collectors.joining()), jte.uri(ancestors).length, jte.value.getValueType(),
                jte.isNewSibling(), jte.isArray(), jte.isArrayElement(),
                jte.path(ancestors), jte.key, jte.value));
    }

    @Test
    public void testName() {
        // Scm(github).withOrg("cantara").with(ScmRepo)

//        Config.Builder builder = Config.newBuilder("Cantara Source Code Portal")
//                .withScm(Config.Provider.GITHUB).withOrganization("Cantara");
//        Config config = builder.build();
    }
}
