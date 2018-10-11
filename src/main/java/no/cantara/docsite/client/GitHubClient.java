package no.cantara.docsite.client;

import no.ssb.config.DynamicConfiguration;

import java.net.http.HttpResponse;

public class GitHubClient {

    private final DynamicConfiguration configuration;

    public GitHubClient(DynamicConfiguration configuration) {
        this.configuration = configuration;
    }

    private String[] getGitHubAuthHeader(DynamicConfiguration configuration) {
        return new String[]{"Authorization", String.format("token %s", configuration.evaluateToString("github.client.accessToken"))};
    }

    public HttpResponse<String> get(String uri) {
        String[] authToken = null;
        if (configuration.evaluateToString("github.client.accessToken") != null) {
            authToken = getGitHubAuthHeader(configuration);
        }
        return HttpRequests.get("https://api.github.com" + uri, authToken);
    }

}
