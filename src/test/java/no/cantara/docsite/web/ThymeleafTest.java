package no.cantara.docsite.web;

import no.cantara.docsite.test.client.ResponseHelper;
import no.cantara.docsite.test.client.TestClient;
import no.cantara.docsite.test.server.TestServerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(TestServerExtension.class)
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
        ResponseHelper<String> response = client.get("/contents/home");
//        LOG.trace("html:\n{}", response.expect200Ok().body());

    }
}
