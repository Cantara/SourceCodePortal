package no.cantara.docsite.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ResourceContextTest {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceContextTest.class);

    @Test
    public void testResourceContexts() {
        {
            ResourceContext context = new ResourceContext("/index");
            assertTrue(context.match("/index"));
            assertTrue(context.subMatch("/index"));
            assertFalse(context.exactMatch("/index/foo"));
        }
        {
            ResourceContext context = new ResourceContext("/index/id");
            assertTrue(context.match("/index/foo"));
            assertTrue(context.subMatch("/index"));
            assertTrue(context.subMatch("/index/id"));
            assertTrue(context.subMatch("/index/id/foo"));
            assertFalse(context.exactMatch("/index/foo"));
            assertTrue(context.exactMatch("/index/id"));

        }
        {
            ResourceContext context = new ResourceContext("/index/id/other");
            assertTrue(context.match("/index/id/other"));
            assertTrue(context.subMatch("/index"));
            assertTrue(context.subMatch("/index/id"));
            assertTrue(context.subMatch("/index/id/other"));
            assertFalse(context.exactMatch("/index/id"));
            assertTrue(context.exactMatch("/index/id/other"));
        }
        {
            ResourceContext context = new ResourceContext("/index/id/other/id");
            assertTrue(context.match("/index/id/other"));
            assertTrue(context.subMatch("/index"));
            assertTrue(context.subMatch("/index/id"));
            assertTrue(context.subMatch("/index/id/other"));
            assertTrue(context.subMatch("/index/id/other/id"));
            assertFalse(context.exactMatch("/index/id/other"));
            assertTrue(context.exactMatch("/index/id/other/id"));
        }

        {
            ResourceContext context = new ResourceContext("/github/webhook");
            assertTrue(context.exactMatch("/github/webhook"));
            assertTrue(context.getLast().get().id.equals("webhook"));
        }

        {
            ResourceContext context = new ResourceContext("/favicon.ico");
            assertTrue(context.exactMatch("/favicon.ico"));
        }
    }
}
