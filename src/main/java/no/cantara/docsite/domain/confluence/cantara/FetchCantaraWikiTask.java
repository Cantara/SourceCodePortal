package no.cantara.docsite.domain.confluence.cantara;

import no.cantara.docsite.cache.CacheCantaraWikiKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.HttpGetCommand;
import no.cantara.docsite.domain.github.pages.ReadmeMDWiki;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.WorkerTask;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.http.HttpResponse;
import java.util.Optional;

import static java.net.HttpURLConnection.HTTP_OK;

public class FetchCantaraWikiTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(FetchCantaraWikiTask.class);
    private final CacheStore cacheStore;
    private final CacheCantaraWikiKey cacheKey;

    public FetchCantaraWikiTask(DynamicConfiguration configuration, ExecutorService executor, CacheStore cacheStore, CacheCantaraWikiKey cacheKey) {
        super(configuration, executor);
        this.cacheStore = cacheStore;
        this.cacheKey = cacheKey;
    }

    @Override
    public void execute() {
        HttpGetCommand<String> cmd = new HttpGetCommand<>("cantaraWiki", getConfiguration(), Optional.of(this),
                String.format("https://wiki.cantara.no/rest/prototype/1/content/%s", cacheKey.contentId), HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = cmd.execute();
        LOG.info("1: {}", response.statusCode());
        if (response.statusCode() == HTTP_OK) {
            String xml = response.body();
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document document = dBuilder.parse(new InputSource(new StringReader(xml)));
                Element documentElement = document.getDocumentElement();
                String body = documentElement.getElementsByTagName("body").item(0).getTextContent();
                body = body.replace("h1.", "#").replace("h2.", "##").replace("h3.", "###")
                        .replace("{section}", "").replace("{column}", "").replace("{gallery}", "")
                        .replace("||", "|");

                // parse this: [I'm an inline-style link|https://www.google.com) to that: [I'm an inline-style link](https://www.google.com)
                ReadmeMDWiki wiki = new ReadmeMDWiki(body);
                LOG.info("Cache Cantara: {}", wiki.html);
                cacheStore.getCantaraWiki().put(cacheKey, wiki.html);

            } catch (ParserConfigurationException | SAXException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
