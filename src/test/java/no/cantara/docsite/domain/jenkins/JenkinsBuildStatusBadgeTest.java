package no.cantara.docsite.domain.jenkins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class JenkinsBuildStatusBadgeTest {

    private static final Logger LOG = LoggerFactory.getLogger(JenkinsBuildStatusBadgeTest.class);

    final String SVG = "\n" +
//            "<?xml version=\"1.0\"?>\n" +
            "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"90\" height=\"20\">\n" +
            "  <linearGradient id=\"a\" x2=\"0\" y2=\"100%\">\n" +
            "    <stop offset=\"0\" stop-color=\"#bbb\" stop-opacity=\".1\"/>\n" +
            "    <stop offset=\"1\" stop-opacity=\".1\"/>\n" +
            "  </linearGradient>\n" +
            "  <rect rx=\"3\" width=\"90\" height=\"20\" fill=\"#555\"/>\n" +
            "  <rect rx=\"3\" x=\"37\" width=\"53\" height=\"20\" fill=\"#4c1\"/>\n" +
            "  <path fill=\"#4c1\" d=\"M37 0h4v20h-4z\"/>\n" +
            "  <rect rx=\"3\" width=\"90\" height=\"20\" fill=\"url(#a)\"/>\n" +
            "  <g fill=\"#fff\" text-anchor=\"middle\" font-family=\"DejaVu Sans,Verdana,Geneva,sans-serif\" font-size=\"11\">\n" +
            "    <text x=\"19.5\" y=\"15\" fill=\"#010101\" fill-opacity=\".3\">build</text>\n" +
            "    <text x=\"19.5\" y=\"14\">build</text>\n" +
            "    <text x=\"62.5\" y=\"15\" fill=\"#010101\" fill-opacity=\".3\">passing</text>\n" +
            "    <text x=\"62.5\" y=\"14\">passing</text>\n" +
            "  </g>\n" +
            "</svg>\n";

    @Test
    public void testName() throws ParserConfigurationException, SAXException, JAXBException {
        SAXParserFactory sax = SAXParserFactory.newInstance();
        sax.setNamespaceAware(false);
        sax.setValidating(false);
        XMLReader reader = sax.newSAXParser().getXMLReader();
        Source er = new SAXSource(reader, new InputSource(new StringReader(SVG)));

        JAXBContext context = JAXBContext.newInstance(JenkinsBuildStatusBadge.class);
        Unmarshaller um = context.createUnmarshaller();
        JenkinsBuildStatusBadge badge = (JenkinsBuildStatusBadge) um.unmarshal(er);
        LOG.trace("{}", badge);
    }
}
