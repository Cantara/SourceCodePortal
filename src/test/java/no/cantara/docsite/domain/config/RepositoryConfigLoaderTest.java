package no.cantara.docsite.domain.config;

import no.cantara.docsite.model.config.RepositoryConfig;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Map;

public class RepositoryConfigLoaderTest {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryConfigLoaderTest.class);

    static DynamicConfiguration configuration() {
        DynamicConfiguration configuration = new StoreBasedDynamicConfiguration.Builder()
                .propertiesResource("security.properties")
                .build();
        return configuration;
    }

    @Test
    public void testRepositoryConfig() {
        RepositoryConfigLoader service = new RepositoryConfigLoader(configuration());
        RepositoryConfig config = service.repositoryConfig;
        Map<String, RepositoryConfigLoader.Group> discoveredRepositoryGroups = service.fetch();
        discoveredRepositoryGroups.values().forEach(g -> {
            LOG.trace("group: {}", g.groupId);
            g.getRepositories().values().forEach(r -> {
                LOG.trace("  repo: {} - {} - {}", r.repoName, r.repoURL, r.readmeURL);
            });
        });
    }
}
