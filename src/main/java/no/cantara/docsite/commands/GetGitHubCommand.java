package no.cantara.docsite.commands;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import no.cantara.docsite.client.HttpRequests;
import no.cantara.docsite.executor.WorkerTask;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class GetGitHubCommand<R> extends BaseHystrixCommand<HttpResponse<R>> {

    private static final Logger LOG = LoggerFactory.getLogger(GetGitHubCommand.class);

    private final DynamicConfiguration configuration;
    private Optional<WorkerTask> worker;
    private String uri;
    private HttpResponse.BodyHandler<R> bodyHandler;

    public GetGitHubCommand(String groupKey, DynamicConfiguration configuration, Optional<WorkerTask> worker, String uri, HttpResponse.BodyHandler<R> bodyHandler) {
        super(groupKey);
        this.configuration = configuration;
        this.worker = worker;
        this.uri = uri;
        this.bodyHandler = bodyHandler;
        HystrixRequestContext.initializeContext();
    }

    private String[] getGitHubAuthHeader(DynamicConfiguration configuration) {
        return new String[]{"Authorization", String.format("token %s", configuration.evaluateToString("github.client.accessToken"))};
    }

    private HttpResponse<R> get(String uri) {
        String[] authToken = null;
        if (configuration.evaluateToString("github.client.accessToken") != null) {
            authToken = getGitHubAuthHeader(configuration);
        }
        return HttpRequests.get("https://api.github.com" + uri, bodyHandler, authToken);
    }

    @Override
    protected HttpResponse<R> run() throws Exception {
        HttpResponse<R> response = get(uri);
        return response;
    }

    @Override
    protected HttpResponse<R> getFallback() {
        if (worker.isPresent()) {
            worker.get().getExecutor().queue(worker.get());
        }
        return new HttpResponse<>() {
            @Override
            public int statusCode() {
                return 500;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<R>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return null;
            }

            @Override
            public R body() {
                return null;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return null;
            }

            @Override
            public HttpClient.Version version() {
                return null;
            }
        };
    }
}
