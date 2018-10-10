package no.cantara.docsite.github;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyNamingStrategy;
import java.io.IOException;
import java.io.InputStream;

public class PushCreatedTagEventTest {

    private static final Logger LOG = LoggerFactory.getLogger(PushCreatedTagEventTest.class);

    @Test
    public void testJson() throws IOException {
        try (InputStream json = ClassLoader.getSystemResourceAsStream("github/PushCreatedTagEvent.json")) {
            JsonbConfig config = new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES);
            Jsonb jsonb = JsonbBuilder.create(config);
            PushCreatedTagEvent event = jsonb.fromJson(json, PushCreatedTagEvent.class);
            LOG.trace("event: {}", event);
        }

    }

}
