package no.cantara.docsite.commands;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import no.cantara.docsite.client.GitHubClient;
import no.cantara.docsite.executor.WorkerTask;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;

import static java.net.HttpURLConnection.HTTP_OK;

public class GetGitHubCommand<R> extends HystrixCommand<R> {

    private static final Logger LOG = LoggerFactory.getLogger(GetGitHubCommand.class);

    private final DynamicConfiguration configuration;
    private final WorkerTask worker;

    public GetGitHubCommand(DynamicConfiguration configuration, WorkerTask worker) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("GitHubGroup")));
        this.configuration = configuration;
        this.worker = worker;
        HystrixRequestContext.initializeContext();
    }

    @Override
    protected R run() throws Exception {
        GitHubClient client = new GitHubClient(configuration);
        HttpResponse<String> response = client.get("/rate_limit");
        if (response.statusCode() == HTTP_OK) {
            return (R) response.body();
        } else {
            return null;
        }
    }

    @Override
    protected R getFallback() {
        worker.getExecutor().queue(worker);
        return null;
    }
}
