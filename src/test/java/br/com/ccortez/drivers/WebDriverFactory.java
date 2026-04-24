package br.com.ccortez.drivers;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Factory for Selenium {@link WebDriver} instances used by the web suite.
 *
 * <p>Two execution modes are supported, selected at runtime:</p>
 * <ul>
 *     <li><b>Local</b> (default): a {@link ChromeDriver} is started against the
 *     host's Chrome installation, with the driver binary resolved by
 *     WebDriverManager. This is the fastest dev-loop option on developer
 *     machines.</li>
 *     <li><b>Remote</b>: when {@code SELENIUM_REMOTE_URL} is set (as a system
 *     property or environment variable), a {@link RemoteWebDriver} is created
 *     pointing at that URL. This is used with the {@code selenium-chrome}
 *     service in {@code docker-compose.yml} to run Chrome fully containerised,
 *     which gives a reproducible browser version for CI.</li>
 * </ul>
 *
 * <p>Example remote URL: {@code http://localhost:4444/wd/hub}.</p>
 */
public final class WebDriverFactory {

    public static final String REMOTE_URL_KEY = "SELENIUM_REMOTE_URL";

    private WebDriverFactory() {
    }

    /**
     * Builds a Chrome {@link WebDriver} using the mode selected by the
     * {@link #REMOTE_URL_KEY} property / environment variable.
     */
    public static WebDriver createChromeDriver() {
        return createChromeDriver(new ChromeOptions());
    }

    /**
     * Same as {@link #createChromeDriver()} but lets the caller tweak the
     * {@link ChromeOptions} (e.g. add {@code --headless=new}).
     */
    public static WebDriver createChromeDriver(ChromeOptions options) {
        String remoteUrl = resolve(REMOTE_URL_KEY);

        if (remoteUrl == null || remoteUrl.isBlank()) {
            WebDriverManager.chromedriver().setup();
            return new ChromeDriver(options);
        }

        try {
            return new RemoteWebDriver(new URL(remoteUrl), options);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(
                    "Invalid " + REMOTE_URL_KEY + ": " + remoteUrl, e);
        }
    }

    private static String resolve(String key) {
        String value = System.getProperty(key);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return System.getenv(key);
    }
}
