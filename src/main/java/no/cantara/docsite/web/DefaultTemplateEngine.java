package no.cantara.docsite.web;

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

class DefaultTemplateEngine {

    static final TemplateEngine INSTANCE;
    static final String RESOURCE_PATH = "/META-INF/views";
    static final String CLASS_RESOURCE_PATH = RESOURCE_PATH.substring(1);

    static {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver(classLoader);
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCacheable(false);
        resolver.setPrefix(RESOURCE_PATH);
        resolver.setSuffix(".html");
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        templateEngine.addDialect(new LayoutDialect());
        INSTANCE = templateEngine;
    }

}
