package no.cantara.docsite.oauth;

import no.cantara.docsite.client.HttpRequests;
import no.ssb.config.DynamicConfiguration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;

public class ObtainGitHubAccessToken implements Closeable {

    private static Logger LOG = LoggerFactory.getLogger(ObtainGitHubAccessToken.class);
    private static final String REDIRECT_URL = "http://localhost:%s/dump";

    private final DynamicConfiguration configuration;
    private final String redirectUrl;
    private final String oauthClientId;
    private final String oauthClientSecret;
    private final ChromeDriver driver;

    public ObtainGitHubAccessToken(DynamicConfiguration configuration, int port) {
        this.configuration = configuration;
        redirectUrl = String.format(REDIRECT_URL, String.valueOf(port));
        oauthClientId = configuration.evaluateToString("github.oauth2.client.clientId");
        oauthClientSecret = configuration.evaluateToString("github.oauth2.client.clientSecret");
        driver = new ChromeDriver();
    }

    private String getLoginURL() {
        try {
            String url = String.format("https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s&forcedPublicURL=true", oauthClientId, redirectUrl);
            HttpResponse<String> req = HttpRequests.get(url);
            if (req.statusCode() != HTTP_MOVED_TEMP) {
                return null;
            }

            String payload = req.body();
            Document doc = Jsoup.parse(payload);
            Element href = doc.body().getElementsByTag("a").get(0);
            String loginUrl = URLDecoder.decode(href.attr("href"), StandardCharsets.UTF_8.name());
            LOG.trace("Login-URL: {}", loginUrl);

            return loginUrl;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void initiateLogin() {
        driver.get(getLoginURL());
    }

    public boolean isLoginPage() {
        return driver.findElementById("login_field") != null; // check location url instead
    }

    public void loginWithCredentials() {
        driver.findElementById("login_field").sendKeys(configuration.evaluateToString("github.username"));
        driver.findElementById("password").sendKeys(configuration.evaluateToString("github.password"));
        driver.findElementByName("commit").click();
    }

    public boolean isAuthorizationPage() {
        try {
            return driver.findElementById("js-oauth-authorize-btn") != null; // check location url instead
        } catch (RuntimeException e) {
        }
        return false;
    }

    public void authorizeOAuthUser() {
        WebElement button = driver.findElement(By.id("js-oauth-authorize-btn"));
        while(!button.isEnabled()) {
            try {
                LOG.trace("Wait for authorization button to become available (sleep 250ms)");
                Thread.sleep(250);
            } catch (InterruptedException e) {
            }
        }
        new Actions(driver).moveToElement(button).click().perform();
    }

    public String getAuthorizationCode() {
        try {
            String currentUrl = driver.getCurrentUrl();
            return new URL(currentUrl).getQuery().split("=")[1];

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAccessToken(String authorizationCode) {
        String accessTokenUrl = String.format("https://github.com/login/oauth/access_token?client_id=%s&client_secret=%s&code=%s&redirect_uri=%s",
                oauthClientId, oauthClientSecret, authorizationCode, redirectUrl);
        LOG.trace("AccessToken-URL: {}", accessTokenUrl);

        HttpResponse<String> post = HttpRequests.post(accessTokenUrl, HttpRequest.BodyPublishers.noBody());
        if (post.statusCode() != HTTP_OK) {
            return null;
        }

        String accessTokenPayload = post.body();
        return accessTokenPayload.split("=")[1].split("&")[0];
    }

    @Override
    public void close() {
        driver.quit();
    }
}
