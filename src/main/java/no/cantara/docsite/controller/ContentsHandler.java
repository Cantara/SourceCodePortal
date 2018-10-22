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

        String org = cacheStore.getRepositoryConfig().gitHub.organization;
        String repoNae = resourceContext.getLast().get().id;
        String branch = "master";
        CacheKey cacheKey = CacheKey.of(org, repoNae, branch);
        CacheGroupKey cacheGroupKey = cacheStore.getCacheKeys().get(cacheKey);

        RepositoryConfig.Repo repositoryConfig = cacheStore.getGroupByGroupId(cacheGroupKey.groupId);
        if (repositoryConfig == null) {
            return false;
        }

        templateVariables.put("repositoryConfig", repositoryConfig);

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
