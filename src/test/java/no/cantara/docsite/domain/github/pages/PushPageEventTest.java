package no.cantara.docsite.domain.github.pages;

import no.cantara.docsite.domain.github.commits.PushCommitEvent;
import no.cantara.docsite.util.JsonbFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertEquals;

public class PushPageEventTest {

    private static final Logger LOG = LoggerFactory.getLogger(PushPageEventTest.class);

    @Test
    public void testName() throws IOException {
        try (InputStream json = ClassLoader.getSystemResourceAsStream("github/PushEventPage.json")) {
            PushCommitEvent event = JsonbFactory.instance().fromJson(json, PushCommitEvent.class);
            assertEquals(event.afterRevision, "28cb78f509d40052afb0260f28c6f01b9eb4280e");
        }
    }

}
