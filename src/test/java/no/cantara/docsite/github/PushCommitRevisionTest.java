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
import java.util.Arrays;
import java.util.stream.Collectors;

public class PushCommitRevisionTest {

    private static final Logger LOG = LoggerFactory.getLogger(PushCommitRevisionTest.class);

    @Test
    public void testJson() throws IOException {
        try (InputStream json = ClassLoader.getSystemResourceAsStream("github/PushCommitsEvent.json")) {
            JsonbConfig config = new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES);
            Jsonb jsonb = JsonbBuilder.create(config);
            PushCommitRevision[] event = jsonb.fromJson(json, PushCommitRevision[].class);
            LOG.trace("event: {}", Arrays.stream(event).map(PushCommitRevision::toString).collect(Collectors.joining("\n")));
        }

    }
}
