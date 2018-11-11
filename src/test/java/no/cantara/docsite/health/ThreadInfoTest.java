package no.cantara.docsite.health;

import no.cantara.docsite.json.JsonbFactory;
import org.testng.annotations.Test;

public class ThreadInfoTest {

    @Test
    public void testThreadInfo() {
        System.out.println(JsonbFactory.prettyPrint(HealthResource.instance().getThreadInfo().build()));
    }
}
