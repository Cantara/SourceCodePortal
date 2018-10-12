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
    requires hystrix.core;
    requires undertow.core;
    requires thymeleaf;
    requires thymeleaf.layout.dialect;
    requires thymeleaf.expression.processor;
    requires org.codehaus.groovy;
    requires org.commonmark;
    requires org.commonmark.ext.gfm.tables;
    requires asciidoctorj;
    requires no.ssb.config;

    exports no.cantara.docsite;
    exports no.cantara.docsite.controller;
    exports no.cantara.docsite.executor;
    exports no.cantara.docsite.services;
    exports no.cantara.docsite.model.maven;
    exports no.cantara.docsite.model.github.pull;
    exports no.cantara.docsite.model.github.push;
    exports no.cantara.docsite.util;
    exports no.cantara.docsite.web;
}
