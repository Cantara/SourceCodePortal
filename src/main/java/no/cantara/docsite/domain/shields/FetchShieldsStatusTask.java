package no.cantara.docsite.domain.shields;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetShieldsCommand;
import no.cantara.docsite.domain.links.LinkURL;
import no.cantara.docsite.domain.links.ShieldsIOGitHubIssuesURL;
import no.cantara.docsite.domain.links.ShieldsIOGroupCommitURL;
import no.cantara.docsite.domain.links.ShieldsIOGroupReleaseURL;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.cantara.docsite.domain.scm.ScmRepositoryService;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.WorkerTask;
import no.cantara.docsite.json.JsonbFactory;
import no.cantara.docsite.util.CommonUtil;
import no.ssb.config.DynamicConfiguration;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
import java.io.IOException;
import java.io.StringReader;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.net.HttpURLConnection.HTTP_OK;

public class FetchShieldsStatusTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(FetchShieldsStatusTask.class);

    private final CacheStore cacheStore;
    private final CacheKey cacheKey;
    private final Fetch fetch;

    public FetchShieldsStatusTask(DynamicConfiguration configuration, ExecutorService executor, CacheStore cacheStore, CacheKey cacheKey, Fetch fetch) {
        super(configuration, executor);
        this.cacheStore = cacheStore;
        this.cacheKey = cacheKey;
        this.fetch = fetch;
    }

    ShieldsBadge getBuildStatus(String svgXml) {
        try {
            SAXParserFactory sax = SAXParserFactory.newInstance();
            sax.setNamespaceAware(false);
            sax.setValidating(false);
            XMLReader reader = sax.newSAXParser().getXMLReader();
            Source er = new SAXSource(reader, new InputSource(new StringReader(svgXml)));

            JAXBContext context = JAXBContext.newInstance(ShieldsBadge.class);
            Unmarshaller um = context.createUnmarshaller();
            ShieldsBadge badge = (ShieldsBadge) um.unmarshal(er);
            return badge;

        } catch (ParserConfigurationException | JAXBException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute() {
        ScmRepository scmRepository = new ScmRepositoryService(cacheStore).getFirst(cacheKey);
        LinkURL shieldsURL;
        if (fetch == Fetch.ISSUES) {
            shieldsURL = new ShieldsIOGitHubIssuesURL(scmRepository);
        } else if (fetch == Fetch.COMMITS) {
            shieldsURL = new ShieldsIOGroupCommitURL(scmRepository);
        } else if (fetch == Fetch.RELEASES) {
            shieldsURL = new ShieldsIOGroupReleaseURL(scmRepository);
        } else {
            throw new UnsupportedOperationException();
        }

        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(shieldsURL.getExternalURL());
            CloseableHttpResponse response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HTTP_OK) {
                String body;
                try {
                    HttpEntity entity = response.getEntity();
                    body = new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8);
                    EntityUtils.consume(entity);
                } finally {
                    response.close();
                }

                ShieldsStatus shieldsStatus = new ShieldsStatus(body, getBuildStatus(body));

                if (fetch == Fetch.ISSUES) {
                    cacheStore.getSheildIssuesStatus().put(cacheKey, shieldsStatus);
                } else if (fetch == Fetch.COMMITS) {
                    cacheStore.getSheildCommitsStatus().put(cacheKey, shieldsStatus);
                } else if (fetch == Fetch.RELEASES) {
                    cacheStore.getShieldReleasesStatus().put(cacheKey, shieldsStatus);
                } else {
                    throw new UnsupportedOperationException();
                }

            } else {
                LOG.warn("{} -- {}", shieldsURL.getExternalURL(), response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            LOG.error("Error fetching shields badge: {}", CommonUtil.captureStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    //    @Override
    public void executeCommand() {
        ScmRepository scmRepository = new ScmRepositoryService(cacheStore).getFirst(cacheKey);
        LinkURL shieldsURL;
        if (fetch == Fetch.ISSUES) {
            shieldsURL = new ShieldsIOGitHubIssuesURL(scmRepository);
        } else if (fetch == Fetch.COMMITS) {
            shieldsURL = new ShieldsIOGroupCommitURL(scmRepository);
        } else if (fetch == Fetch.RELEASES) {
            shieldsURL = new ShieldsIOGroupReleaseURL(scmRepository);
        } else {
            throw new UnsupportedOperationException();
        }

        GetShieldsCommand<String> cmd = new GetShieldsCommand<>("shieldsStatus", getConfiguration(), Optional.of(this), shieldsURL.getExternalURL(), HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = cmd.execute();

        if (response.statusCode() == HTTP_OK) {
            String body = response.body();
            ShieldsStatus shieldsStatus = new ShieldsStatus(body, getBuildStatus(body));

            if (fetch == Fetch.ISSUES) {
                cacheStore.getSheildIssuesStatus().put(cacheKey, shieldsStatus);
            } else if (fetch == Fetch.COMMITS) {
                cacheStore.getSheildCommitsStatus().put(cacheKey, shieldsStatus);
            } else if (fetch == Fetch.RELEASES) {
                cacheStore.getShieldReleasesStatus().put(cacheKey, shieldsStatus);
            } else {
                throw new UnsupportedOperationException();
            }

        } else {
            LOG.warn("{} -- {}", shieldsURL.getExternalURL(), response.statusCode());
        }
    }

    @Override
    public String toString() {
        return String.format("%s: %s", getClass().getSimpleName(), JsonbFactory.asCompactString(JsonbFactory.asJsonObject(cacheKey)));
    }

    public enum Fetch {
        ISSUES,
        COMMITS,
        RELEASES;
    }

}
