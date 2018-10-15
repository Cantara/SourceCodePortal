package no.cantara.docsite.domain.maven;

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

public class MavenPOMParser {

    public MavenPOM parse(String xml) {
        try {
            SAXParserFactory sax = SAXParserFactory.newInstance();
            sax.setNamespaceAware(false);
            XMLReader reader = sax.newSAXParser().getXMLReader();
            Source er = new SAXSource(reader, new InputSource(new StringReader(xml)));

            JAXBContext context = JAXBContext.newInstance(MavenPOM.class);
            Unmarshaller um = context.createUnmarshaller();
            MavenPOM mavenPom = (MavenPOM) um.unmarshal(er);
            return mavenPom;
        } catch (ParserConfigurationException | SAXException | JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
