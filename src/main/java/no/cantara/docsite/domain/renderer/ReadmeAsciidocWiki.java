package no.cantara.docsite.domain.renderer;

import org.asciidoctor.Asciidoctor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;

public class ReadmeAsciidocWiki {

    public final String html;

    public ReadmeAsciidocWiki(String asciidocWiki) {
        Asciidoctor asciidoctor = Asciidoctor.Factory.create(); // ClassLoader.getSystemClassLoader()
        byte[] bytes = asciidocWiki.getBytes();
        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(bytes))) {
            StringWriter writer = new StringWriter();
            asciidoctor.convert(reader, writer, new HashMap<>());
            html = writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "ReadmeAsciidocWiki{" +
                "html='" + html + '\'' +
                '}';
    }
}
