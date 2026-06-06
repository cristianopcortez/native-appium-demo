package br.com.ccortez.tests;

import br.com.ccortez.drivers.AndroidDriverManager;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.net.MalformedURLException;


public class BaseTest {

    protected AndroidDriverManager driverManager;

    @BeforeClass(alwaysRun = true)
    public void setup() throws MalformedURLException {
        this.driverManager = new AndroidDriverManager();
        this.driverManager.createAndroidDriver();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        this.driverManager.quitDriver();
        // Allow the Espresso instrumentation process to fully terminate before the
        // next test class tries to start a new am-instrument session on the same device.
        // Without this gap, the next session's am instrument conflicts with the still-alive
        // instrumentation process of this session, causing a "Process crashed" failure.
        try {
            Thread.sleep(15_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
