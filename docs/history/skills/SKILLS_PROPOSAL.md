# Claude Code Skills Proposal

**Date**: 2026-01-28
**Context**: Post-Dark Mode Implementation + Phase 3 Frontend Modernization

---

## Current Skills Inventory

### ✅ Existing Skills (5 total)

**New Skills (3)**:
1. `/migrate-controller` - Convert Undertow → Spring MVC controllers
2. `/add-health-indicator` - Create Actuator health indicators
3. `/add-scheduled-task` - Create @Scheduled background tasks

**Updated Skills (2)**:
4. `/modernize-dependency` v2.0 - Update dependencies with Spring Boot 3.x patterns
5. `/verify-build` v2.0 - Comprehensive Spring Boot build verification

---

## Proposed Skill Updates

### 1. Update `/modernize-dependency` → v3.0

**New Knowledge from Phase 3**:
- Node.js version updates (EOL detection)
- npm package.json engine constraints
- Vite migration patterns (Gulp → Vite)
- Bootstrap 4 → 5 migration gotchas
- Font Awesome 4 → 6 migration
- jQuery removal patterns
- Dark mode CSS integration

**Example Additions**:
```yaml
# Node.js EOL check
- Check if Node version is EOL (e.g., Node 10 EOL April 2021)
- Update package.json engines field
- Update frontend-maven-plugin version

# Vite migration
- Replace gulp.js with vite.config.js
- Update package.json scripts (build → vite build)
- Fix output directory paths (relative paths)
- Update CSS preprocessor config (api: 'modern-compiler')

# Bootstrap 5 migration
- Update data-* attributes (data-toggle → data-bs-toggle)
- Remove jQuery dependencies
- Update modal/dropdown/collapse triggers
- Add Bootstrap 5 dark mode support
```

**Value**: Captures frontend modernization patterns

---

### 2. Update `/verify-build` → v3.0

**New Knowledge from Phase 3**:
- Vite build verification
- npm audit verification (0 vulnerabilities)
- Asset compilation verification (CSS, JS sizes)
- Dark mode asset verification
- HTMX endpoint verification
- Static resource serving verification

**Example Additions**:
```bash
# Vite Build Verification Checklist
cd src/main/sass && npm run build  # Expect: built in ~9s
npm audit --production  # Expect: 0 vulnerabilities
ls -lh ../resources/META-INF/views/css/style.css  # Expect: ~240KB
ls -lh ../resources/META-INF/views/js/main.js  # Expect: ~160KB

# Dark Mode Verification
curl http://localhost:9090/js/main.js | grep -c "initTheme"  # Expect: 3
grep -c "data-bs-theme" src/main/resources/META-INF/views/template.html  # Expect: 1

# Static Assets Verification
curl -I http://localhost:9090/css/style.css | head -1  # Expect: HTTP/1.1 200
curl -I http://localhost:9090/js/main.js | head -1  # Expect: HTTP/1.1 200
```

**Value**: Comprehensive frontend build verification

---

## Proposed New Domain-Specific Skills

### 3. `/add-badge-type` 🆕

**Purpose**: Add new badge type to repository dashboard

**Target Codebase**: Source Code Portal

**What it does**:
- Prompts for badge service (Jenkins, Snyk, CircleCI, Shields.io custom)
- Creates badge SVG files (success, failure, unknown states)
- Updates BadgeResourceController with new endpoint
- Adds cache configuration for badge status
- Creates scheduled service to fetch badge status
- Updates template to display badge
- Adds tests

**Example Usage**:
```
/add-badge-type

What is the badge name? CircleCI
What endpoint pattern? /badge/circleci/{org}/{repo}
What service fetches status? CircleCI API
```

**Generated Files**:
- `BadgeResourceController.java` - Add endpoint
- `CircleCiStatus.java` - Domain model
- `FetchCircleCiStatusCommand.java` - Hystrix/Resilience4j command
- `CircleCiScheduledService.java` - Background fetch
- `img/circleci-*.svg` - Badge images
- Tests

**Time Savings**: 4-6 hours → 10-15 minutes

**Value**: Extends dashboard with new status indicators

---

### 4. `/add-scm-provider` 🆕

