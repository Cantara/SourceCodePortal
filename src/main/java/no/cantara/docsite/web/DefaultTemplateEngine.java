package no.cantara.docsite.web;

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

class DefaultTemplateEngine {

    static final TemplateEngine INSTANCE;

    static {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver(classLoader);
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCacheable(false);
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        templateEngine.addDialect(new LayoutDialect());
        INSTANCE = templateEngine;
    }

}
