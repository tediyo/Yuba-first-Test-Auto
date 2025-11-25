package com.example.automation.support;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;

/**
 * Simple thread-safe factory for creating and cleaning up WebDriver instances.
 * Uses local ChromeDriver from the drivers folder.
 */
public final class DriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverFactory() {
        // utility class
    }

    public static void initDriver() {
        if (DRIVER.get() == null) {
            // Get the drivers folder path - try multiple possible locations
            String currentDir = System.getProperty("user.dir");
            File driverFile = null;
            
            // Try paths in order of likelihood
            String[] possiblePaths = {
                Paths.get(currentDir, "drivers", "msedgedriver.exe").toString(),
                Paths.get(currentDir, "selenium-bdd", "drivers", "msedgedriver.exe").toString(),
                Paths.get(currentDir, "..", "drivers", "msedgedriver.exe").toString()
            };
            
            for (String path : possiblePaths) {
                File testFile = new File(path);
                if (testFile.exists() && testFile.isFile()) {
                    driverFile = testFile;
                    break;
                }
            }
            
            if (driverFile == null) {
                throw new RuntimeException("EdgeDriver not found. Searched in:\n" +
                    String.join("\n", possiblePaths) + 
                    "\nPlease ensure msedgedriver.exe is in the drivers folder.");
            }
            
            String driverPath = driverFile.getAbsolutePath();
            
            // Set system property for EdgeDriver
            System.setProperty("webdriver.edge.driver", driverPath);
            
            // Create EdgeDriverService with the local driver
            EdgeDriverService service = new EdgeDriverService.Builder()
                .usingDriverExecutable(driverFile)
                .build();
            
            EdgeOptions options = new EdgeOptions();
            options.addArguments("--window-size=1280,800");
            options.addArguments("--start-maximized");
            options.addArguments("--disable-blink-features=AutomationControlled");
            
            WebDriver driver = new EdgeDriver(service, options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            DRIVER.set(driver);
        }
    }

    public static WebDriver getDriver() {
        return DRIVER.get();
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
        }
    }
}

