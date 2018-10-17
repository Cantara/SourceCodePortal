package no.cantara.docsite.domain.config;

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

public class RepositoryConfigTest {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryConfigTest.class);

    @Test
    public void testJson() throws IOException {
        try (InputStream json = ClassLoader.getSystemResourceAsStream("conf/config.json")) {
            JsonbConfig config = new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES);
            Jsonb jsonb = JsonbBuilder.create(config);
            RepositoryConfig repositoryConfig = jsonb.fromJson(json, RepositoryConfig.class);
            assertEquals(repositoryConfig.gitHub.organization, "Cantara");
        }
    }

}
