package no.cantara.docsite.domain.snyk;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetCommand;
import no.cantara.docsite.domain.links.SnykIOTestBadgeURL;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.WorkerTask;
import no.cantara.docsite.health.HealthResource;
import no.cantara.docsite.json.JsonbFactory;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Optional;

import static java.net.HttpURLConnection.HTTP_OK;

public class FetchSnykTestTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(FetchSnykTestTask.class);

    private final CacheStore cacheStore;
    private final CacheKey cacheKey;

    public FetchSnykTestTask(DynamicConfiguration configuration, ExecutorService executor, CacheStore cacheStore, CacheKey cacheKey) {
        super(configuration, executor);
        this.cacheStore = cacheStore;
        this.cacheKey = cacheKey;
    }

    SnykTestBadge getBuildStatus(String svgXml) {
        try {
            SAXParserFactory sax = SAXParserFactory.newInstance();
            sax.setNamespaceAware(false);
            sax.setValidating(false);
            XMLReader reader = sax.newSAXParser().getXMLReader();
            Source er = new SAXSource(reader, new InputSource(new StringReader(svgXml)));

            JAXBContext context = JAXBContext.newInstance(SnykTestBadge.class);
            Unmarshaller um = context.createUnmarshaller();
            SnykTestBadge badge = (SnykTestBadge) um.unmarshal(er);
            return badge;

        } catch (ParserConfigurationException | JAXBException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute() {
        SnykIOTestBadgeURL snykURL = new SnykIOTestBadgeURL(cacheKey);
        GetCommand<String> cmd = new GetCommand<>("snykTestStatus", getConfiguration(), Optional.of(this), snykURL.getExternalURL(), HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = cmd.execute();
        HealthResource.instance().markSnykLastSeen();
        if (response.statusCode() == HTTP_OK) {
            String body = response.body();
            SnykTestStatus snykTestStatus = new SnykTestStatus(body, getBuildStatus(body));
            cacheStore.getSnykTestStatus().put(cacheKey, snykTestStatus);

        }
//        else {
//            LOG.warn("{} -- {}", snykURL.getExternalURL(), response.statusCode());
//        }
    }

    @Override
    public String toString() {
        return String.format("%s: %s", getClass().getSimpleName(), JsonbFactory.asCompactString(JsonbFactory.asJsonObject(cacheKey)));
    }

}
