package no.cantara.docsite.commands;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import no.cantara.docsite.client.HttpRequests;
import no.cantara.docsite.executor.WorkerTask;
import no.cantara.docsite.health.HealthResource;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.util.Optional;

public class GetGitHubCommand<R> extends BaseHystrixCommand<HttpResponse<R>> {

    private static final Logger LOG = LoggerFactory.getLogger(GetGitHubCommand.class);

    private final DynamicConfiguration configuration;
    private Optional<WorkerTask> worker;
    private String url;
    private HttpResponse.BodyHandler<R> bodyHandler;

    public GetGitHubCommand(String groupKey, DynamicConfiguration configuration, Optional<WorkerTask> worker, String url, HttpResponse.BodyHandler<R> bodyHandler) {
        super(groupKey);
        this.configuration = configuration;
        this.worker = worker;
        this.url = url;
        this.bodyHandler = bodyHandler;
        HystrixRequestContext.initializeContext();
    }

    private String[] getGitHubAuthHeader(DynamicConfiguration configuration) {
        return new String[]{"Authorization", String.format("token %s", configuration.evaluateToString("github.client.accessToken"))};
    }

    private HttpResponse<R> get(String url) {
        String[] authToken = null;
        if (configuration.evaluateToString("github.client.accessToken") != null) {
            authToken = getGitHubAuthHeader(configuration);
        } else {
            String clientIdAndSecret = String.format("client_id=%s&client_secret=%s",
                    configuration.evaluateToString("github.oauth2.client.clientId"),
                    configuration.evaluateToString("github.oauth2.client.clientSecret"));
            url = (url.contains("?") ? url + "&" + clientIdAndSecret : url + "?" + clientIdAndSecret);
        }
        try {
            return HttpRequests.get(url, bodyHandler, authToken);
        } catch (Throwable e) {
            if (!(e instanceof InterruptedException)) {
                throw new RuntimeException(e);
            }
        }
        return getNullResponse(url);
    }

    @Override
    protected HttpResponse<R> run() throws Exception {
        HttpResponse<R> response = get(url);
        if (HealthResource.instance().getGitHubLastSeen() == 0) {
            HealthResource.instance().markGitHubLastSeen();
        }
        return response;
    }

    @Override
    protected HttpResponse<R> getFallback() {
        if (worker.isPresent()) {
            worker.get().executor().queue(worker.get());
        }
        return getNullResponse(url);
    }
}
