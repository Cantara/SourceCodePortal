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

public class PushPageEventTest {

    private static final Logger LOG = LoggerFactory.getLogger(PushPageEventTest.class);

    @Test
    public void testName() throws IOException {
        try (InputStream json = ClassLoader.getSystemResourceAsStream("github/PushEventPage.json")) {
            JsonbConfig config = new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES);
            Jsonb jsonb = JsonbBuilder.create(config);
            PageEvent event = jsonb.fromJson(json, PageEvent.class);
//            LOG.trace("event: {}", event);
        }
    }

}
