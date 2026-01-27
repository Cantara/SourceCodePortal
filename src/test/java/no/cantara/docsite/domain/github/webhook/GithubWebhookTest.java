package no.cantara.docsite.domain.github.webhook;

import no.cantara.docsite.test.client.TestClient;
import no.cantara.docsite.test.server.TestServerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@ExtendWith(TestServerExtension.class)
public class GithubWebhookTest {

    @Inject TestClient client;

    @Test
    public void testGithubWebhook() throws IOException {
        try (InputStream is = ClassLoader.getSystemResourceAsStream("github/PushEventPage.json")) {
            String json = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
            // X-GitHub-Event
            client.post("/github/webhook", json);
        }
    }
}
