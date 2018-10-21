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
            ResourceContext.Resource resource = context.getLast().get();
            assertTrue(resource.match("/index"));
            assertTrue(resource.subMatch("/index"));
            assertFalse(resource.exactMatch("/index/foo"));
        }
        {
            ResourceContext context = new ResourceContext("/index/id");
            ResourceContext.Resource resource = context.getLast().get();
            assertTrue(resource.match("/index/foo"));
            assertTrue(resource.subMatch("/index"));
            assertTrue(resource.subMatch("/index/id"));
            assertTrue(resource.subMatch("/index/id/foo"));
            assertFalse(resource.exactMatch("/index/foo"));
            assertTrue(resource.exactMatch("/index/id"));

        }
        {
            ResourceContext context = new ResourceContext("/index/id/other");
            ResourceContext.Resource resource = context.getLast().get();
            assertTrue(resource.match("/index/id/other"));
            assertTrue(resource.subMatch("/index"));
            assertTrue(resource.subMatch("/index/id"));
            assertTrue(resource.subMatch("/index/id/other"));
            assertFalse(resource.exactMatch("/index/id"));
            assertTrue(resource.exactMatch("/index/id/other"));
        }
        {
            ResourceContext context = new ResourceContext("/index/id/other/id");
            ResourceContext.Resource resource = context.getLast().get();
            assertTrue(resource.match("/index/id/other"));
            assertTrue(resource.subMatch("/index"));
            assertTrue(resource.subMatch("/index/id"));
            assertTrue(resource.subMatch("/index/id/other"));
            assertTrue(resource.subMatch("/index/id/other/id"));
            assertFalse(resource.exactMatch("/index/id/other"));
            assertTrue(resource.exactMatch("/index/id/other/id"));
        }

        {
            ResourceContext context = new ResourceContext("/github/webhook");
            assertTrue(context.getLast().get().exactMatch("/github/webhook"));
            assertTrue(context.getLast().get().id.equals("webhook"));
        }
    }
}
