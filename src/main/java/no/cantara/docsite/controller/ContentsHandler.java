package no.cantara.docsite.controller;

import io.undertow.server.HttpServerExchange;
import no.cantara.docsite.cache.CacheGroupKey;
import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.Repository;
import no.cantara.docsite.domain.config.RepositoryConfig;
import no.cantara.docsite.domain.github.contents.RepositoryContents;
import no.cantara.docsite.domain.github.pages.ReadmeAsciidocWiki;
import no.cantara.docsite.domain.github.pages.ReadmeMDWiki;
import no.cantara.docsite.web.ResourceContext;
import no.cantara.docsite.web.ThymeleafViewEngineProcessor;
import no.cantara.docsite.web.WebContext;
import no.cantara.docsite.web.WebHandler;
import no.ssb.config.DynamicConfiguration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
        CacheGroupKey cacheGroupKey = cacheStore.getCacheKeys().get(cacheKey);

        Repository repository = cacheStore.getRepositoryGroups().get(cacheGroupKey);
        templateVariables.put("repository", repository);

        RepositoryContents contents = cacheStore.getPages().get(cacheKey);
        templateVariables.put("contents", contents);

        if (contents.name.endsWith(".md")) {
            ReadmeMDWiki wiki = new ReadmeMDWiki(contents.content);
            templateVariables.put("contentHtml", wiki.html);

        } else  if (contents.name.endsWith(".adoc")) {
            ReadmeAsciidocWiki wiki = new ReadmeAsciidocWiki(contents.content);

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

            String htmlContent = doc.body().html();
            templateVariables.put("contentHtml", htmlContent);
        }

        if (ThymeleafViewEngineProcessor.processView(exchange, webContext.asTemplateResource(), templateVariables)) {
            return true;
        }

        return false;
    }
}
