package no.cantara.docsite.commands;

import no.cantara.docsite.client.HttpRequests;
import no.cantara.docsite.executor.WorkerTask;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.util.Optional;

import static java.net.HttpURLConnection.HTTP_OK;

public class GetShieldsCommand<R> extends BaseResilientCommand<HttpResponse<R>> {

    private static final Logger LOG = LoggerFactory.getLogger(GetShieldsCommand.class);

    private final DynamicConfiguration configuration;
    private Optional<WorkerTask> worker;
    private String url;
    private HttpResponse.BodyHandler<R> bodyHandler;

    public GetShieldsCommand(String groupKey, DynamicConfiguration configuration, Optional<WorkerTask> worker, String url, HttpResponse.BodyHandler<R> bodyHandler) {
        super(groupKey);
        this.configuration = configuration;
        this.worker = worker;
        this.url = url;
        this.bodyHandler = bodyHandler;
    }

    private HttpResponse<R> get(String url) {
        return HttpRequests.getShieldsIO(url, bodyHandler);
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
    protected HttpResponse<R> handleFallback(Exception e) {
        String workerName = worker.map(w -> w.getClass().getSimpleName()).orElse("Unknown");
        String circuitState = getCircuitBreaker().getState().toString();
        LOG.error("{} failed. Circuit breaker state: {}. Error: {}", workerName, circuitState, e.getMessage());
        return getNullResponse(url);
    }
}
