package no.cantara.docsite.commands;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import no.cantara.docsite.client.HttpRequests;
import no.cantara.docsite.executor.WorkerTask;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.util.Optional;

import static java.net.HttpURLConnection.HTTP_OK;

public class GetCommand<R> extends BaseHystrixCommand<HttpResponse<R>> {

    private static final Logger LOG = LoggerFactory.getLogger(GetCommand.class);

    private final DynamicConfiguration configuration;
    private Optional<WorkerTask> worker;
    private String url;
    private HttpResponse.BodyHandler<R> bodyHandler;

    public GetCommand(String groupKey, DynamicConfiguration configuration, Optional<WorkerTask> worker, String url, HttpResponse.BodyHandler<R> bodyHandler) {
        super(groupKey);
        this.configuration = configuration;
        this.worker = worker;
        this.url = url;
        this.bodyHandler = bodyHandler;
        HystrixRequestContext.initializeContext();
    }

    private HttpResponse<R> get(String url) {
        return HttpRequests.get(url, bodyHandler);
    }

    @Override
    protected HttpResponse<R> run() throws Exception {
        try {
            HttpResponse<R> response = get(url);
            if (configuration.evaluateToBoolean("http.hystrix.writeToFile") && response.statusCode() == HTTP_OK) {
                ifDumpToFile(url, (HttpResponse<String>) response);
            }
            return response;
        } catch (Throwable e) {
            if (!(e instanceof InterruptedException)) {
                throw new RuntimeException(e);
            }
        }
        return getNullResponse(url);
    }

    @Override
    protected HttpResponse<R> getFallback() {
        try {
            LOG.error("{} -> {}", getExecutionEvents(), getFailedExecutionException().getMessage());
        } catch (Throwable e) {
            LOG.error("Error logging fallback");
        }
        if (worker.isPresent()) {
            worker.get().executor().queue(worker.get());
        }
        return getNullResponse(url);
    }
}
