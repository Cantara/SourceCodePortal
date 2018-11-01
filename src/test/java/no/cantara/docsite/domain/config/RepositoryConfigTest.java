package no.cantara.docsite.domain.config;

import no.cantara.docsite.json.JsonbFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertEquals;

public class RepositoryConfigTest {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryConfigTest.class);

    @Test
    public void testJson() throws IOException {
        try (InputStream json = ClassLoader.getSystemResourceAsStream("conf/config.json")) {
            RepositoryConfigBinding repositoryConfig = JsonbFactory.instance().fromJson(json, RepositoryConfigBinding.class);
            assertEquals(repositoryConfig.gitHub.organization, "Cantara");
        }
    }

}
