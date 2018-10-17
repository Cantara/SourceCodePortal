package no.cantara.docsite.domain.github.webhook;

import io.undertow.server.HttpServerExchange;

public class GithubWebookHandler {

    /*
    if (!GitHubWebhookUtility.verifySignature(payload, xHubSignature, SecurityConfig.getGitHubWebHookSecurityAccessToken())) {
        log.error("GitHub WebHook authorization failed!");
        return Response.status(Response.Status.FORBIDDEN).build();
    } else {
        log.trace("GitHub WebHook is authorized..");
    }

    String decoded = URLDecoder.decode(payload, StandardCharsets.UTF_8.name());
    log.trace("Event: {} -> Payload: {}", xHubEvent, decoded);

    DocumentContext ctx = JsonPath.parse(decoded);

    // ------------------------------------------------------------------------------------------------------
    // Github Ping Event
    // ------------------------------------------------------------------------------------------------------

    if ("ping".equals(xHubEvent)) {
        log.trace("Received Ping Event!");
    }

    // ------------------------------------------------------------------------------------------------------
    // Github Module update Event
    // ------------------------------------------------------------------------------------------------------

    //
    // compare module update with current module
    //   new sub-modules (sjekk om id finnes i internalMap. sjekk også om en id er tatt bort fra en module. lag en ny map hvor id-er legges til for en new-module og sammenlikn med gammel module
    //   name changes
    // fetch new page if there are changes
    // 5144b64a-cc59-11e7-9b70-737fe2f2f4fa

    if ("push".equals(xHubEvent) && GithubUtil.isCommitEvent(ctx)) {
        // descoped/module.json
        Modules.Module moduleUpdate = GithubUtil.getPushModuleUpdate(ctx);
        if (moduleUpdate != null && githubPagesService.getConfigModules().exists(moduleUpdate.getId())) {
            queueModuleEvent.fire(moduleUpdate);
        }
    }


    // ------------------------------------------------------------------------------------------------------
    // Github Pages Event
    // ------------------------------------------------------------------------------------------------------

    if ("push".equals(xHubEvent) && GithubUtil.isPageCommitEvent(ctx)) {
        String repositoryName = GithubUtil.getPageCommitRepositoryName(ctx); // descoped-web
        String fileName = GithubUtil.getPageCommitFileName(ctx); // server-container/README.md
        String pageId = (fileName.contains("/") ? fileName.split("\\/")[0] : repositoryName);
        log.trace("pageId: {}", pageId);

        Modules.Page page = githubPagesService.findAllPagesByGroup("default").get(pageId);
        String commitURL = String.format("https://raw.githubusercontent.com/%s/%s/%s",
                GithubUtil.getPageCommitRepositoryFullName(ctx),
                GithubUtil.getPageCommitAfterRevision(ctx),
                GithubUtil.getPageCommitFileName(ctx));
        log.trace("CommitURL: {}", commitURL);
        page.setPageURL(commitURL);

        queuePageEvent.fire(page);
    }

    // ------------------------------------------------------------------------------------------------------
    // Github Commit Event
    // ------------------------------------------------------------------------------------------------------

    // 46754fe2-cac2-11e7-8615-7e26ff60c754

    if ("push".equals(xHubEvent) && GithubUtil.isCommitEvent(ctx)) {
        // CommitRevision to be fired to Service Observer using a reference and a queueSingleCommit
        CommitRevision.Entry commitRevision = GithubUtil.getPushCommitRevision(ctx);
        queueCommitRevisionsEvent.select(GithubCommitsLiteral.INSTANCE, QueueLiteral.INSTANCE).fire(commitRevision);
    }

        // ------------------------------------------------------------------------------------------------------
        // Github Create Tag Event
        // ------------------------------------------------------------------------------------------------------

    //  08c29fe0-c259-11e7-90da-9e94cf9211cc

    if ("create".equals(xHubEvent)) {
        boolean isCreateTagEvent = GithubUtil.isCreateTagEvent(ctx);
        log.trace("isCreateTagEvent: {}", isCreateTagEvent);
        if (isCreateTagEvent) {
            String repositoryName = GithubUtil.getCreateTagRepositoryName(ctx);
            String refVersion = GithubUtil.getCreateTagRefVersion(ctx);
            log.trace("repositoryName: {} -- refVersion: {}", repositoryName, refVersion);
            Modules.Page project = githubPagesService.findModulePagesByGroup("default").get(repositoryName);
            if (project != null) {
                log.trace("Found page: {}", project.getId());
                project.asModule().setBranch(refVersion);
                queueReleasesEvent.fire(project);
            } else {
                log.trace("Skipping Release Project not found: {}: {}", repositoryName, refVersion);
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------
    // Github Release Event
    // ------------------------------------------------------------------------------------------------------

    if ("release".equals(xHubEvent)) {
        // use 0870d660-c259-11e7-875b-e3dd6c55e81d

    }
    */

    public GithubWebookHandler(HttpServerExchange exchange) {
    }


}
