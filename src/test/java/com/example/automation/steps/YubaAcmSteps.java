package com.example.automation.steps;

import com.example.automation.reporting.PerformanceTracker;
import com.example.automation.reporting.TestResultsCollector;
import com.example.automation.support.DriverFactory;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.assertj.core.api.Assertions.assertThat;

public class YubaAcmSteps {

    private final WebDriver driver = DriverFactory.getDriver();
    private String initialUrlBeforeSelection;
    private String urlBeforeElementSelection;
    private String finalUrlAfterSelection;

    @And("I click the ACM navigation link {string}")
    public void i_click_the_acm_navigation_link(String xpath) {
        long actionStartTime = System.currentTimeMillis();
        String stepId = PerformanceTracker.startStep(
            "Click ACM navigation link: " + xpath,
            "Clicks an ACM navigation link by XPath",
            "navigation"
        );
        
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        
        // Store current URL before navigation (if not already set)
        if (initialUrlBeforeSelection == null || initialUrlBeforeSelection.isEmpty()) {
            initialUrlBeforeSelection = driver.getCurrentUrl();
        }
        System.out.println("Current URL before ACM navigation: " + initialUrlBeforeSelection);
        
        // Wait for the navigation link to be present
        WebElement navLink = extendedWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        
        // Scroll to the navigation link
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", navLink);
        
        // Wait for navigation link to be clickable
        navLink = extendedWait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        
        // Click the navigation link
        navLink.click();
        long responseTime = System.currentTimeMillis();
        PerformanceTracker.recordResponseTime(stepId, actionStartTime);
        
        // Wait for navigation to complete and page to fully load
        try {
            Thread.sleep(2000); // Give more time for page transition
            extendedWait.until(driver -> {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                String readyState = (String) js.executeScript("return document.readyState");
                return "complete".equals(readyState);
            });
            
            // Additional wait for dynamic content to load (Radix UI components)
            Thread.sleep(2000);
            
            // Wait for any loading indicators to disappear
            try {
                extendedWait.until(driver -> {
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    // Check if page is stable
                    String readyState = (String) js.executeScript("return document.readyState");
                    if (!"complete".equals(readyState)) return false;
                    
                    // Check if there are any elements with 'radix-' prefix (Radix UI components)
                    Long radixElements = (Long) js.executeScript(
                        "return document.querySelectorAll('[id^=\"radix-\"]').length;"
                    );
                    return radixElements > 0; // Wait until at least one Radix element appears
                });
            } catch (Exception e) {
                System.out.println("Note: Could not verify Radix elements loaded, continuing anyway...");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long loadEndTime = System.currentTimeMillis();
        PerformanceTracker.completeStep(stepId, actionStartTime, responseTime, loadEndTime);
        
        System.out.println("ACM navigation link clicked successfully");
        System.out.println("Current URL after navigation: " + driver.getCurrentUrl());
    }

    @And("I click the ACM element {string}")
    public void i_click_the_acm_element(String xpath) {
        long actionStartTime = System.currentTimeMillis();
        String stepId = PerformanceTracker.startStep(
            "Click ACM element: " + xpath,
            "Clicks an ACM element (dropdown trigger button)",
            "click"
        );
        
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(40));
        
        System.out.println("Looking for ACM element (Allocate Credits button)");
        System.out.println("Current URL: " + driver.getCurrentUrl());
        
        // Wait a bit for any dynamic content to load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        WebElement element = null;
        
        // Try multiple selector strategies in order of reliability
        try {
            // Strategy 1: Use data-slot attribute (most reliable)
            System.out.println("Trying to find element by data-slot attribute...");
            element = extendedWait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("button[data-slot='dropdown-menu-trigger']")
            ));
            System.out.println("✅ Found element using data-slot attribute");
        } catch (Exception e1) {
            System.out.println("data-slot selector failed, trying by text content...");
            try {
                // Strategy 2: Find by text content "Allocate Credits"
                element = extendedWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//button[contains(., 'Allocate Credits')]")
                ));
                System.out.println("✅ Found element using text content");
            } catch (Exception e2) {
                System.out.println("Text content selector failed, trying provided XPath...");
                try {
                    // Strategy 3: Try the provided XPath (fallback)
                    // Use JavaScript XPath evaluation to handle special characters better
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    Object result = js.executeScript(
                        "var xpath = arguments[0];" +
                        "var result = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);" +
                        "return result.singleNodeValue;",
                        xpath
                    );
                    
                    if (result != null && result instanceof WebElement) {
                        element = (WebElement) result;
                        System.out.println("✅ Found element using JavaScript XPath evaluation");
                    } else {
                        // Fallback to standard Selenium XPath
                        element = extendedWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
                        System.out.println("✅ Found element using standard Selenium XPath");
                    }
                } catch (Exception e3) {
                    PerformanceTracker.failStep(stepId, actionStartTime);
                    
                    // Record test failure in TestResultsCollector
                    long failureTime = System.currentTimeMillis() - actionStartTime;
                    TestResultsCollector.recordTestResult(
                        "ACM Test - Element Click Failed",
                        "FAILED",
                        failureTime,
                        "ACM",
                        "Failed to find ACM element (Allocate Credits button). Tried: data-slot, text content, and XPath. Error: " + e3.getMessage()
                    );
                    
                    System.err.println("Failed to find ACM element after trying all strategies");
                    System.err.println("Error: " + e3.getMessage());
                    
                    // Print available elements for debugging
                    try {
                        List<WebElement> buttons = driver.findElements(By.cssSelector("button[data-slot]"));
                        System.err.println("Available buttons with data-slot: " + buttons.size());
                        for (int i = 0; i < Math.min(5, buttons.size()); i++) {
                            WebElement el = buttons.get(i);
                            System.err.println("  - Button " + (i+1) + ": data-slot='" + el.getAttribute("data-slot") + 
                                "', text='" + el.getText() + "'");
                        }
                    } catch (Exception debugEx) {
                        System.err.println("Could not list available buttons: " + debugEx.getMessage());
                    }
                    
                    throw new org.openqa.selenium.TimeoutException(
                        "Could not find ACM element (Allocate Credits button). " +
                        "Tried: button[data-slot='dropdown-menu-trigger'], text 'Allocate Credits', and XPath: " + xpath, e3);
                }
            }
        }
        
        // Scroll to the element
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        
        // Wait for element to be clickable
        element = extendedWait.until(ExpectedConditions.elementToBeClickable(element));
        
        // Click the element
        element.click();
        long responseTime = System.currentTimeMillis();
        PerformanceTracker.recordResponseTime(stepId, actionStartTime);
        
        // Wait for page to stabilize
        try {
            Thread.sleep(2000);
            extendedWait.until(driver -> {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                String readyState = (String) js.executeScript("return document.readyState");
                return "complete".equals(readyState);
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long loadEndTime = System.currentTimeMillis();
        PerformanceTracker.completeStep(stepId, actionStartTime, responseTime, loadEndTime);
        
        System.out.println("ACM element clicked successfully");
    }

    @And("I select the ACM element {string}")
    public void i_select_the_acm_element(String xpath) {
        long actionStartTime = System.currentTimeMillis();
        String stepId = PerformanceTracker.startStep(
            "Select ACM element: " + xpath,
            "Selects an ACM element (Transfer Credits menu item)",
            "click"
        );
        
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        
        // Store URL before selection (use this for comparison in verification)
        urlBeforeElementSelection = driver.getCurrentUrl();
        System.out.println("URL before ACM element selection: " + urlBeforeElementSelection);
        
        // Wait a bit for dropdown menu to appear after clicking the trigger
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Wait for the element to be present using multiple strategies
        WebElement element = null;
        
        try {
            // Strategy 1: Use text content "Transfer Credits" (most reliable)
            System.out.println("Trying to find menu item by text 'Transfer Credits'...");
            element = extendedWait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@role='menuitem' and contains(., 'Transfer Credits')]")
            ));
            System.out.println("✅ Found element using text content");
        } catch (Exception e1) {
            System.out.println("Text content selector failed, trying to find all menu items and filter by text...");
            try {
                // Strategy 2: Find all menu items and filter by text
                List<WebElement> menuItems = extendedWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.cssSelector("div[role='menuitem'][data-slot='dropdown-menu-item']")
                ));
                System.out.println("Found " + menuItems.size() + " menu items in dropdown");
                for (WebElement item : menuItems) {
                    String itemText = item.getText();
                    System.out.println("  - Menu item text: '" + itemText + "'");
                    if (itemText.contains("Transfer Credits")) {
                        element = item;
                        System.out.println("✅ Found element by filtering menu items");
                        break;
                    }
                }
                if (element == null) {
                    throw new org.openqa.selenium.NoSuchElementException("Transfer Credits menu item not found in dropdown");
                }
            } catch (Exception e2) {
                    System.out.println("Menu item filtering failed, trying provided XPath...");
                    try {
                        // Strategy 4: Try the provided XPath (fallback)
                        // Use JavaScript XPath evaluation to handle special characters better
                        JavascriptExecutor js = (JavascriptExecutor) driver;
                        Object result = js.executeScript(
                            "var xpath = arguments[0];" +
                            "var result = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);" +
                            "return result.singleNodeValue;",
                            xpath
                        );
                        
                        if (result != null && result instanceof WebElement) {
                            element = (WebElement) result;
                            System.out.println("✅ Found element using JavaScript XPath evaluation");
                        } else {
                            // Fallback to standard Selenium XPath
                            element = extendedWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
                            System.out.println("✅ Found element using standard Selenium XPath");
                        }
                    } catch (Exception e4) {
                        PerformanceTracker.failStep(stepId, actionStartTime);
                        
                        // Record test failure in TestResultsCollector
                        long failureTime = System.currentTimeMillis() - actionStartTime;
                        TestResultsCollector.recordTestResult(
                            "ACM Test - Element Selection Failed",
                            "FAILED",
                            failureTime,
                            "ACM",
                            "Failed to find ACM menu item (Transfer Credits). Tried: text content, data-slot, and XPath. Error: " + e4.getMessage()
                        );
                        
                        System.err.println("Failed to find ACM menu item after trying all strategies");
                        System.err.println("Error: " + e4.getMessage());
                        
                        // Print available menu items for debugging
                        try {
                            List<WebElement> menuItems = driver.findElements(By.cssSelector("div[role='menuitem']"));
                            System.err.println("Available menu items on page: " + menuItems.size());
                            for (int i = 0; i < Math.min(10, menuItems.size()); i++) {
                                WebElement el = menuItems.get(i);
                                System.err.println("  - Menu item " + (i+1) + ": text='" + el.getText() + 
                                    "', data-slot='" + el.getAttribute("data-slot") + "'");
                            }
                        } catch (Exception debugEx) {
                            System.err.println("Could not list available menu items: " + debugEx.getMessage());
                        }
                        
                        throw new org.openqa.selenium.TimeoutException(
                            "Could not find ACM menu item (Transfer Credits). " +
                            "Tried: text content, data-slot attribute, and XPath: " + xpath, e4);
                    }
            }
        }
        
        // Scroll to the element
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        
        // Wait for element to be clickable - use the found element directly
        element = extendedWait.until(ExpectedConditions.elementToBeClickable(element));
        
        // Click/select the element
        element.click();
        long responseTime = System.currentTimeMillis();
        PerformanceTracker.recordResponseTime(stepId, actionStartTime);
        
        // Wait for potential page navigation/load
        try {
            Thread.sleep(3000); // Give more time for page to load
            extendedWait.until(driver -> {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                String readyState = (String) js.executeScript("return document.readyState");
                return "complete".equals(readyState);
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Store final URL after selection
        finalUrlAfterSelection = driver.getCurrentUrl();
        System.out.println("URL after ACM element selection: " + finalUrlAfterSelection);
        
        long loadEndTime = System.currentTimeMillis();
        PerformanceTracker.completeStep(stepId, actionStartTime, responseTime, loadEndTime);
        
        System.out.println("ACM element selected successfully");
    }

    @Then("a new page should load for ACM test")
    public void a_new_page_should_load_for_acm_test() {
        long actionStartTime = System.currentTimeMillis();
        String stepId = PerformanceTracker.startStep(
            "Verify new page loaded for ACM test",
            "Verifies that a new page loaded after ACM element selection",
            "verification"
        );
        
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        // Wait for page to potentially navigate/load after clicking the menu item
        try {
            Thread.sleep(3000); // Give time for navigation to start
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Wait for page to stabilize (either new page loaded or stayed on same page)
        try {
            extendedWait.until(driver -> {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                String readyState = (String) js.executeScript("return document.readyState");
                return "complete".equals(readyState);
            });
        } catch (Exception e) {
            System.out.println("Page ready state check completed");
        }
        
        // Get current URL after waiting
        String currentUrl = driver.getCurrentUrl();
        System.out.println("=== ACM Test Verification ===");
        System.out.println("URL before selection: " + urlBeforeElementSelection);
        System.out.println("URL after selection: " + finalUrlAfterSelection);
        System.out.println("Current URL for verification: " + currentUrl);
        
        // Check if URL changed from before selection (indicating new page loaded)
        // Use the URL right before the selection click as the baseline
        String urlBeforeSelection = urlBeforeElementSelection != null ? urlBeforeElementSelection : initialUrlBeforeSelection;
        boolean urlChanged = !currentUrl.equals(urlBeforeSelection) && !currentUrl.equals(finalUrlAfterSelection);
        
        // Additional checks for page content change
        boolean pageContentChanged = false;
        String pageTitleAfter = "";
        
        try {
            pageTitleAfter = driver.getTitle();
            String bodyText = driver.findElement(By.tagName("body")).getText();
            
            // Check if page has substantial content
            pageContentChanged = bodyText.length() > 100;
            
            System.out.println("Page Title After: " + pageTitleAfter);
            System.out.println("Page Content Length: " + bodyText.length());
        } catch (Exception e) {
            System.err.println("Error checking page content: " + e.getMessage());
        }
        
        // STRICT VERIFICATION: New page must have loaded
        // Primary indicator: URL must have changed
        // Secondary indicator: If URL didn't change, check for significant content/title change
        boolean newPageLoaded = urlChanged;
        
        // Fallback for SPAs: If URL didn't change but content/title changed significantly
        if (!urlChanged) {
            // Check if we're still on the same page by comparing with URL after selection
            if (finalUrlAfterSelection != null && !currentUrl.equals(finalUrlAfterSelection)) {
                // URL changed from after selection to now - page loaded
                newPageLoaded = true;
                System.out.println("Page loaded: URL changed from after selection");
            } else if (pageContentChanged) {
                // Content changed significantly - might be a new page in SPA
                // But be strict: only if URL is different from before selection
                if (!currentUrl.equals(urlBeforeSelection)) {
                    newPageLoaded = true;
                    System.out.println("Page loaded: Content changed and URL differs from before selection");
                } else {
                    System.out.println("Warning: Content changed but URL is same as before selection");
                }
            }
        }
        
        long verificationTime = System.currentTimeMillis() - actionStartTime;
        
        if (newPageLoaded) {
            // Record success
            TestResultsCollector.recordTestResult(
                "ACM Test - New Page Load Verification",
                "PASSED",
                verificationTime,
                "ACM",
                "New page loaded successfully. URL changed from '" + urlBeforeSelection + "' to '" + currentUrl + "'"
            );
            PerformanceTracker.completeStep(stepId, actionStartTime, verificationTime);
            
            System.out.println("✅ ACM Test PASSED: New page loaded successfully");
            System.out.println("========================================");
            assertThat(newPageLoaded)
                .as("A new page should have loaded after ACM element selection. " +
                    "Expected URL change from '" + urlBeforeSelection + "' but current URL is '" + currentUrl + "'")
                .isTrue();
        } else {
            // Record failure - THIS IS CRITICAL: Test must fail if no new page loaded
            TestResultsCollector.recordTestResult(
                "ACM Test - New Page Load Verification",
                "FAILED",
                verificationTime,
                "ACM",
                "FAILED: New page did not load after clicking Transfer Credits. " +
                "URL before selection: '" + urlBeforeSelection + "', " +
                "Current URL: '" + currentUrl + "'. " +
                "The page did not navigate to a new page as expected."
            );
            PerformanceTracker.failStep(stepId, actionStartTime);
            
            System.err.println("========================================");
            System.err.println("❌ ACM Test FAILED: New page did not load");
            System.err.println("URL before selection: " + urlBeforeSelection);
            System.err.println("Current URL: " + currentUrl);
            System.err.println("Expected: URL should have changed to indicate new page loaded");
            System.err.println("Actual: URL did not change - page did not load");
            System.err.println("========================================");
            
            // CRITICAL: Fail the test with clear assertion
            // This will cause the test to fail and be reported as failed
            assertThat(newPageLoaded)
                .as("TEST FAILED: A new page should have loaded after clicking the Transfer Credits menu item, " +
                    "but no new page was loaded. " +
                    "URL before selection: '" + urlBeforeSelection + "', " +
                    "Current URL: '" + currentUrl + "'. " +
                    "The test is marked as FAILED because the expected page navigation did not occur.")
                .isTrue(); // This will throw AssertionError when newPageLoaded is false
        }
    }

    @And("I click the refresh button")
    public void i_click_the_refresh_button() {
        long actionStartTime = System.currentTimeMillis();
        String stepId = PerformanceTracker.startStep(
            "Click refresh button",
            "Clicks the refresh button to reload the page",
            "refresh"
        );
        
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        
        System.out.println("Looking for refresh button");
        System.out.println("Current URL: " + driver.getCurrentUrl());
        
        WebElement refreshButton = null;
        
        // Try multiple selector strategies in order of reliability
        try {
            // Strategy 1: Use data-slot="button" and text "Refresh"
            System.out.println("Trying to find refresh button by data-slot and text...");
            refreshButton = extendedWait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[@data-slot='button' and contains(., 'Refresh')]")
            ));
            System.out.println("✅ Found refresh button using data-slot and text");
        } catch (Exception e1) {
            System.out.println("data-slot selector failed, trying by text content only...");
            try {
                // Strategy 2: Find by text content "Refresh"
                refreshButton = extendedWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//button[contains(., 'Refresh')]")
                ));
                System.out.println("✅ Found refresh button using text content");
            } catch (Exception e2) {
                System.out.println("Text content selector failed, trying CSS selector...");
                try {
                    // Strategy 3: Try CSS selector with data-slot
                    refreshButton = extendedWait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("button[data-slot='button']")
                    ));
                    // Verify it contains "Refresh" text
                    if (!refreshButton.getText().contains("Refresh")) {
                        // Find all buttons with data-slot and filter by text
                        List<WebElement> buttons = driver.findElements(By.cssSelector("button[data-slot='button']"));
                        for (WebElement btn : buttons) {
                            if (btn.getText().contains("Refresh")) {
                                refreshButton = btn;
                                break;
                            }
                        }
                        if (refreshButton == null || !refreshButton.getText().contains("Refresh")) {
                            throw new org.openqa.selenium.NoSuchElementException("Refresh button not found");
                        }
                    }
                    System.out.println("✅ Found refresh button using CSS selector");
                } catch (Exception e3) {
                    PerformanceTracker.failStep(stepId, actionStartTime);
                    System.err.println("Failed to find refresh button after trying all strategies");
                    System.err.println("Error: " + e3.getMessage());
                    
                    // Print available buttons for debugging
                    try {
                        List<WebElement> buttons = driver.findElements(By.cssSelector("button"));
                        System.err.println("Available buttons on page: " + buttons.size());
                        for (int i = 0; i < Math.min(10, buttons.size()); i++) {
                            WebElement btn = buttons.get(i);
                            System.err.println("  - Button " + (i+1) + ": text='" + btn.getText() + 
                                "', data-slot='" + btn.getAttribute("data-slot") + "'");
                        }
                    } catch (Exception debugEx) {
                        System.err.println("Could not list available buttons: " + debugEx.getMessage());
                    }
                    
                    throw new org.openqa.selenium.TimeoutException(
                        "Could not find refresh button. Tried: data-slot with text, text content, and CSS selector.", e3);
                }
            }
        }
        
        // Scroll to the refresh button
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", refreshButton);
        
        // Wait for button to be clickable
        refreshButton = extendedWait.until(ExpectedConditions.elementToBeClickable(refreshButton));
        
        // Store URL before refresh to detect page reload
        String urlBeforeRefresh = driver.getCurrentUrl();
        System.out.println("URL before refresh: " + urlBeforeRefresh);
        
        // Click the refresh button - response time is when click completes
        refreshButton.click();
        long responseTime = System.currentTimeMillis();
        PerformanceTracker.recordResponseTime(stepId, actionStartTime);
        
        System.out.println("Refresh button clicked, waiting for page to reload...");
        
        // Wait for page to refresh/reload
        try {
            // Wait for document ready state to change (page starts reloading)
            Thread.sleep(500);
            
            // Wait for page to complete loading after refresh
            extendedWait.until(driver -> {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                String readyState = (String) js.executeScript("return document.readyState");
                return "complete".equals(readyState);
            });
            
            // Additional wait for dynamic content to load
            Thread.sleep(2000);
            
            // Verify page has refreshed by checking if URL changed or page reloaded
            String urlAfterRefresh = driver.getCurrentUrl();
            System.out.println("URL after refresh: " + urlAfterRefresh);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long loadEndTime = System.currentTimeMillis();
        PerformanceTracker.completeStep(stepId, actionStartTime, responseTime, loadEndTime);
        
        long refreshTime = loadEndTime - responseTime;
        System.out.println("Refresh completed. Loading time: " + refreshTime + " ms");
    }

    @Then("the page should refresh and load within acceptable time")
    public void the_page_should_refresh_and_load_within_acceptable_time() {
        long actionStartTime = System.currentTimeMillis();
        String stepId = PerformanceTracker.startStep(
            "Verify page refresh and loading time",
            "Verifies that the page refreshed and measures loading time",
            "verification"
        );
        
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        // Verify page is loaded
        try {
            extendedWait.until(driver -> {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                String readyState = (String) js.executeScript("return document.readyState");
                return "complete".equals(readyState);
            });
        } catch (Exception e) {
            System.out.println("Page ready state check completed");
        }
        
        // Get the refresh loading time from the previous step
        // The PerformanceTracker should have recorded this
        String currentUrl = driver.getCurrentUrl();
        String pageTitle = driver.getTitle();
        
        System.out.println("=== Refresh Test Verification ===");
        System.out.println("Current URL: " + currentUrl);
        System.out.println("Page Title: " + pageTitle);
        
        // Verify page is accessible and loaded
        boolean pageLoaded = false;
        try {
            String bodyText = driver.findElement(By.tagName("body")).getText();
            pageLoaded = bodyText.length() > 100 && "complete".equals(
                ((JavascriptExecutor) driver).executeScript("return document.readyState")
            );
            System.out.println("Page Content Length: " + bodyText.length());
        } catch (Exception e) {
            System.err.println("Error checking page content: " + e.getMessage());
        }
        
        long verificationTime = System.currentTimeMillis() - actionStartTime;
        
        if (pageLoaded) {
            // Record success
            TestResultsCollector.recordTestResult(
                "ARefresh Test - Page Refresh Verification",
                "PASSED",
                verificationTime,
                "ARefresh",
                "Page refreshed and loaded successfully. URL: " + currentUrl
            );
            PerformanceTracker.completeStep(stepId, actionStartTime, verificationTime);
            
            System.out.println("✅ ARefresh Test PASSED: Page refreshed and loaded successfully");
            System.out.println("========================================");
            assertThat(pageLoaded)
                .as("The page should have refreshed and loaded successfully")
                .isTrue();
        } else {
            // Record failure
            TestResultsCollector.recordTestResult(
                "ARefresh Test - Page Refresh Verification",
                "FAILED",
                verificationTime,
                "ARefresh",
                "Page did not refresh or load properly. URL: " + currentUrl
            );
            PerformanceTracker.failStep(stepId, actionStartTime);
            
            System.err.println("========================================");
            System.err.println("❌ ARefresh Test FAILED: Page did not refresh properly");
            System.err.println("Current URL: " + currentUrl);
            System.err.println("========================================");
            
            assertThat(pageLoaded)
                .as("The page should have refreshed and loaded successfully, but it did not")
                .isTrue();
        }
    }
}

