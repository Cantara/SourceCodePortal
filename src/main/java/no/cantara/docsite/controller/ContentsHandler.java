package no.cantara.docsite.controller;

import io.undertow.server.HttpServerExchange;
import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.RepositoryConfig;
import no.cantara.docsite.domain.github.contents.RepositoryContents;
import no.cantara.docsite.domain.github.pages.ReadmeAsciidocWiki;
import no.cantara.docsite.domain.github.pages.ReadmeMDWiki;
import no.cantara.docsite.web.ResourceContext;
import no.cantara.docsite.web.ThymeleafViewEngineProcessor;
import no.cantara.docsite.web.WebContext;
import no.cantara.docsite.web.WebHandler;
import no.ssb.config.DynamicConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ContentsHandler implements WebHandler {

    @Override
    public boolean handleRequest(DynamicConfiguration configuration, CacheStore cacheStore, ResourceContext resourceContext, WebContext webContext, HttpServerExchange exchange) {
        Map<String, Object> templateVariables = new HashMap<>();

        RepositoryConfig.Repo repositoryConfig = cacheStore.getGroupByGroupId(resourceContext.getLast().get().id);
        if (repositoryConfig == null) {
            return false;
        }

        templateVariables.put("repositoryConfig", repositoryConfig);
        CacheKey cacheKey = CacheKey.of(cacheStore.getRepositoryConfig().gitHub.organization, repositoryConfig.repo, repositoryConfig.branch);
        RepositoryContents contents = cacheStore.getPages().get(cacheKey);
        templateVariables.put("contents", contents);

        if (contents.name.endsWith(".md")) {
            ReadmeMDWiki doc = new ReadmeMDWiki(contents.content);
            templateVariables.put("contentHtml", doc.html);

        } else  if (contents.name.endsWith(".adoc")) {
            ReadmeAsciidocWiki doc = new ReadmeAsciidocWiki(contents.content);
            templateVariables.put("contentHtml", doc.html);
        }

        if (ThymeleafViewEngineProcessor.processView(exchange, webContext.asTemplateResource(), templateVariables)) {
            return true;
        }

        return false;
    }
}
