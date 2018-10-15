package no.cantara.docsite.domain.maven;

import no.cantara.docsite.client.HttpRequests;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.net.http.HttpResponse;

public class MavenPOMTest {

    @Test
    public void testMavenPOM() throws JAXBException, ParserConfigurationException, SAXException {
        HttpResponse<String> response = HttpRequests.get("https://raw.githubusercontent.com/statisticsnorway/distributed-saga/master/pom.xml");

        MavenPOMParser parser = new MavenPOMParser();
        MavenPOM mavenPom = parser.parse(response.body());

        System.out.println("mavenPOM: " + mavenPom);
    }

}