**Purpose**: Add support for GitLab, Bitbucket, Azure DevOps, etc.

**Target Codebase**: Source Code Portal

**What it does**:
- Prompts for SCM provider (GitLab, Bitbucket, Azure DevOps)
- Creates provider-specific API client
- Creates domain models (Repository, Commit, Release)
- Adds configuration properties
- Creates webhook handler with signature verification
- Updates RepositoryConfigLoader for multi-provider support
- Adds provider-specific icon/branding
- Adds tests

**Example Usage**:
```
/add-scm-provider

What SCM provider? GitLab
API Base URL? https://gitlab.com/api/v4
Authentication method? Personal Access Token
```

**Generated Files**:
- `GitLabApiClient.java` - API client
- `GitLabRepository.java` - Domain model
- `GitLabWebhookHandler.java` - Webhook support
- `application.yml` - Configuration section
- Tests

**Time Savings**: 2-3 weeks → 1-2 hours

**Value**: Major feature addition (multi-SCM support)

---

### 5. `/add-dashboard-widget` 🆕

**Purpose**: Add new widget/card to dashboard

**Target Codebase**: Source Code Portal

**What it does**:
- Prompts for widget type (chart, table, list, metric)
- Creates Spring MVC controller endpoint
- Creates Thymeleaf template fragment
- Adds HTMX polling configuration (optional)
- Styles widget with Bootstrap 5 + dark mode
- Adds cache for widget data
- Adds tests

**Example Usage**:
```
/add-dashboard-widget

What widget type? Pull Request Summary
What data to display? Open PRs by age (< 1 day, 1-3 days, > 3 days)
Refresh interval? 5 minutes
```

**Generated Files**:
- `PullRequestWebController.java` - Endpoint
- `fragments/pr-summary.html` - Template
- `PullRequestService.java` - Business logic
- Tests

**Time Savings**: 3-5 hours → 15-20 minutes

**Value**: Rapid dashboard customization

---

## Proposed New Implementation-Type Skills

### 6. `/add-dark-mode` 🆕

**Purpose**: Add Bootstrap 5 dark mode with theme toggle

**Target**: Any Spring Boot + Bootstrap 5 application

**What it does**:
- Adds `data-bs-theme` attribute to HTML template
- Creates theme toggle button with icon
- Implements localStorage theme persistence
- Adds custom dark mode CSS overrides
- Updates Vite/Webpack config if needed
- Tests theme switching

**Example Usage**:
```
/add-dark-mode

Template file? src/main/resources/templates/base.html
Where to place toggle? Navbar (right side)
Default theme? light
```

**Generated Changes**:
- HTML: Add `data-bs-theme="light"` + toggle button
- JS: Add `initTheme()` function with localStorage
- CSS: Add `[data-bs-theme="dark"]` overrides
- Build: Compile and verify

**Time Savings**: 2-3 hours → 10 minutes

**Value**: Reusable pattern for any Bootstrap 5 app

**Reusability**: ⭐⭐⭐⭐⭐ (Any Bootstrap 5 project)

---

### 7. `/add-htmx-endpoint` 🆕

**Purpose**: Add HTMX partial page update endpoint

**Target**: Any Spring Boot + Thymeleaf application

**What it does**:
- Creates Spring MVC endpoint returning HTML fragment
- Creates Thymeleaf fragment template
- Adds HTMX attributes to parent div
- Configures polling/trigger (click, every Xs, etc.)
- Adds swap strategy (innerHTML, outerHTML, etc.)
- Tests HTMX endpoint

**Example Usage**:
```
/add-htmx-endpoint

What content to update? Latest commit list
Update trigger? Poll every 30 seconds
Swap strategy? innerHTML
```

**Generated Changes**:
- Controller: Add `/api/commits/latest` endpoint
- Template: Create `fragments/commits-latest.html`
- Parent page: Add `hx-get`, `hx-trigger`, `hx-swap` attributes
- Build: Compile and verify

**Time Savings**: 1-2 hours → 10 minutes

**Value**: Rapid SPA-like features without JavaScript frameworks

**Reusability**: ⭐⭐⭐⭐⭐ (Any Thymeleaf + HTMX project)

