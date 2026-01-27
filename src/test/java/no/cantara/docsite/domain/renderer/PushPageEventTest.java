package no.cantara.docsite.domain.renderer;

import no.cantara.docsite.domain.github.commits.GitHubPushCommitEvent;
import no.cantara.docsite.json.JsonbFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PushPageEventTest {

    private static final Logger LOG = LoggerFactory.getLogger(PushPageEventTest.class);

    @Test
    public void testName() throws IOException {
        try (InputStream json = ClassLoader.getSystemResourceAsStream("github/PushEventPage.json")) {
            GitHubPushCommitEvent event = JsonbFactory.instance().fromJson(json, GitHubPushCommitEvent.class);
            assertEquals(event.afterRevision, "28cb78f509d40052afb0260f28c6f01b9eb4280e");
        }
    }

}
