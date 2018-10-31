package no.cantara.docsite.domain.github.releases;

import no.cantara.docsite.util.JsonbFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertEquals;

public class PushCreatedTagEventTest {

    private static final Logger LOG = LoggerFactory.getLogger(PushCreatedTagEventTest.class);

    @Test
    public void testJson() throws IOException {
        try (InputStream json = ClassLoader.getSystemResourceAsStream("github/PushCreatedTagEvent.json")) {
            GitHubCreatedTagEvent event = JsonbFactory.instance().fromJson(json, GitHubCreatedTagEvent.class);
            assertEquals(event.ref, "v0.0.1");
        }
    }

}
