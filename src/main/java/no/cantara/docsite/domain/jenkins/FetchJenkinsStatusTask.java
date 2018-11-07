package no.cantara.docsite.domain.jenkins;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetCommand;
import no.cantara.docsite.domain.config.RepoConfig;
import no.cantara.docsite.domain.links.JenkinsURL;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.WorkerTask;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

public class FetchJenkinsStatusTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(FetchJenkinsStatusTask.class);

    private final CacheStore cacheStore;
    private final CacheKey cacheKey;

    public FetchJenkinsStatusTask(DynamicConfiguration configuration, ExecutorService executor, CacheStore cacheStore, CacheKey cacheKey) {
        super(configuration, executor);
        this.cacheStore = cacheStore;
        this.cacheKey = cacheKey;
    }

    JenkinsBuildStatusBadge getBuildStatus(String svgXml) {
        try {
            SAXParserFactory sax = SAXParserFactory.newInstance();
            sax.setNamespaceAware(false);
            sax.setValidating(false);
            XMLReader reader = sax.newSAXParser().getXMLReader();
            Source er = new SAXSource(reader, new InputSource(new StringReader(svgXml)));

            JAXBContext context = JAXBContext.newInstance(JenkinsBuildStatusBadge.class);
            Unmarshaller um = context.createUnmarshaller();
            JenkinsBuildStatusBadge badge = (JenkinsBuildStatusBadge) um.unmarshal(er);
            return badge;

        } catch (ParserConfigurationException | JAXBException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute() {
        String jobPrefix = "";
        // TODO this is a workaround because of wrong modelling
        {
            Set<CacheRepositoryKey> repo = cacheStore.getCacheRepositoryKeys(cacheKey);
            if (repo != null && repo.iterator().hasNext()) {
                CacheRepositoryKey cacheRepositoryKey = repo.iterator().next();
                RepoConfig.Jenkins jenkins = cacheStore.getRepositories().get(cacheRepositoryKey).config.getService(RepoConfig.Jenkins.class);
                if (jenkins != null) {
                    jobPrefix = jenkins.jenkinsPrefix;
                }
            }
        }

        JenkinsURL jenkinsURL = new JenkinsURL(getConfiguration(), cacheKey, jobPrefix);
        GetCommand<String> cmd = new GetCommand<>("jenkinsStatus", getConfiguration(), Optional.of(this), jenkinsURL.getExternalURL(), HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = cmd.execute();
        if (response.statusCode() == HTTP_OK) {
            String body = response.body();
            JenkinsBuildStatus buildStatus = new JenkinsBuildStatus(body, getBuildStatus(body));
            cacheStore.getJenkinsBuildStatus().put(cacheKey, buildStatus);

        } else {
            LOG.warn("{} -- {}", jenkinsURL.getExternalURL(), response.statusCode());
        }
    }

    @Override
    public String toString() {
        return String.format("%s: %s", getClass().getSimpleName(), JsonbFactory.asCompactString(JsonbFactory.asJsonObject(cacheKey)));
    }

}
