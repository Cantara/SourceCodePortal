package no.cantara.docsite.domain.config;

import no.cantara.docsite.json.JsonDocumentTraversal;
import no.cantara.docsite.json.JsonTraversalElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Deque;
import java.util.function.BiConsumer;

public class ConfigTest {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigTest.class);

    @Test
    public void testJsonDocumentTraversal() throws IOException {
        JsonObject jsonObject;
        try (InputStream in = ClassLoader.getSystemResourceAsStream("conf/config.json")) {
            try (JsonReader reader = Json.createReader(new InputStreamReader(in))) {
                jsonObject = reader.readObject();
            }
        }

        BiConsumer<Deque<JsonTraversalElement>, JsonTraversalElement> visitor = (ancestors, jte) -> {
            StringBuffer buf = new StringBuffer();
            for(int n=0; n<jte.depth(ancestors); n++) buf.append(" ");
            LOG.trace("{}[{}]({}|{}) {}: [{}] => {} -> {}", buf.toString(), jte.uri(ancestors).length, jte.parent, jte.key, jte.value.getValueType(), jte.path(ancestors), jte.key, jte.value);
        };
        JsonDocumentTraversal.walk(jsonObject, visitor);
    }

    @Test
    public void testName() {
//        Config.Builder builder = Config.newBuilder("Cantara Source Code Portal")
//                .withScm(Config.Provider.GITHUB).withOrganization("Cantara");
//        Config config = builder.build();
    }
}
