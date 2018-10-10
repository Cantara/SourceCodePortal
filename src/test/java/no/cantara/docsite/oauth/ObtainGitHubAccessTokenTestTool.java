package no.cantara.docsite.oauth;

import no.cantara.docsite.test.ConfigurationOverride;
import no.cantara.docsite.test.client.ResponseHelper;
import no.cantara.docsite.test.client.TestClient;
import no.cantara.docsite.test.server.TestServer;
import no.cantara.docsite.test.server.TestServerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Inject;

@Listeners(TestServerListener.class)
public class ObtainGitHubAccessTokenTestTool {

    private static Logger LOG = LoggerFactory.getLogger(ObtainGitHubAccessTokenTestTool.class);

    @Inject
    TestServer server;

    @Inject
    TestClient client;

    @Ignore
    @Test
    public void testDumpContext() {
        ResponseHelper<String> response = client.get("/dump?foo=bar");
        LOG.trace("dump:\n{}", response.expect200Ok().body());
    }

    @Test
    @ConfigurationOverride({"http.port.test", "9091"})
    public void thatOAuth2AccessTokenIsObtained() {
        if (server.getConfiguration().evaluateToString("github.oauth2.client.clientId") == null
                || server.getConfiguration().evaluateToString("github.oauth2.client.clientSecret") == null
                || server.getConfiguration().evaluateToString("github.username") == null
                || server.getConfiguration().evaluateToString("github.password") == null) {
            LOG.error("You MUST 'specify github.oauth2.client.clientId', 'github.oauth2.client.clientSecret', 'github.username' and 'github.password' in 'security.properties'");
            return;
        }

        try (ObtainGitHubAccessToken githubAccessToken = new ObtainGitHubAccessToken(server.getConfiguration(), server.getTestServerServicePort())) {
            githubAccessToken.initiateLogin();

            if (githubAccessToken.isLoginPage()) {
                githubAccessToken.loginWithCredentials();
            }

            if (githubAccessToken.isAuthorizationPage()) {
                githubAccessToken.authorizeOAuthUser();
            }

            String authorizationCode = githubAccessToken.getAuthorizationCode();
            String accessToken = githubAccessToken.getAccessToken(authorizationCode);
            LOG.trace("\n-----------------------------------\n\nACCESS_TOKEN: {}\n\n-----------------------------------\n", accessToken);
        }
    }

}
