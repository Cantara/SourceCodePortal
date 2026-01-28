# Documentation Reorganization Plan

**Date**: 2026-01-27
**Current State**: 20 markdown files, needs consolidation and organization
**Goal**: Create clear, maintainable documentation structure

---

## Current State Analysis

### Existing Files (20 total)

| File | Status | Keep/Consolidate/Archive |
|------|--------|--------------------------|
| CLAUDE.md | ✅ Good | **KEEP** - Main guide for Claude Code |
| CLAUDE_SKILLS.md | ✅ Good | **KEEP** - Skills reference |
| LEARNINGS_PHASE2.md | ✅ New | **KEEP** - Phase 2 learnings |
| VERIFICATION_GUIDE.md | ✅ Good | **KEEP** - How to verify migration |
| TODO.md | ❓ Unknown | **REVIEW** - Check if still relevant |
| Snyk.md | ❓ Unknown | **REVIEW** - Check content |
| GOTCHAS_AND_LEARNINGS.md | 🔄 Merge | **CONSOLIDATE** into LEARNINGS_PHASE2.md |
| MODERNIZATION_PHASE1.md | 📦 Archive | **ARCHIVE** - Phase 1 complete |
| PHASE1_COMPLETE.md | 📦 Archive | **ARCHIVE** - Phase 1 complete |
| PHASE1_SUMMARY.md | 📦 Archive | **ARCHIVE** - Phase 1 complete |
| PHASE2_PLAN.md | 🔄 Archive | **ARCHIVE** - Phase 2 complete |
| PHASE2_PROGRESS.md | ✅ Good | **KEEP** - Important reference |
| MIGRATION_JUNIT5_SUMMARY.md | 📦 Archive | **ARCHIVE** - Phase 1 work |
| SKILLS_AND_DOCUMENTATION_SUMMARY.md | 🔄 Obsolete | **DELETE** - Superseded by CLAUDE_SKILLS.md |
| TASK3_SUMMARY.md | 📦 Archive | **ARCHIVE** - Detailed task logs |
| TASK4_SUMMARY.md | 📦 Archive | **ARCHIVE** - Detailed task logs |
| TASK5_SUMMARY.md | 📦 Archive | **ARCHIVE** - Detailed task logs |
| TASK6_SUMMARY.md | 📦 Archive | **ARCHIVE** - Detailed task logs |
| TASK7_SUMMARY.md | 📦 Archive | **ARCHIVE** - Detailed task logs |
| TASK8_SUMMARY.md | 📦 Archive | **ARCHIVE** - Detailed task logs |

---

## Proposed New Structure

```
/src/cantara/SourceCodePortal/
│
├── README.md                          # Project overview (NEW - user-facing)
├── CLAUDE.md                          # Claude Code guide (KEEP)
│
├── docs/                              # Documentation directory (NEW)
│   ├── getting-started/
│   │   ├── quickstart.md              # NEW - 5 min quick start
│   │   ├── building.md                # NEW - Build & run instructions
│   │   ├── configuration.md           # NEW - Configuration guide
│   │   └── development.md             # NEW - Dev environment setup
│   │
│   ├── architecture/
│   │   ├── overview.md                # NEW - System architecture
│   │   ├── spring-boot.md             # NEW - Spring Boot setup
│   │   ├── controllers.md             # NEW - Controller patterns
│   │   ├── caching.md                 # NEW - Caching strategy
│   │   └── scheduled-tasks.md         # NEW - Background jobs
│   │
│   ├── features/
│   │   ├── dashboard.md               # NEW - Dashboard features
│   │   ├── repository-groups.md       # NEW - Repository grouping
│   │   ├── integrations.md            # NEW - External integrations
│   │   ├── webhooks.md                # NEW - GitHub webhooks
│   │   └── observability.md           # NEW - Metrics & monitoring
│   │
│   ├── operations/
│   │   ├── deployment.md              # NEW - Deploy guide
│   │   ├── monitoring.md              # NEW - Monitoring setup
│   │   ├── troubleshooting.md         # NEW - Common issues
│   │   └── backup-recovery.md         # NEW - Backup strategy
│   │
│   └── development/
│       ├── contributing.md            # NEW - Contribution guide
│       ├── testing.md                 # NEW - Testing guide
│       ├── code-style.md              # NEW - Code conventions
│       └── skills.md                  # MOVE from CLAUDE_SKILLS.md
│
├── docs/history/                      # Archive directory (NEW)
│   ├── phase1/
│   │   ├── PHASE1_SUMMARY.md          # ARCHIVE - Phase 1 complete
│   │   ├── PHASE1_COMPLETE.md         # ARCHIVE
│   │   ├── MODERNIZATION_PHASE1.md    # ARCHIVE
│   │   └── MIGRATION_JUNIT5_SUMMARY.md # ARCHIVE
│   │
│   ├── phase2/
│   │   ├── PHASE2_PROGRESS.md         # ARCHIVE (keep as reference)
│   │   ├── PHASE2_PLAN.md             # ARCHIVE
│   │   ├── LEARNINGS_PHASE2.md        # KEEP in main, link here
│   │   └── tasks/
│   │       ├── TASK3_SUMMARY.md       # ARCHIVE
│   │       ├── TASK4_SUMMARY.md       # ARCHIVE
│   │       ├── TASK5_SUMMARY.md       # ARCHIVE
│   │       ├── TASK6_SUMMARY.md       # ARCHIVE
│   │       ├── TASK7_SUMMARY.md       # ARCHIVE
│   │       └── TASK8_SUMMARY.md       # ARCHIVE
│   │
│   └── migration-notes/
│       └── gotchas.md                 # CONSOLIDATE from GOTCHAS_AND_LEARNINGS.md
│
├── CHANGELOG.md                       # NEW - Version history
├── LEARNINGS.md                       # CONSOLIDATE - All learnings
└── VERIFICATION_GUIDE.md              # KEEP - Verification steps
```