---

### 8. `/migrate-build-tool` 🆕

**Purpose**: Migrate frontend build tool (Gulp → Vite, Webpack → Vite, etc.)

**Target**: Any project with frontend build pipeline

**What it does**:
- Analyzes existing build tool configuration
- Creates equivalent Vite/Webpack/Rollup config
- Updates package.json scripts
- Fixes output directory paths
- Migrates CSS preprocessor config
- Updates Maven/Gradle frontend plugin
- Runs build and verifies output

**Example Usage**:
```
/migrate-build-tool

Current build tool? Gulp
Target build tool? Vite
Output directory? src/main/resources/static
```

**Generated Files**:
- `vite.config.js` (or webpack.config.js, etc.)
- `package.json` - Updated scripts
- `pom.xml` - Updated frontend-maven-plugin
- Delete old config (gulpfile.js)

**Time Savings**: 4-8 hours → 30 minutes

**Value**: Massive build speed improvements (Gulp: ~5min → Vite: ~9s)

**Reusability**: ⭐⭐⭐⭐ (Any project with frontend build)

---

### 9. `/add-localStorage-preference` 🆕

**Purpose**: Add client-side preference with localStorage persistence

**Target**: Any web application

**What it does**:
- Prompts for preference type (theme, layout, sorting, filters)
- Creates JavaScript init function
- Adds localStorage read/write logic
- Creates UI control (toggle, dropdown, checkbox)
- Adds change event handlers
- Tests persistence across page loads

**Example Usage**:
```
/add-localStorage-preference

Preference name? Table row density (compact/comfortable/spacious)
UI control type? Dropdown menu
Default value? comfortable
```

**Generated Changes**:
- JS: Add `initTableDensity()` function
- HTML: Add dropdown menu
- CSS: Add density classes
- localStorage key: 'table-density'

**Time Savings**: 1-2 hours → 10 minutes

**Value**: Better UX with persistent preferences

**Reusability**: ⭐⭐⭐⭐⭐ (Any web app)

---

### 10. `/fix-spring-profile-issues` 🆕

**Purpose**: Fix Spring Boot @Profile configuration issues

**Target**: Any Spring Boot application

**What it does**:
- Scans for `@Profile("!test")` annotations
- Identifies beans that are needed in all profiles
- Removes restrictive profile annotations
- Updates test configuration properly
- Verifies application starts in all profiles
- Tests bean wiring

**Example Usage**:
```
/fix-spring-profile-issues

Profile causing issues? test
Error message? "No qualifying bean of type 'CacheStore' available"
```

**Analysis Steps**:
1. Find all `@Profile("!test")` annotations
2. Identify which beans are required dependencies
3. Remove `@Profile` from required beans
4. Keep `@Profile` on optional beans (e.g., schedulers)
5. Verify startup in all profiles

**Time Savings**: 1-3 hours → 15 minutes

**Value**: Fixes common Spring Boot configuration errors

**Reusability**: ⭐⭐⭐⭐ (Any multi-profile Spring Boot app)

---

## Skills Priority Matrix

### Implementation Priority

| Skill | Type | Value | Effort | Priority | Reusability |
|-------|------|-------|--------|----------|-------------|
| `/add-dark-mode` | Implementation | ⭐⭐⭐⭐⭐ | Low | 🔥 HIGH | ⭐⭐⭐⭐⭐ |
| `/add-htmx-endpoint` | Implementation | ⭐⭐⭐⭐⭐ | Low | 🔥 HIGH | ⭐⭐⭐⭐⭐ |
| `/add-localStorage-preference` | Implementation | ⭐⭐⭐⭐ | Low | 🔥 HIGH | ⭐⭐⭐⭐⭐ |
| `/fix-spring-profile-issues` | Implementation | ⭐⭐⭐⭐ | Low | 🔥 HIGH | ⭐⭐⭐⭐ |
| `/migrate-build-tool` | Implementation | ⭐⭐⭐⭐⭐ | Medium | 🟡 MEDIUM | ⭐⭐⭐⭐ |
| `/add-badge-type` | Domain | ⭐⭐⭐⭐ | Medium | 🟡 MEDIUM | ⭐⭐ |
| `/add-dashboard-widget` | Domain | ⭐⭐⭐⭐⭐ | Medium | 🟡 MEDIUM | ⭐⭐ |
| `/add-scm-provider` | Domain | ⭐⭐⭐⭐⭐ | High | 🟢 LOW | ⭐⭐ |

