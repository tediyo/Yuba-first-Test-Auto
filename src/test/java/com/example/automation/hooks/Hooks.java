package com.example.automation.hooks;

import com.example.automation.support.DriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import org.openqa.selenium.WebDriver;

public class Hooks {

    private static boolean driverInitialized = false;

    @BeforeAll
    public static void setUpOnce() {
        // Initialize driver only once for all scenarios
        if (!driverInitialized) {
            DriverFactory.initDriver();
            driverInitialized = true;
        }
    }

    @Before
    public void setUp() {
        // Ensure driver is initialized (fallback if BeforeAll doesn't work)
        if (!driverInitialized) {
            DriverFactory.initDriver();
            driverInitialized = true;
        }
        
        // Clear cookies between scenarios but keep browser open
        WebDriver driver = DriverFactory.getDriver();
        if (driver != null) {
            driver.manage().deleteAllCookies();
            // Navigate to blank page to reset state
            driver.navigate().to("about:blank");
        }
    }

    @After
    public void tearDown() {
        // Don't quit driver after each scenario - keep it open for next scenario
        // Just clear any temporary state if needed
    }

    @AfterAll
    public static void tearDownOnce() {
        // Generate custom HTML report
        try {
            com.example.automation.reporting.CustomHtmlReportGenerator.generateCustomReport();
        } catch (Exception e) {
            System.err.println("Failed to generate custom report: " + e.getMessage());
        }
        
        // Quit driver only once at the end of all scenarios
        if (driverInitialized) {
            DriverFactory.quitDriver();
            driverInitialized = false;
        }
    }
}

