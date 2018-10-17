package no.cantara.docsite.domain.github.releases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyNamingStrategy;
import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertEquals;

public class PushCreatedTagEventTest {

    private static final Logger LOG = LoggerFactory.getLogger(PushCreatedTagEventTest.class);

    @Test
    public void testJson() throws IOException {
        try (InputStream json = ClassLoader.getSystemResourceAsStream("github/PushCreatedTagEvent.json")) {
            JsonbConfig config = new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES);
            Jsonb jsonb = JsonbBuilder.create(config);
            CreatedTagEvent event = jsonb.fromJson(json, CreatedTagEvent.class);
            assertEquals(event.ref, "v0.0.1");
        }
    }

}
