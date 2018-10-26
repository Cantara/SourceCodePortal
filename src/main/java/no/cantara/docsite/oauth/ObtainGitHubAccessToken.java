package no.cantara.docsite.oauth;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import no.cantara.docsite.Application;
import no.cantara.docsite.client.HttpRequests;
import no.cantara.docsite.util.CommonUtil;
import no.cantara.docsite.util.JavaUtilLoggerBridge;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;

public class ObtainGitHubAccessToken implements Closeable {

    private static final String REDIRECT_URL = "http://localhost:%s/dump";
    private static Logger LOG = LoggerFactory.getLogger(ObtainGitHubAccessToken.class);
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

        ChromeOptions options = new ChromeOptions();
        //options.addArguments("start-maximized"); // open Browser in maximized mode
        //options.addArguments("disable-infobars"); // disabling infobars
        options.addArguments("--headless");
        options.addArguments("--disable-extensions"); // disabling extensions
        options.addArguments("--disable-gpu"); // applicable to windows os only
        options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
        options.addArguments("--no-sandbox"); // Bypass OS security model
        LOG.debug("ChromeOptions: {}", options.asMap());
        driver = new ChromeDriver(options);
    }

    public static final void main(String[] args) {
        long now = System.currentTimeMillis();

        LoggerContext loggerContext = ((ch.qos.logback.classic.Logger) LOG).getLoggerContext();
        URL mainURL = ConfigurationWatchListUtil.getMainWatchURL(loggerContext);
        LOG.info("Logback used '{}' as the configuration file.", mainURL);

        DynamicConfiguration configuration = new StoreBasedDynamicConfiguration.Builder()
                .propertiesResource(Application.getDefaultConfigurationResourcePath())
                .propertiesResource("application.properties")
                .values("http.host", "127.0.0.1")
                .values("http.port", "9091")
                .propertiesResource("security.properties")
                .propertiesResource("application_override.properties")
                .environment("SCP_")
                .systemProperties()
                .build();

        JavaUtilLoggerBridge.installJavaUtilLoggerBridgeHandler(Level.INFO);

        Application application = Application.initialize(configuration);

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOG.warn("ShutdownHook triggered..");
                application.stop();
            }));


            application.start();

            long time = System.currentTimeMillis() - now;
            LOG.info("Server started in {}ms..", time);


            if (configuration.evaluateToString("github.oauth2.client.clientId") == null
                    || configuration.evaluateToString("github.oauth2.client.clientSecret") == null
            ) {
                LOG.error("You MUST 'specify github.oauth2.client.clientId', 'github.oauth2.client.clientSecret' in 'security.properties'");
                return;
            }

            try (ObtainGitHubAccessToken githubAccessToken = new ObtainGitHubAccessToken(configuration, application.getPort())) {
                githubAccessToken.initiateLogin();

                if (githubAccessToken.isLoginPage()) {
                    String uid = (configuration.evaluateToString("github.username") == null ? InputField.readInput("Enter username: ") : configuration.evaluateToString("github.username"));
                    String pwd = (configuration.evaluateToString("github.password") == null ? PasswordField.readPassword("Enter password: ") : configuration.evaluateToString("github.password"));

                    githubAccessToken.loginWithCredentials(uid, pwd);
                }

                if (githubAccessToken.isTwoFactorAuthentication()) {
                    String otp = InputField.readInput("Enter OTP-code: ");
                    githubAccessToken.verifyTwoFactorAuthentication(otp);
                }

                if (githubAccessToken.isAuthorizationPage()) {
                    githubAccessToken.authorizeOAuthUser();
                }

                String authorizationCode = githubAccessToken.getAuthorizationCode();
                String accessToken = githubAccessToken.getAccessToken(authorizationCode);
                LOG.trace("\n-----------------------------------\n\nACCESS_TOKEN: {}\n\n-----------------------------------\n", accessToken);
            } catch (Exception e) {
                LOG.error("Error obtaining GitHubAccessToken: {}", CommonUtil.captureStackTrace(e));

            }


        } finally {
            application.stop();
        }
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

    public void loginWithCredentials(String username, String password) {
        driver.findElementById("login_field").sendKeys(username);
        driver.findElementById("password").sendKeys(password);
        driver.findElementByName("commit").click();
    }

    private boolean isTwoFactorAuthentication() {
        try {
            return driver.findElementById("otp") != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private void verifyTwoFactorAuthentication(String otp) {
        driver.findElementById("otp").sendKeys(otp);
        WebElement button = driver.findElement(By.xpath("//*[@id=\"login\"]/div[5]/form/button"));
        while (!button.isEnabled()) {
            try {
                LOG.trace("Wait for authorization button to become available (sleep 250ms)");
                Thread.sleep(250);
            } catch (InterruptedException e) {
            }
        }
        new Actions(driver).moveToElement(button).click().perform();
    }

    public boolean isAuthorizationPage() {
        try {
            return driver.findElementById("js-oauth-authorize-btn") != null; // check location url instead
        } catch (RuntimeException e) {
            return false;
        }
    }

    public void authorizeOAuthUser() {
        WebElement button = driver.findElement(By.id("js-oauth-authorize-btn"));
        while (!button.isEnabled()) {
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

    //
    // ---------------------------------------------------------------------------------------------------------
    //

    // http://www.cse.chalmers.se/edu/course/TDA602/Eraserlab/pwdmasking.html

    static class MaskingThread extends Thread {
        private volatile boolean stop;
        private char echochar = '*';

        public MaskingThread(String prompt) {
            System.out.print(prompt);
        }

        public void run() {

            int priority = Thread.currentThread().getPriority();
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            try {
                stop = true;
                while (stop) {
                    System.out.print("\010" + echochar);
                    try {
                        // attempt masking at this rate
                        Thread.currentThread().sleep(1);
                    } catch (InterruptedException iex) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            } finally {
                Thread.currentThread().setPriority(priority);
            }
        }

        public void stopMasking() {
            this.stop = false;
        }
    }

    static class InputField {
        public static String readInput(String prompt) {
            System.out.print(prompt);
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String input = "";
            try {
                input = in.readLine();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return input;
        }
    }

    static class PasswordField {
        public static String readPassword(String prompt) {
            MaskingThread et = new MaskingThread(prompt);
            Thread mask = new Thread(et);
            mask.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String password = "";

            try {
                password = in.readLine();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            et.stopMasking();
            return password;
        }
    }
}

