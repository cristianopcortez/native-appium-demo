package br.com.ccortez.tests;

import br.com.ccortez.drivers.AndroidDriverManager;
import br.com.ccortez.drivers.WebDriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.net.MalformedURLException;

/**
 * Base class for suites that need <em>both</em> an Android {@link AndroidDriverManager}
 * and a Selenium {@link WebDriver} instantiated on the same JVM process.
 * Intended for cross-platform flows that hop between a web page and the
 * companion mobile app within a single scenario.
 */
public class BaseMixedDriverTest {

    protected AndroidDriverManager driverManager;
    protected WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setup() throws MalformedURLException {
        driver = WebDriverFactory.createChromeDriver();

        this.driverManager = new AndroidDriverManager();
        this.driverManager.createAndroidDriver();
    }

    protected void quit() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        quit();
        if (this.driverManager != null) {
            this.driverManager.quitDriver();
        }
    }
}
