package no.cantara.docsite.web;

import no.cantara.docsite.test.client.ResponseHelper;
import no.cantara.docsite.test.client.TestClient;
import no.cantara.docsite.test.server.TestServerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static org.testng.Assert.assertNotNull;

@Listeners(TestServerListener.class)
public class ThymeleafTest {

    private static final Logger LOG = LoggerFactory.getLogger(ThymeleafTest.class);

    @Inject
    TestClient client;

    @Test
    public void testThymeleafEngineProducer() {
        assertNotNull(DefaultTemplateEngine.INSTANCE);
    }

    @Test
    public void testThymeleafViewEngine() {
        ResponseHelper<String> response = client.get("/docs/home");
        LOG.trace("html:\n{}", response.expect200Ok().body());

    }
}
