package no.cantara.docsite.domain.maven;

import no.cantara.docsite.client.HttpRequests;
import org.testng.annotations.Test;

import java.net.http.HttpResponse;

import static org.testng.Assert.assertNotNull;

public class MavenPOMTest {

    @Test
    public void testMavenPOM() {
        HttpResponse<String> response = HttpRequests.get("https://raw.githubusercontent.com/statisticsnorway/distributed-saga/master/pom.xml");

        MavenPOM mavenPom = FetchMavenPOMTask.parse(response.body());
        assertNotNull(mavenPom);
    }

}
