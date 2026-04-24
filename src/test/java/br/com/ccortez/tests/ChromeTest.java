package br.com.ccortez.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import br.com.ccortez.drivers.WebDriverFactory;

public class ChromeTest {

    private WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setupTest() {
        driver = WebDriverFactory.createChromeDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void seleniumWebDriverPageLoads() {
        driver.get("https://bonigarcia.dev/selenium-webdriver-java/");
        String title = driver.getTitle();

        assertThat(title).contains("Selenium WebDriver");
    }

}
