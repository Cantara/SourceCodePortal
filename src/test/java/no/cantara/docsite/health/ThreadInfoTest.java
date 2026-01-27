package no.cantara.docsite.health;

import no.cantara.docsite.json.JsonbFactory;
import org.junit.jupiter.api.Test;

public class ThreadInfoTest {

    @Test
    public void testThreadInfo() {
        System.out.println(JsonbFactory.prettyPrint(HealthResource.instance().getThreadInfo().build()));
    }
}
