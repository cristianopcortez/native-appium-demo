package br.com.ccortez.tests;

import br.com.ccortez.pages.RiderOptionsPage;
import br.com.ccortez.pages.TravelRequestPage;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AndroidAppTests extends BaseTest {

    @Test
    public void travelRequestTitleShouldBeDisplayed() {
        final TravelRequestPage travelRequestPage = new TravelRequestPage(this.driverManager.getDriver());

        System.out.println("Current Activity: " + this.driverManager.getDriver().currentActivity());
        takeScreenshot("screen_before_title_check_simplified");

        assertTrue(travelRequestPage.isPageLoaded(), "Travel Request Screen did not load!");

        String titleText = travelRequestPage.getTravelRequestTitleText();
        assertEquals(titleText, "Travel Request", "The title text should be 'Travel Request'");
    }

    @Test(priority = 3)
    public void requestTravelEndToEnd() {
        final TravelRequestPage travelRequestPage = new TravelRequestPage(this.driverManager.getDriver());

        String titleText = travelRequestPage.getTravelRequestTitleText();
        assertEquals(titleText, "Travel Request", "The title text should be 'Travel Request'");

        travelRequestPage.enterUserId("12345", "Id do usuário");
        travelRequestPage.enterOriginAddress("Av. Brasil, 2033 - Jardim America, São Paulo - SP, 01431-001", "Endereço de origem");
        travelRequestPage.enterDestinyAddress("Av. Paulista, 1538 - Bela Vista, São Paulo - SP, 01310-200", "Endereço de destino");
        travelRequestPage.clickRequestTravelButton();

        // No Thread.sleep here on purpose: getAvailableRidersTitleText() already
        // wraps a WebDriverWait(visibilityOfElementLocated(...)) that polls until
        // the next screen is actually ready, so adding a blind sleep would only
        // penalise fast machines without protecting slow ones.
        final RiderOptionsPage riderOptionsPage = new RiderOptionsPage(this.driverManager.getDriver());
        assertEquals(riderOptionsPage.getAvailableRidersTitleText(), "Available Riders");
    }

    public void takeScreenshot(String filename) {
        File scrFile = ((TakesScreenshot) this.driverManager.getDriver()).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(scrFile, new File(filename + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
