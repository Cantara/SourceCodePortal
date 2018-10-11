package no.cantara.docsite.github;

import no.cantara.docsite.client.HttpRequests;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;
import java.net.http.HttpResponse;

public class MavenPOMTest {

    @Test
    public void testName() throws JAXBException, ParserConfigurationException, SAXException {
        HttpResponse<String> response = HttpRequests.get("https://raw.githubusercontent.com/Cantara/SourceCodePortal/master/pom.xml");

        SAXParserFactory sax = SAXParserFactory.newInstance();
        sax.setNamespaceAware(false);
        XMLReader reader = sax.newSAXParser().getXMLReader();
        Source er = new SAXSource(reader, new InputSource(new StringReader(response.body())));

        JAXBContext context = JAXBContext.newInstance(MavenPom.class);
        Unmarshaller um = context.createUnmarshaller();
        MavenPom mavenPom = (MavenPom) um.unmarshal(er);

        System.out.println("mavenPOM: " + mavenPom);
    }

}
