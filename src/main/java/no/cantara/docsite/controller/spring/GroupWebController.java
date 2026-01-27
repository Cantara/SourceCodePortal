package no.cantara.docsite.controller.spring;

import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.RepoConfig;
import no.cantara.docsite.domain.scm.ScmCommitRevision;
import no.cantara.docsite.domain.scm.ScmCommitRevisionService;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.cantara.docsite.domain.scm.ScmRepositoryGroup;
import no.cantara.docsite.domain.scm.ScmRepositoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring MVC Group/Card Web Controller
 * <p>
 * Displays repository group view page (card layout).
 * Replaces the Undertow CardHandler with Spring MVC @Controller.
 * <p>
 * Endpoints:
 * - GET /group/{groupId}: Display repository group page
 * <p>
 * Template: group/card.html (Thymeleaf)
 * <p>
 * Migration from Undertow:
 * - Before: CardHandler implements WebHandler with manual routing
 * - After: @Controller with @GetMapping path variable
 * - Before: Manual template variable building (Map<String, Object>)
 * - After: Spring Model for template variables
 * - Before: ThymeleafViewEngineProcessor.processView()
 * - After: Return template name (Spring resolves automatically)
 * <p>
 * Code Reduction: ~60 lines → ~30 lines (50% reduction)
 *
 * @author Claude Code Agent
 * @since Week 2-3 - Spring Boot Controller Migration - Task 6
 */
@Controller
@RequestMapping("/group")
public class GroupWebController {

    private final CacheStore cacheStore;

    public GroupWebController(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    /**
     * Display repository group page
     * <p>
     * Shows:
     * - Group name and description
     * - List of repositories in the group (sorted alphabetically)
     * - Last 5 commit revisions across the group
     * - Build status, Snyk status, badges for each repository
     *
     * @param groupId Group identifier (e.g., "whydah", "config-service")
     * @param model   Spring Model for template variables
     * @return Template name "group/card" (resolves to group/card.html)
     */
    @GetMapping("/{groupId}")
    public String showGroupCard(@PathVariable String groupId, Model model) {
        ScmRepositoryService service = new ScmRepositoryService(cacheStore);
        RepoConfig.Repo repositoryConfig = service.getGroupRepoConfig(groupId);

        if (repositoryConfig == null) {
            return "redirect:/dashboard";  // Group not found, redirect to dashboard
        }

        // Get last 5 commits for this group
        ScmCommitRevisionService commitRevisionService = new ScmCommitRevisionService(cacheStore);
        List<ScmCommitRevision> lastCommitRevisions = commitRevisionService.entrySet(groupId)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        // Get repositories in this group (sorted alphabetically)
        Map.Entry<CacheRepositoryKey, Set<ScmRepository>> repositoryGroups = service.getRepositoryGroups(groupId);
        CacheRepositoryKey key = repositoryGroups.getKey();
        Set<ScmRepository> repos = repositoryGroups.getValue()
                .stream()
                .sorted(Comparator.comparing(c -> c.cacheRepositoryKey.repoName.toLowerCase()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Build repository group view model
        ScmRepositoryGroup<Set<ScmRepository>> scmRepositoryGroup = new ScmRepositoryGroup<>(
                repos,
                repositoryConfig.displayName,
                repositoryConfig.description,
                repos.size()
        );

        // Add template variables
        model.addAttribute("lastCommitRevisions", lastCommitRevisions);
        model.addAttribute("groupId", groupId);
        model.addAttribute("cacheKey", key);
        model.addAttribute("repositoryGroup", scmRepositoryGroup);

        return "group/card";  // Thymeleaf template: templates/group/card.html
    }
}
