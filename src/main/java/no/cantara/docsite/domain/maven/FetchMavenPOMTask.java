package no.cantara.docsite.domain.maven;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.domain.github.contents.GitHubRepositoryContents;
import no.cantara.docsite.domain.links.GitHubApiContentsURL;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.WorkerTask;
import no.cantara.docsite.util.CommonUtil;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.json.bind.JsonbBuilder;
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

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;

public class FetchMavenPOMTask extends WorkerTask  {

    private static final Logger LOG = LoggerFactory.getLogger(FetchMavenPOMTask.class);
    private final CacheStore cacheStore;
    private final CacheKey cacheKey;
    private final GitHubApiContentsURL repoContentsURL;

    public FetchMavenPOMTask(DynamicConfiguration configuration, ExecutorService executor, CacheStore cacheStore, CacheKey cacheKey, GitHubApiContentsURL repoContentsURL) {
        super(configuration, executor);
        this.cacheStore = cacheStore;
        this.cacheKey = cacheKey;
        this.repoContentsURL = repoContentsURL;
    }

    public static MavenPOM parse(String xml) {
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
            LOG.error("Error parsing pom.xml: {}\n{}", xml, CommonUtil.captureStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean execute() {
        GetGitHubCommand<String> cmd = new GetGitHubCommand<>("githubPage", configuration(), Optional.of(this), repoContentsURL.getExternalURL("pom.xml", cacheKey.branch), HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = cmd.execute();
        if (response.statusCode() == HTTP_INTERNAL_ERROR) {
            return false;
        }
        if (GetGitHubCommand.anyOf(response, 200)) {
            GitHubRepositoryContents mavenPOMContents = JsonbBuilder.create().fromJson(response.body(), GitHubRepositoryContents.class);
            MavenPOM mavenPOM = parse(mavenPOMContents.content);
            cacheStore.getMavenProjects().put(cacheKey, mavenPOM);
        }
        return true;
    }

}
