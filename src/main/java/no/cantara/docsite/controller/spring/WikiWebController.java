package no.cantara.docsite.controller.spring;

import no.cantara.docsite.cache.CacheCantaraWikiKey;
import no.cantara.docsite.cache.CacheStore;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.cache.Cache;

/**
 * Spring MVC Wiki Web Controller
 * <p>
 * Displays Cantara wiki pages from Confluence.
 * Replaces the Undertow CantaraWikiHandler with Spring MVC @Controller.
 * <p>
 * Endpoints:
 * - GET /wiki/{pageName}: Display wiki page
 * <p>
 * Template: wiki/cantara.html (Thymeleaf)
 * <p>
 * Code Reduction: ~50 lines → ~25 lines (50% reduction)
 *
 * @author Claude Code Agent
 * @since Week 2-3 - Spring Boot Controller Migration - Task 6
 */
@Controller
@RequestMapping("/wiki")
public class WikiWebController {

    private final CacheStore cacheStore;

    public WikiWebController(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    /**
     * Display wiki page from Confluence
     *
     * @param pageName Wiki page name
     * @param model    Spring Model for template variables
     * @return Template name "wiki/cantara"
     */
    @GetMapping("/{pageName}")
    public String showWikiPage(@PathVariable String pageName, Model model) {
        if (pageName == null || pageName.isEmpty()) {
            return "redirect:/dashboard";
        }

        // Find wiki page in cache
        CacheCantaraWikiKey cacheCantaraWikiKey = null;
        for (Cache.Entry<CacheCantaraWikiKey, String> entry : cacheStore.getCantaraWiki()) {
            if (pageName.equals(entry.getKey().pageName)) {
                cacheCantaraWikiKey = entry.getKey();
                break;
            }
        }

        if (cacheCantaraWikiKey == null) {
            return "redirect:/dashboard";  // Page not found
        }

        String wikiContent = cacheStore.getCantaraWiki().get(cacheCantaraWikiKey);

        model.addAttribute("wikiHtml", wikiContent);

        return "wiki/cantara";
    }
}
