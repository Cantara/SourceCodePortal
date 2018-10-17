package no.cantara.docsite.domain.github.pages;

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

public class PushPageEventTest {

    private static final Logger LOG = LoggerFactory.getLogger(PushPageEventTest.class);

    @Test
    public void testName() throws IOException {
        try (InputStream json = ClassLoader.getSystemResourceAsStream("github/PushEventPage.json")) {
            JsonbConfig config = new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES);
            Jsonb jsonb = JsonbBuilder.create(config);
            PushCommitEvent event = jsonb.fromJson(json, PushCommitEvent.class);
            assertEquals(event.afterRevision, "28cb78f509d40052afb0260f28c6f01b9eb4280e");
        }
    }

}
