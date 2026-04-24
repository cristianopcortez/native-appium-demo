package br.com.ccortez.tests;

import br.com.ccortez.pages.TravelRequestPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Smoke suite that proves a single JVM process can hold a Selenium
 * {@link org.openqa.selenium.WebDriver} and an Appium Android driver at the
 * same time (see {@link BaseMixedDriverTest}). The tests do not share state
 * yet — a genuine cross-platform scenario (e.g. web action observed by the
 * mobile app) is a natural next addition.
 */
public class MixedDriverTests extends BaseMixedDriverTest {

    @Test(priority = 1)
    public void webPageInteraction() {
        driver.get("https://bonigarcia.dev/selenium-webdriver-java/");
        String title = driver.getTitle();

        Assert.assertTrue(title.contains("Selenium WebDriver"));
        quit();
    }

    @Test(priority = 2)
    public void testTravelRequestTitle() {
        final TravelRequestPage travelRequestPage = new TravelRequestPage(this.driverManager.getDriver());

        String titleText = travelRequestPage.getTravelRequestTitleText();

        System.out.println("Title text: " + titleText);
        assertEquals(titleText, "Travel Request");
    }

}
