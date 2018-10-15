package no.cantara.docsite.domain.github.pages;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.Arrays;
import java.util.List;

public class ReadmeMDWiki {

    public final String html;

    public ReadmeMDWiki(String commonmarkWiki) {
        List<Extension> extensions = Arrays.asList(TablesExtension.create());
        Parser parser = Parser.builder().extensions(extensions).build();
        Node document = parser.parse(commonmarkWiki);
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
        html = renderer.render(document);
    }

    @Override
    public String toString() {
        return "ReadmeMDWiki{" +
                "html='" + html + '\'' +
                '}';
    }
}
