package no.cantara.docsite.domain.renderer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentRenderer.class);

    public static String render(String filename, String content) {
        String renderedHtml = null;
        if (filename.endsWith(".md")) {
            ReadmeMDWiki wiki = new ReadmeMDWiki(content);
            renderedHtml = wiki.html;

        } else if (filename.endsWith(".adoc")) {
            ReadmeAsciidocWiki wiki = new ReadmeAsciidocWiki(content);

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
            LOG.error("Unable to parse content: {}", filename);
        }

        return renderedHtml;
    }
}
