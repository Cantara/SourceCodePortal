package no.cantara.docsite.domain.snyk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;
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

public class SnykTestBadgeTest {

private static final Logger LOG = LoggerFactory.getLogger(SnykTestBadgeTest.class);

    final String SVG = "\n" +
            "<svg id=\"snyk-badge\" data-package=\"undefined@undefined\" xmlns=\"http://www.w3.org/2000/svg\" width=\"110\" height=\"20\">\n" +
            "  <linearGradient id=\"b\" x2=\"0\" y2=\"100%\">\n" +
            "    <stop offset=\"0\" stop-color=\"#bbb\" stop-opacity=\".1\"/>\n" +
            "    <stop offset=\"1\" stop-opacity=\".1\"/>\n" +
            "  </linearGradient>\n" +
            "  <mask id=\"a\">\n" +
            "    <rect width=\"110\" height=\"20\" rx=\"3\" fill=\"#fff\"/>\n" +
            "  </mask>\n" +
            "  <g mask=\"url(#a)\">\n" +
            "    <path fill=\"#555\" d=\"M0 0h90v20H0z\"/>\n" +
            "    <path fill=\"#e05d44\" d=\"M90 0h110v20H90z\"/>\n" +
            "    <path fill=\"url(#b)\" d=\"M0 0h110v20H0z\"/>\n" +
            "  </g>\n" +
            "  <g fill=\"#fff\" text-anchor=\"middle\" font-family=\"DejaVu Sans,Verdana,Geneva,sans-serif\" font-size=\"11\">\n" +
            "    <text x=\"45\" y=\"15\" fill=\"#010101\" fill-opacity=\".3\">vulnerabilities</text>\n" +
            "    <text x=\"45\" y=\"14\">vulnerabilities</text>\n" +
            "    <text x=\"100\" y=\"15\" fill=\"#010101\" fill-opacity=\".3\">1</text>\n" +
            "    <text x=\"100\" y=\"14\">1</text>\n" +
            "  </g>\n" +
            "</svg>\n";

    @Test
    public void testName() throws ParserConfigurationException, SAXException, JAXBException {
        SAXParserFactory sax = SAXParserFactory.newInstance();
        sax.setNamespaceAware(false);
        sax.setValidating(false);
        XMLReader reader = sax.newSAXParser().getXMLReader();
        Source er = new SAXSource(reader, new InputSource(new StringReader(SVG)));

        JAXBContext context = JAXBContext.newInstance(SnykTestBadge.class);
        Unmarshaller um = context.createUnmarshaller();
        SnykTestBadge badge = (SnykTestBadge) um.unmarshal(er);
        LOG.trace("{}", badge);
    }
}
