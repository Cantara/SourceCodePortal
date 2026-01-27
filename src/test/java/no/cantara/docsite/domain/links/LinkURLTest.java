package no.cantara.docsite.domain.links;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LinkURLTest {

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

        list.add(new GitHubRawRepoURL(ScmRepository.of(configuration,
                CacheRepositoryKey.of("Cantara", "repo1", "master", "group1", false),
                "dn", "cd", new LinkedHashMap<>(),"id", "desc", "group", "Apache-2.0", "http://example.com")));

        CacheRepositoryKey key1 = CacheRepositoryKey.of("Cantara", "repo1", "master", "group1", false);
        list.add(new GitHubApiReadmeURL(key1.asCacheKey(),
                ScmRepository.of(configuration, key1,"dn", "cd", new LinkedHashMap<>(),"id", "desc", "group", "Apache-2.0", "http://example.com")));

        CacheRepositoryKey key2 = CacheRepositoryKey.of("Cantara", "repo1", "master", "group1", false);
        list.add(new GitHubApiContentsURL(key2.asCacheKey(), ScmRepository.of(configuration, key2, "dn", "cd", new LinkedHashMap<>(),"id", "desc", "group", "Apache-2.0", "http://example.com")));

        list.add(new GitHubHtmlURL("http://example.com"));

        assertEquals(list.stream().filter(externalURL -> externalURL.isGenericOf(String.class)).count(), 1);
        assertEquals(list.stream().filter(externalURL -> externalURL.isGenericOf(CacheKey.class)).count(), 2);
        assertEquals(list.stream().filter(externalURL -> externalURL.isGenericOf(ScmRepository.class)).count(), 1);
    }
}
