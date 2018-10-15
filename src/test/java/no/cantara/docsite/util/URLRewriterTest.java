package no.cantara.docsite.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class URLRewriterTest {

    private static final Logger LOG = LoggerFactory.getLogger(URLRewriterTest.class);

    @Test
    public void testBaseURI() {
        URLRewriter url = new URLRewriter("https://raw.githubusercontent.com/Cantara/Whydah-UserAdminService/master/pom.xml");
        String uri = url.toURI();
        LOG.trace("URI: {}", uri);
    }

}
