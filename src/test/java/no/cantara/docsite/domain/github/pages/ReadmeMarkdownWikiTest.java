package no.cantara.docsite.domain.github.pages;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReadmeMarkdownWikiTest {

    @Test
    public void testAsciidocRenderer() throws IOException {
        try (InputStream is = ClassLoader.getSystemResourceAsStream("github/README.md")) {
            String adoc = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
            ReadmeMDWiki doc = new ReadmeMDWiki(adoc);
            System.out.println("md: " + doc);
        }
    }

    @Test
    public void testPrintNodes() {
        List<Extension> extensions = Arrays.asList(TablesExtension.create());
        Parser parser = Parser.builder().extensions(extensions).build();
        Node document = parser.parse(ReadmeBadgeMDFixture.BADGE_MARKDOWN);
        PrintVisitor visitor = new PrintVisitor();
        // look for listItem -> para -> image: Image{destination ~ jenkins).. We need a url list match badge images..
//        document.accept(visitor);

    }

    static class PrintVisitor extends AbstractVisitor {
        private static final Logger LOG = LoggerFactory.getLogger(PrintVisitor.class);

        @Override
        public void visit(BlockQuote blockQuote) {
            LOG.trace("blockQuote: {}", blockQuote);
            visitChildren(blockQuote);
        }

        @Override
        public void visit(BulletList bulletList) {
            LOG.trace("bulletList: {}", bulletList);
            visitChildren(bulletList);
        }

        @Override
        public void visit(Code code) {
            LOG.trace("code: {}", code);
            visitChildren(code);
        }

        @Override
        public void visit(Document document) {
            LOG.trace("document: {}", document);
            visitChildren(document);
        }

        @Override
        public void visit(Emphasis emphasis) {
            LOG.trace("emphasis: {}", emphasis);
            visitChildren(emphasis);
        }

        @Override
        public void visit(FencedCodeBlock fencedCodeBlock) {
            LOG.trace("fencedCodeBlock: {}", fencedCodeBlock);
            visitChildren(fencedCodeBlock);
        }

        @Override
        public void visit(HardLineBreak hardLineBreak) {
            LOG.trace("hardLineBreak: {}", hardLineBreak);
            visitChildren(hardLineBreak);
        }

        @Override
        public void visit(Heading heading) {
            LOG.trace("heading: {}", heading);
            visitChildren(heading);
        }

        @Override
        public void visit(ThematicBreak thematicBreak) {
            LOG.trace("thematicBreak: {}", thematicBreak);
            visitChildren(thematicBreak);
        }

        @Override
        public void visit(HtmlInline htmlInline) {
            LOG.trace("htmlInline: {}", htmlInline);
            visitChildren(htmlInline);
        }

        @Override
        public void visit(HtmlBlock htmlBlock) {
            LOG.trace("htmlBlock: {}", htmlBlock);
            visitChildren(htmlBlock);
        }

        @Override
        public void visit(Image image) {
            LOG.trace("image: {}", image);
            visitChildren(image);
        }

        @Override
        public void visit(IndentedCodeBlock indentedCodeBlock) {
            LOG.trace("indentedCodeBlock: {}", indentedCodeBlock);
            visitChildren(indentedCodeBlock);
        }

        @Override
        public void visit(Link link) {
            LOG.trace("link: {}", link);
            visitChildren(link);
        }

        @Override
        public void visit(ListItem listItem) {
            LOG.trace("listItem: {}", listItem);
            visitChildren(listItem);
        }

        @Override
        public void visit(OrderedList orderedList) {
            LOG.trace("orderedList: {}", orderedList);
            visitChildren(orderedList);
        }

        @Override
        public void visit(Paragraph paragraph) {
            LOG.trace("paragraph: {}", paragraph);
            visitChildren(paragraph);
        }

        @Override
        public void visit(SoftLineBreak softLineBreak) {
            LOG.trace("softLineBreak: {}", softLineBreak);
            visitChildren(softLineBreak);
        }

        @Override
        public void visit(StrongEmphasis strongEmphasis) {
            LOG.trace("strongEmphasis: {}", strongEmphasis);
            visitChildren(strongEmphasis);
        }

        @Override
        public void visit(Text text) {
            LOG.trace("text: {}", text);
            visitChildren(text);
        }

        @Override
        public void visit(CustomBlock customBlock) {
            LOG.trace("customBlock: {}", customBlock);
            visitChildren(customBlock);
        }

        @Override
        public void visit(CustomNode customNode) {
            LOG.trace("customNode: {}", customNode);
            visitChildren(customNode);
        }
    }
}
