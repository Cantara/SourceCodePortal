package no.cantara.docsite.domain.maven;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.domain.github.contents.RepositoryContents;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.WorkerTask;
import no.cantara.docsite.util.JsonbFactory;
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

public class FetchMavenPOMTask extends WorkerTask  {

    private static final Logger LOG = LoggerFactory.getLogger(FetchMavenPOMTask.class);
    private final CacheStore cacheStore;
    private final CacheKey cacheKey;
    private final String repoContentsURL;

    public FetchMavenPOMTask(DynamicConfiguration configuration, ExecutorService executor, CacheStore cacheStore, CacheKey cacheKey, String repoContentsURL) {
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute() {
        GetGitHubCommand<String> cmd = new GetGitHubCommand<>("githubPage", getConfiguration(), Optional.of(this), String.format(repoContentsURL, "pom.xml", cacheKey.branch), HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = cmd.execute();
        if (GetGitHubCommand.anyOf(response, 200)) {
            RepositoryContents mavenPOMContents = JsonbFactory.instance().fromJson(response.body(), RepositoryContents.class);
            MavenPOM mavenPOM = parse(mavenPOMContents.content);
            cacheStore.getProjects().put(cacheKey, mavenPOM);
        }
    }

}
