package no.cantara.docsite.commands;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import no.cantara.docsite.client.HttpRequests;
import no.cantara.docsite.executor.WorkerTask;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.util.Optional;

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
        HttpResponse<R> response = get(url);
        return response;
    }

    @Override
    protected HttpResponse<R> getFallback() {
        LOG.error("{} -> {}", getExecutionEvents(), getFailedExecutionException().getMessage());
        if (worker.isPresent()) {
            worker.get().getExecutor().queue(worker.get());
        }
        return getNullResponse(url);
    }
}