---

## Documentation Consolidation Plan

### New Top-Level Files

#### 1. README.md (NEW)
**Purpose**: User-facing project overview

**Content**:
- What is Source Code Portal?
- Key features (bullet points)
- Quick start (3 commands to get running)
- Screenshots (dashboard, group view)
- Links to detailed docs
- Technology stack
- License & contributors

**Target Audience**: New users, GitHub visitors

---

#### 2. CHANGELOG.md (NEW)
**Purpose**: Track version history

**Content**:
```markdown
# Changelog

## [0.10.17-SNAPSHOT] - 2026-01-27

### Added - Phase 2: Spring Boot Migration
- Spring Boot 3.2.2 as primary mode
- Spring Boot Actuator with custom health indicators
- Spring MVC controllers (Ping, Health, Dashboard)
- Spring @Scheduled tasks (Jenkins, Snyk)
- Caffeine cache with Micrometer metrics
- Type-safe configuration with @ConfigurationProperties

### Changed
- Java 11 → Java 21 LTS (Phase 1)
- Undertow now embedded in Spring Boot
- TestNG → JUnit 5 (Phase 1)
- Resilience4j circuit breakers (Phase 1)

### Deprecated
- Undertow mode (will be removed in future version)
- Custom ExecutorService (use Spring @Async)
- JSR-107 JCache (use Spring Cache)

## [0.10.16] - 2025-XX-XX
...
```

---

#### 3. LEARNINGS.md (CONSOLIDATE)
**Purpose**: Single source for all learnings

**Consolidate From**:
- LEARNINGS_PHASE2.md (primary source)
- GOTCHAS_AND_LEARNINGS.md (merge in)
- Task summaries (key insights only)

**Structure**:
```markdown
# Learnings & Gotchas

## Phase 2 - Spring Boot Migration
[Content from LEARNINGS_PHASE2.md]

## Phase 1 - Java 21 & JUnit 5
[Extract from Phase 1 docs]

## General Gotchas
[Content from GOTCHAS_AND_LEARNINGS.md]
```

---

### New docs/ Directory

#### docs/getting-started/quickstart.md
**Purpose**: 5-minute quick start

**Content**:
```markdown
# Quick Start

## Prerequisites
- Java 21
- Maven 3.9+
- GitHub personal access token

## Run in 3 Commands

```bash
# 1. Clone and build
git clone ... && cd SourceCodePortal
mvn clean compile

# 2. Set credentials
export SCP_GITHUB_ACCESS_TOKEN=your_token

# 3. Run
mvn spring-boot:run
```

## Access
- Dashboard: http://localhost:9090/dashboard
- Health: http://localhost:9090/actuator/health
- Metrics: http://localhost:9090/actuator/metrics

## Next Steps
- [Configuration Guide](configuration.md)
- [Development Setup](development.md)
```

---

#### docs/architecture/spring-boot.md
**Purpose**: Spring Boot architecture overview

**Content**:
- Spring Boot vs Undertow mode
- Component scanning
- Configuration loading
- Bean lifecycle
- Actuator setup
- Health indicators
- Scheduled tasks

---

#### docs/features/observability.md
**Purpose**: Monitoring & metrics guide

**Content**:
- Actuator endpoints
- Custom health indicators
- Prometheus metrics
- Grafana dashboards (to be created)
- Logging configuration
- Alerting setup (future)

---

#### docs/operations/troubleshooting.md
**Purpose**: Common issues & solutions

**Content**:
- Port already in use
- GitHub token issues
- Config.json not found
- Cache not populating
- Maven plugin errors
- Startup failures

**Source**: Extract from VERIFICATION_GUIDE.md troubleshooting section

---

#### docs/development/skills.md
**Purpose**: Development skills reference

**Content**: Move from CLAUDE_SKILLS.md (or keep both with cross-links)

---

## Reorganization Steps

### Phase 1: Create New Structure (30 minutes)
```bash
# 1. Create directory structure
mkdir -p docs/{getting-started,architecture,features,operations,development}
mkdir -p docs/history/{phase1,phase2/tasks,migration-notes}

# 2. Create new top-level files
touch README.md CHANGELOG.md LEARNINGS.md

# 3. Move Phase 1 archives
mv PHASE1_*.md MODERNIZATION_PHASE1.md MIGRATION_JUNIT5_SUMMARY.md docs/history/phase1/

# 4. Move Phase 2 archives
mv PHASE2_PLAN.md docs/history/phase2/
mv TASK*_SUMMARY.md docs/history/phase2/tasks/

# 5. Archive gotchas
mv GOTCHAS_AND_LEARNINGS.md docs/history/migration-notes/gotchas.md
```

