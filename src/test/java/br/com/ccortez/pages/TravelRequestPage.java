package br.com.ccortez.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class TravelRequestPage {

    private final AndroidDriver driver;
    private final WebDriverWait wait;

    public TravelRequestPage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // --- Locators ---

    private By travelRequestTitleLocator() {
        return By.tagName("travelRequestTitle");
    }

    private By userIdFieldLocator(String label) {
        return By.tagName(label);
    }

    private By originAddressFieldLocator(String label) {
        return By.tagName(label);
    }

    private By destinyAddressFieldLocator(String label) {
        return By.tagName(label);
    }

    private By requestTravelButtonLocator() {
        return By.tagName("requestTravelButton");
    }

    // --- Element Access Methods ---

    public String getTravelRequestTitleText() {
        // Use visibilityOfElementLocated, NOT presenceOfElementLocated:
        // Compose elements are often present in the tree before they are
        // actually drawn, and reading their text prematurely returns "".
        return wait.until(ExpectedConditions.visibilityOfElementLocated(travelRequestTitleLocator())).getText();
    }

    public WebElement userIdField(String label) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(userIdFieldLocator(label)));
    }

    public WebElement originAddressField(String label) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(originAddressFieldLocator(label)));
    }

    public WebElement destinyAddressField(String label) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(destinyAddressFieldLocator(label)));
    }

    public WebElement requestTravelButton() {
        return wait.until(ExpectedConditions.elementToBeClickable(requestTravelButtonLocator()));
    }

    // --- Action Methods ---

    public void enterUserId(String userId, String label) {
        userIdField(label).sendKeys(userId);
    }

    public void enterOriginAddress(String originAddress, String label) {
        originAddressField(label).sendKeys(originAddress);
    }

    public void enterDestinyAddress(String destinyAddress, String label) {
        destinyAddressField(label).sendKeys(destinyAddress);
    }

    public void clickRequestTravelButton() {
        requestTravelButton().click();
    }

    public boolean isPageLoaded() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(travelRequestTitleLocator())).isDisplayed();
    }
}
