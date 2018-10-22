package no.cantara.docsite.domain.github.pages;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.commands.GetGitHubCommand;
import no.cantara.docsite.domain.github.contents.RepositoryContents;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.executor.WorkerTask;
import no.ssb.config.DynamicConfiguration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.JsonbBuilder;
import java.net.http.HttpResponse;
import java.util.Optional;

public class FetchPageTask extends WorkerTask {

    private static final Logger LOG = LoggerFactory.getLogger(FetchPageTask.class);
    private final CacheStore cacheStore;
    private final CacheKey cacheKey;
    private final String repoReadmeURL;

    public FetchPageTask(DynamicConfiguration configuration, ExecutorService executor, CacheStore cacheStore, CacheKey cacheKey, String repoReadmeURL) {
        super(configuration, executor);
        this.cacheStore = cacheStore;
        this.cacheKey = cacheKey;
        this.repoReadmeURL = repoReadmeURL;
    }

    @Override
    public void execute() {
        GetGitHubCommand<String> cmd = new GetGitHubCommand<>("githubPage", getConfiguration(), Optional.of(this), repoReadmeURL, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = cmd.execute();
        if (GetGitHubCommand.anyOf(response, 200)) {
            RepositoryContents readmeContents = JsonbBuilder.create().fromJson(response.body(), RepositoryContents.class);

            String renderedHtml = null;
            if (readmeContents.name.endsWith(".md")) {
                ReadmeMDWiki wiki = new ReadmeMDWiki(readmeContents.content);
                renderedHtml = wiki.html;

            } else if (readmeContents.name.endsWith(".adoc")) {
                ReadmeAsciidocWiki wiki = new ReadmeAsciidocWiki(readmeContents.content);

                Document doc = Jsoup.parse(wiki.html);
                {
                    Elements el = doc.select(".language-xml");
                    for (Element e : el) {
                        e.parent().addClass("prettyprint");
                    }
                }
                {
                    Elements el = doc.select(".language-java");
                    for (Element e : el) {
                        e.parent().addClass("prettyprint");
                    }
                }
                renderedHtml = doc.body().html();

            } else {
                LOG.error("Unable to parse content: {}", readmeContents.name);
            }
            readmeContents.renderedHtml = renderedHtml;

            cacheStore.getPages().put(cacheKey, readmeContents);
        } else {
            LOG.warn("Resource not found: {} ({})", response.uri(), response.statusCode());
        }
    }
}
