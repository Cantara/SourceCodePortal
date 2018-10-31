package no.cantara.docsite.domain.links;

import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class ExternalURLTest {

    static DynamicConfiguration configuration() {
        DynamicConfiguration configuration = new StoreBasedDynamicConfiguration.Builder()
                .propertiesResource("application-defaults.properties")
                .propertiesResource("security.properties")
                .propertiesResource("application.properties")
                .build();
        return configuration;
    }

    @Test
    public void testURLs() {
        DynamicConfiguration configuration = configuration();
        List<LinkURL<?>> list = new ArrayList<>();

        list.add(new GitHubRawRepoURL(ScmRepository.of(configuration, CacheRepositoryKey.of("Cantara", "repo1", "master", "group1", false),
                "dn", "cd", "id", "desc", "group", "http://example.com")));

        list.add(new GitHubApiReadmeURL(ScmRepository.of(configuration, CacheRepositoryKey.of("Cantara", "repo1", "master", "group1", false),
                "dn", "cd", "id", "desc", "group", "http://example.com")));

        list.add(new GitHubApiContentsURL(ScmRepository.of(configuration, CacheRepositoryKey.of("Cantara", "repo1", "master", "group1", false),
                "dn", "cd", "id", "desc", "group", "http://example.com")));

        list.add(new GitHubHtmlURL("http://example.com"));

        assertEquals(list.stream().filter(externalURL -> externalURL.isGenericOf(String.class)).count(), 1);
        assertEquals(list.stream().filter(externalURL -> externalURL.isGenericOf(ScmRepository.class)).count(), 3);
    }
}
