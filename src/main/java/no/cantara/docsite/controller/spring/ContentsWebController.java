package no.cantara.docsite.controller.spring;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.RepoConfig;
import no.cantara.docsite.domain.scm.ScmRepositoryContents;
import no.cantara.docsite.domain.scm.ScmRepositoryContentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Spring MVC Contents Web Controller
 * <p>
 * Displays repository contents page (README, documentation).
 * Replaces the Undertow ContentsHandler with Spring MVC @Controller.
 * <p>
 * Endpoints:
 * - GET /contents/{org}/{repo}/{branch}: Display repository README
 * <p>
 * Template: contents/content.html (Thymeleaf)
 * <p>
 * Code Reduction: ~50 lines → ~25 lines (50% reduction)
 *
 * @author Claude Code Agent
 * @since Week 2-3 - Spring Boot Controller Migration - Task 6
 */
@Controller
@RequestMapping("/contents")
public class ContentsWebController {

    private static final Logger LOG = LoggerFactory.getLogger(ContentsWebController.class);

    private final CacheStore cacheStore;

    public ContentsWebController(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    /**
     * Display repository contents (README)
     *
     * @param org    Organization name
     * @param repo   Repository name
     * @param branch Branch name
     * @param model  Spring Model for template variables
     * @return Template name "contents/content"
     */
    @GetMapping("/{org}/{repo}/{branch}")
    public String showContents(@PathVariable String org,
                                @PathVariable String repo,
                                @PathVariable String branch,
                                Model model) {
        ScmRepositoryContentsService repositoryContentsService = new ScmRepositoryContentsService(cacheStore);
        String organization = cacheStore.getRepositoryConfig().getOrganization(RepoConfig.ScmProvider.GITHUB);
        CacheKey cacheKey = CacheKey.of(organization, repo, branch);
        ScmRepositoryContents contents = repositoryContentsService.get(cacheKey);

        if (contents == null) {
            LOG.error("Contents is NULL. Probably because it was not fetched due to rate limit issue!");
            return "redirect:/dashboard";  // Redirect to dashboard if contents not found
        }

        model.addAttribute("contents", contents);

        return "contents/content";
    }
}
