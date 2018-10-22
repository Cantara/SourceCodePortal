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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ContentsHandler implements WebHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ContentsHandler.class);

    @Override
    public boolean handleRequest(DynamicConfiguration configuration, CacheStore cacheStore, ResourceContext resourceContext, WebContext webContext, HttpServerExchange exchange) {
        Map<String, Object> templateVariables = new HashMap<>();

        if (resourceContext.getTuples().size() != 2) {
            return false;
        }

        String org = cacheStore.getRepositoryConfig().gitHub.organization;
        String repoName = resourceContext.getTuples().get(0).id;
        String branch = resourceContext.getTuples().get(1).resource;

        CacheKey cacheKey = CacheKey.of(org, repoName, branch);
        Set<CacheGroupKey> groupKeys = new LinkedHashSet<>();
        for (Cache.Entry<CacheGroupKey,CacheKey> entry : cacheStore.getCacheGroupKeys()) {
            if (entry.getValue().equals(cacheKey)) {
                groupKeys.add(entry.getKey());
            }
        }
        CacheGroupKey cacheGroupKey = groupKeys.stream().filter(f -> f.repoName.toLowerCase().contains(f.groupId.toLowerCase())).findFirst().orElse(groupKeys.iterator().next());

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
