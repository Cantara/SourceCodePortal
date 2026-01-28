package no.cantara.docsite.controller.spring;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.config.ApplicationProperties;
import no.cantara.docsite.domain.config.RepoConfig;
import no.cantara.docsite.domain.scm.ScmCommitRevision;
import no.cantara.docsite.domain.scm.ScmCommitRevisionService;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.cantara.docsite.domain.scm.ScmRepositoryGroup;
import no.cantara.docsite.domain.scm.ScmRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.cache.Cache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * Spring MVC Dashboard Controller
 *
 * Replaces the Undertow-based DashboardHandler with a Spring MVC @Controller.
 *
 * Endpoints:
 * - GET /dashboard: Main dashboard page with repository groups
 * - GET / : Redirects to /dashboard
 *
 * Migration from Undertow:
 * Before (Undertow):
 * - WebHandler interface with manual Thymeleaf processing
 * - Manual template variable population
 * - Exchange.getResponseSender().send() for output
 *
 * After (Spring MVC):
 * - @Controller with @GetMapping
 * - Spring's Model for template variables
 * - Return view name, Spring handles rendering
 *
 * Benefits:
 * - Automatic view resolution
 * - Less boilerplate code
 * - Better testability with @WebMvcTest
 * - Integration with Spring Security
 * - Automatic model attribute binding
 *
 * Template Location:
 * - src/main/resources/META-INF/views/index.html (Thymeleaf)
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration - Task 5
 */
@Controller
@Profile("!test")
public class DashboardWebController {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardWebController.class);

    private final ApplicationProperties properties;
    private final CacheStore cacheStore;

    @Autowired
    public DashboardWebController(ApplicationProperties properties, CacheStore cacheStore) {
        this.properties = properties;
        this.cacheStore = cacheStore;
    }

    /**
     * Redirect root to dashboard
     *
     * GET /
     * Redirects to /dashboard
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    /**
     * Main dashboard page
     *
     * GET /dashboard
     *
     * Displays repository groups with:
     * - Group ID and display name
     * - Repository count
     * - Recent commit count
     * - Build status summary
     * - Latest 5 commits across all repositories
     *
     * Model attributes:
     * - repositoryGroups: Map of repository groups
     * - lastCommitRevisions: List of latest 5 commits
     * - organization: GitHub organization name
     * - connectedRepos: Total repository count
     * - updatedTime: Current time
     *
     * @param model Spring MVC model for template variables
     * @return View name "index" (resolves to index.html)
     */
    @GetMapping({"/dashboard", "/index"})
    public String dashboard(Model model) {
        LOG.debug("Rendering dashboard");

        try {
            // Get latest 5 commits across all repositories
            ScmCommitRevisionService commitRevisionService = new ScmCommitRevisionService(cacheStore);
            List<ScmCommitRevision> lastCommitRevisions = commitRevisionService.entrySet().values().stream()
                .limit(5)
                .toList();
            model.addAttribute("lastCommitRevisions", lastCommitRevisions);

            // Get repository groups
            ScmRepositoryService scmRepositoryService = new ScmRepositoryService(cacheStore);
            Map<CacheRepositoryKey, ScmRepositoryGroup<ScmRepository>> repositoryGroups =
                scmRepositoryService.defaultRepositoryGroups();
            model.addAttribute("repositoryGroups", repositoryGroups);

            // Add metadata for template
            model.addAttribute("updatedTime", java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            model.addAttribute("connectedRepos", repositoryGroups.size());

            return "index"; // Resolves to META-INF/views/index.html
        } catch (Exception e) {
            LOG.error("Error rendering dashboard", e);
            model.addAttribute("error", "Failed to load dashboard: " + e.getMessage());
            return "error"; // Resolves to META-INF/views/error.html
        }
    }

    /**
     * HTMX endpoint for latest commits fragment
     *
     * GET /api/commits/latest
     *
     * Returns HTML fragment with latest 5 commits for HTMX polling.
     * This endpoint is called every 30 seconds by HTMX to update
     * the commits section without full page refresh.
     *
     * @param model Spring MVC model for template variables
     * @return View name "fragments/commits-latest" (partial HTML)
     */
    @GetMapping("/api/commits/latest")
    public String latestCommitsFragment(Model model) {
        LOG.debug("Fetching latest commits fragment for HTMX");

        try {
            ScmCommitRevisionService commitRevisionService = new ScmCommitRevisionService(cacheStore);
            List<ScmCommitRevision> lastCommitRevisions = commitRevisionService.entrySet().values().stream()
                .limit(5)
                .toList();
            model.addAttribute("lastCommitRevisions", lastCommitRevisions);

            return "fragments/commits-latest"; // Resolves to META-INF/views/fragments/commits-latest.html
        } catch (Exception e) {
            LOG.error("Error fetching latest commits fragment", e);
            return "fragments/error"; // Return error fragment
        }
    }

    // Helper methods (will be replaced with @Cacheable service methods later)

    private List<ScmRepository> getRepositoryGroupsByGroupId(String groupId) {
        List<ScmRepository> repositories = new ArrayList<>();
        Cache<CacheRepositoryKey, ScmRepository> cache = cacheStore.getRepositories();

        StreamSupport.stream(cache.spliterator(), false)
            .forEach(entry -> {
                if (entry.getKey().compareTo(groupId)) {
                    repositories.add(entry.getValue());
                }
            });

        return repositories;
    }

    private long getCommitCount(CacheRepositoryKey repositoryKey) {
        try {
            CacheKey cacheKey = cacheStore.getCacheRepositoryKeys().get(repositoryKey);
            if (cacheKey == null) {
                return 0;
            }

            // Count commits for this repository
            return StreamSupport.stream(cacheStore.getCommits().spliterator(), false)
                .filter(entry -> {
                    // Match by organization, repo name, and branch
                    return entry.getKey().organization.equals(cacheKey.organization)
                        && entry.getKey().repoName.equals(cacheKey.repoName)
                        && entry.getKey().branch.equals(cacheKey.branch);
                })
                .count();
        } catch (Exception e) {
            LOG.warn("Error counting commits for {}: {}", repositoryKey, e.getMessage());
            return 0;
        }
    }
}
