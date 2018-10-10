module cantara.docsite {
    requires jdk.unsupported;
    requires java.base;
    requires java.logging;
    requires java.xml.bind;
    requires java.net.http;
    requires org.slf4j;
    requires jul_to_slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires java.json;
    requires java.json.bind;
    requires cache.api;
    requires org.jsoup;
    requires undertow.core;
    requires thymeleaf;
    requires thymeleaf.layout.dialect;
    requires thymeleaf.expression.processor;
    requires org.codehaus.groovy;
    requires org.commonmark;
    requires org.commonmark.ext.gfm.tables;
    requires asciidoctorj;
    requires no.ssb.config;
}