### Phase 2: Create New Documentation (2-3 hours)
- Write README.md (30 min)
- Write CHANGELOG.md (15 min)
- Write docs/getting-started/quickstart.md (30 min)
- Write docs/architecture/spring-boot.md (45 min)
- Write docs/operations/troubleshooting.md (30 min)
- Consolidate LEARNINGS.md (30 min)

### Phase 3: Update References (30 minutes)
- Update CLAUDE.md to reference new docs
- Add links to README.md
- Update VERIFICATION_GUIDE.md references
- Update CLAUDE_SKILLS.md references

### Phase 4: Clean Up (15 minutes)
- Delete SKILLS_AND_DOCUMENTATION_SUMMARY.md (obsolete)
- Review and update TODO.md
- Review Snyk.md (move to docs/features/ or archive)

---

## Files to Keep at Root Level

These stay at project root for discoverability:

1. **README.md** - First thing people see
2. **CLAUDE.md** - Claude Code entry point
3. **CHANGELOG.md** - Version history
4. **LEARNINGS.md** - Quick reference for learnings
5. **VERIFICATION_GUIDE.md** - Verify Spring Boot migration
6. **TODO.md** - Current work tracking (if still relevant)

Everything else goes into `docs/` or `docs/history/`.

---

## CLAUDE.md Updates

Update quick start section:

```markdown
## Quick Start for New Claude Code Sessions

When starting work on this project:

1. **Read this file first** (`CLAUDE.md`)
2. **Check README.md** for project overview
3. **Review LEARNINGS.md** for gotchas
4. **Check docs/** for detailed guides:
   - `docs/getting-started/` - How to run
   - `docs/architecture/` - System design
   - `docs/development/` - Dev guide and skills
5. **Use skills** for common tasks (see `docs/development/skills.md`)
6. **After task**: Update LEARNINGS.md and CHANGELOG.md
```

---

## Benefits of New Structure

### For New Contributors
- ✅ Clear entry point (README.md)
- ✅ Quick start in 5 minutes
- ✅ Organized documentation (docs/)
- ✅ Easy to find information

### For Claude Code
- ✅ CLAUDE.md stays at root (easy to find)
- ✅ Skills accessible via docs/development/skills.md
- ✅ Learnings consolidated in LEARNINGS.md
- ✅ History preserved in docs/history/

### For Maintainers
- ✅ Less clutter at root level
- ✅ Historical docs archived but accessible
- ✅ Clear separation of concerns
- ✅ Easy to maintain

### For Documentation
- ✅ Logical grouping (getting-started, architecture, operations, development)
- ✅ Single source of truth per topic
- ✅ No duplicate information
- ✅ Easy to find what you need

---

## Migration Checklist

- [ ] Create directory structure
- [ ] Write README.md
- [ ] Write CHANGELOG.md
- [ ] Write docs/getting-started/quickstart.md
- [ ] Write docs/architecture/spring-boot.md
- [ ] Write docs/operations/troubleshooting.md
- [ ] Consolidate LEARNINGS.md
- [ ] Move Phase 1 files to docs/history/phase1/
- [ ] Move Phase 2 task files to docs/history/phase2/tasks/
- [ ] Move PHASE2_PLAN.md to docs/history/phase2/
- [ ] Move/consolidate GOTCHAS_AND_LEARNINGS.md
- [ ] Delete SKILLS_AND_DOCUMENTATION_SUMMARY.md
- [ ] Update CLAUDE.md references
- [ ] Update VERIFICATION_GUIDE.md references
- [ ] Review TODO.md (keep or archive)
- [ ] Review Snyk.md (move to docs/features/ or archive)
- [ ] Test all documentation links
- [ ] Commit changes

---

## Timeline

**Estimated Total Time**: 4-5 hours

**Breakdown**:
- Planning: 30 min (done)
- Directory setup: 30 min
- Write new docs: 2-3 hours
- Update references: 30 min
- Clean up: 30 min
- Testing & verification: 30 min

**Recommendation**: Do this in 2 sessions
- Session 1 (2 hours): Structure + README.md + quickstart.md
- Session 2 (2-3 hours): Remaining docs + consolidation + cleanup

---

## Questions to Resolve

1. **TODO.md**: Still relevant or archive?
2. **Snyk.md**: Keep at root or move to docs/features/?
3. **PHASE2_PROGRESS.md**: Archive or keep at root as reference?
4. **Skills location**: Keep CLAUDE_SKILLS.md at root or move to docs/development/?

---

## Next Steps

1. **Review this plan** with user
2. **Get approval** for structure
3. **Execute Phase 1** (create structure, move archives)
4. **Execute Phase 2** (write new docs)
5. **Execute Phase 3** (update references)
6. **Execute Phase 4** (clean up)

---

**Status**: ✅ Plan Complete - Ready for Execution