### Recommended Implementation Order

**Tier 1 (Immediate - High Reusability)**:
1. `/add-dark-mode` - Universal pattern, just completed
2. `/add-htmx-endpoint` - Universal pattern, just completed
3. `/add-localStorage-preference` - Universal pattern, extends dark mode
4. `/fix-spring-profile-issues` - Just solved this problem

**Tier 2 (Soon - Medium Effort)**:
5. `/migrate-build-tool` - Captures Gulp → Vite migration
6. `/add-dashboard-widget` - High value for SCP
7. `/add-badge-type` - Extends SCP capabilities

**Tier 3 (Later - High Effort)**:
8. `/add-scm-provider` - Strategic but complex

---

## Recommended: Implement Tier 1 Skills Now

### Rationale

1. **Fresh Knowledge**: We just completed dark mode and HTMX implementation
2. **High Reusability**: Tier 1 skills apply to ANY web application
3. **Low Effort**: Each skill is 30-60 minutes to create
4. **Immediate Value**: Can use them on other projects immediately

### Implementation Approach

**Session 1** (30-45 min):
- Create `/add-dark-mode` skill
- Capture dark mode implementation we just completed

**Session 2** (30-45 min):
- Create `/add-htmx-endpoint` skill
- Capture HTMX patterns from commits endpoint

**Session 3** (30-45 min):
- Create `/add-localStorage-preference` skill
- Generalize theme toggle pattern

**Session 4** (30-45 min):
- Create `/fix-spring-profile-issues` skill
- Capture profile configuration fixes

**Total Time**: 2-3 hours for 4 universal skills

---

## Discussion Questions

### For User

1. **Which skills should we prioritize?**
   - Tier 1 (high reusability) vs Domain-specific first?
   - Update existing skills first vs create new skills?

2. **Domain-specific skills value?**
   - Is `/add-badge-type` valuable for your use case?
   - Would `/add-scm-provider` enable multi-SCM support you need?
   - Is `/add-dashboard-widget` worth the effort?

3. **Skill format preferences?**
   - Simple (step-by-step checklist) vs Comprehensive (code generation)?
   - Interactive (ask questions) vs Template-based (fill in blanks)?

4. **Immediate needs?**
   - Do you have upcoming work where these skills would help?
   - Are there other patterns worth capturing?

---

## Success Criteria

### For Skill Updates

✅ Capture dark mode implementation patterns
✅ Capture HTMX endpoint patterns
✅ Capture Vite migration patterns
✅ Capture Spring Boot @Profile fixes
✅ Update skill version numbers (v2.0 → v3.0)

### For New Skills

✅ Clear purpose and use case
✅ Step-by-step implementation guide
✅ Real examples from our codebase
✅ Time savings calculation
✅ Reusability rating
✅ Testing verification steps

---

## Next Steps

**Your Choice**:

**Option A**: Update existing skills with Phase 3 learnings (30 min)
- Update `/modernize-dependency` v2.0 → v3.0
- Update `/verify-build` v2.0 → v3.0

**Option B**: Create Tier 1 implementation-type skills (2-3 hours)
- `/add-dark-mode`
- `/add-htmx-endpoint`
- `/add-localStorage-preference`
- `/fix-spring-profile-issues`

**Option C**: Create domain-specific skills for SCP (3-4 hours)
- `/add-badge-type`
- `/add-dashboard-widget`
- `/add-scm-provider`

**Option D**: Discuss and refine proposal first
- Which skills are most valuable?
- What patterns are missing?
- Should we prioritize differently?

---

**What would you like to do?**

1. **Update existing skills** (quick, captures Phase 3 learnings)
2. **Create Tier 1 implementation skills** (universal, high reusability)
3. **Create domain skills** (SCP-specific, strategic value)
4. **Discuss and refine** (ensure we're building the right skills)

