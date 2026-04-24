package br.com.ccortez.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class RiderOptionsPage {

    private static final String APP_ID = "br.com.ccortez.desafioshopperapptaxi:id/";

    private final AndroidDriver driver;
    private final WebDriverWait wait;

    public RiderOptionsPage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // --- Locators ---

    private By availableRidersTitleLocator() {
        return By.tagName("availableRidersTitle");
    }

    private By riderOptionCardLocator(int index) {
        return By.id(APP_ID + "riderOptionCard_" + index);
    }

    private By riderOptionNameLocator(int index) {
        return By.id(APP_ID + "riderOptionName_" + index);
    }

    private By riderOptionDescriptionLocator(int index) {
        return By.id(APP_ID + "riderOptionDescription_" + index);
    }

    private By riderOptionVehicleLocator(int index) {
        return By.id(APP_ID + "riderOptionVehicle_" + index);
    }

    private By riderOptionRatingLocator(int index) {
        return By.id(APP_ID + "riderOptionRating_" + index);
    }

    private By riderOptionPriceLocator(int index) {
        return By.id(APP_ID + "riderOptionPrice_" + index);
    }

    private By confirmRideButtonLocator(int index) {
        return By.id(APP_ID + "confirmRideButton_" + index);
    }

    private By loadingIndicatorLocator() {
        return By.id(APP_ID + "loadingIndicatorWithText");
    }

    private By errorScreenLocator() {
        return By.id(APP_ID + "errorScreen");
    }

    private By emptyListScreenLocator() {
        return By.id(APP_ID + "emptyListScreen");
    }

    private By confirmDialogTitleLocator() {
        return By.id(APP_ID + "confirmDialogTitle");
    }

    private By confirmDialogTextLocator() {
        return By.id(APP_ID + "confirmDialogText");
    }

    private By confirmDialogConfirmButtonLocator() {
        return By.id(APP_ID + "confirmDialogConfirmButton");
    }

    // --- Element Access Methods ---

    public WebElement availableRidersTitle() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(availableRidersTitleLocator()));
    }

    public WebElement riderOptionCard(int index) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(riderOptionCardLocator(index)));
    }

    public WebElement riderOptionName(int index) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(riderOptionNameLocator(index)));
    }

    public WebElement riderOptionDescription(int index) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(riderOptionDescriptionLocator(index)));
    }

    public WebElement riderOptionVehicle(int index) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(riderOptionVehicleLocator(index)));
    }

    public WebElement riderOptionRating(int index) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(riderOptionRatingLocator(index)));
    }

    public WebElement riderOptionPrice(int index) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(riderOptionPriceLocator(index)));
    }

    public WebElement confirmRideButton(int index) {
        return wait.until(ExpectedConditions.elementToBeClickable(confirmRideButtonLocator(index)));
    }

    public WebElement loadingIndicator() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(loadingIndicatorLocator()));
    }

    public WebElement errorScreen() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(errorScreenLocator()));
    }

    public WebElement emptyListScreen() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(emptyListScreenLocator()));
    }

    public WebElement confirmDialogTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(confirmDialogTitleLocator()));
    }

    public WebElement confirmDialogText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(confirmDialogTextLocator()));
    }

    public WebElement confirmDialogConfirmButton() {
        return wait.until(ExpectedConditions.elementToBeClickable(confirmDialogConfirmButtonLocator()));
    }

    // --- Action Methods ---

    public String getAvailableRidersTitleText() {
        // Use visibilityOfElementLocated, NOT presenceOfElementLocated:
        // Compose elements are often present in the tree before they are
        // actually drawn, and reading their text prematurely returns "".
        return wait.until(ExpectedConditions.visibilityOfElementLocated(availableRidersTitleLocator())).getText();
    }

    public void clickRiderOptionCard(int index) {
        riderOptionCard(index).click();
    }

    public String getRiderOptionNameText(int index) {
        return riderOptionName(index).getText();
    }

    public String getRiderOptionDescriptionText(int index) {
        return riderOptionDescription(index).getText();
    }

    public String getRiderOptionVehicleText(int index) {
        return riderOptionVehicle(index).getText();
    }

    public String getRiderOptionRatingText(int index) {
        return riderOptionRating(index).getText();
    }

    public String getRiderOptionPriceText(int index) {
        return riderOptionPrice(index).getText();
    }

    public void clickConfirmRideButton(int index) {
        confirmRideButton(index).click();
    }

    public boolean isLoadingIndicatorDisplayed() {
        return loadingIndicator().isDisplayed();
    }

    public boolean isErrorScreenDisplayed() {
        return errorScreen().isDisplayed();
    }

    public boolean isEmptyListScreenDisplayed() {
        return emptyListScreen().isDisplayed();
    }

    public String getConfirmDialogTitleText() {
        return confirmDialogTitle().getText();
    }

    public String getConfirmDialogText() {
        return confirmDialogText().getText();
    }

    public void clickConfirmDialogConfirmButton() {
        confirmDialogConfirmButton().click();
    }

    // --- Helper methods ---

    public List<WebElement> getAllRiderOptionCards() {
        return driver.findElements(riderOptionCardLocator(0)).stream()
                .map(element -> driver.findElement(By.id(element.getAttribute("resource-id").replace("_0", ""))))
                .collect(Collectors.toList());
    }

    // --- Verification Methods ---

    public boolean isPageLoaded() {
        return availableRidersTitle().isDisplayed();
    }

    public int getNumberOfRiderOptions() {
        return getAllRiderOptionCards().size();
    }
}
