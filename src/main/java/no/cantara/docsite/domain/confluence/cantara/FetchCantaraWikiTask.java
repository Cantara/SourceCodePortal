package no.cantara.docsite.domain.confluence.cantara;

import no.cantara.docsite.cache.CacheCantaraWikiKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.HttpGetCommand;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.WorkerTask;
import no.ssb.config.DynamicConfiguration;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                String.format("https://wiki.cantara.no/pages/viewpage.action?pageId=%s", cacheKey.contentId), HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = cmd.execute();
        if (response.statusCode() == HTTP_OK) {
            String html = response.body();
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            org.jsoup.nodes.Element body = doc.normalise().body();
            body.select("a").forEach(el -> {
                if (el.attributes().hasKey("href") && !"#".equals(el.attributes().get("href")) && !el.attributes().get("href").startsWith("http")) {
                    el.attr("href", "https://wiki.cantara.no" + el.attributes().get("href"));
                }
            });
            body.select("img").forEach(el -> {
                if (el.attributes().hasKey("src") && !"#".equals(el.attributes().get("src")) && !el.attributes().get("src").startsWith("http")) {
                    el.attr("src", "https://wiki.cantara.no" + el.attributes().get("src"));
                }
            });
            body.getElementsByClass("logo global custom").remove();
            String title = body.getElementById("title-heading").html();
            String content = body.getElementsByClass("wiki-content").html();
            String rendered = String.format("<h1>%s</h1>%s", title, content);
//            LOG.info("Cache Cantara:\n{}", rendered);
            cacheStore.getCantaraWiki().put(cacheKey, rendered);
        }
    }
}
