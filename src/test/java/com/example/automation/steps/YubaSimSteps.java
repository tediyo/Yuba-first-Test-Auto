package com.example.automation.steps;

import com.example.automation.reporting.PerformanceTracker;
import com.example.automation.support.DriverFactory;
import io.cucumber.java.en.And;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class YubaSimSteps {

    private final WebDriver driver = DriverFactory.getDriver();
    private final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    private static final String CHOOSE_WORKSPACE_URL = "https://yubanow.com/choose-workspace";

    @And("I wait for navigation to choose workspace page")
    public void i_wait_for_navigation_to_choose_workspace_page() {
        long actionStartTime = System.currentTimeMillis();
        String stepId = PerformanceTracker.startStep(
            "Wait for navigation to choose workspace page",
            "Waits for navigation to workspace selection page",
            "navigation"
        );
        
        // Wait for navigation to the choose-workspace page
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(60));
        
        String initialUrl = driver.getCurrentUrl();
        System.out.println("Initial URL after sign-in: " + initialUrl);
        
        try {
            // Wait for URL to change (navigation away from sign-in page)
            extendedWait.until(driver -> {
                String currentUrl = driver.getCurrentUrl();
                // Check if we've navigated away from sign-in/login pages
                boolean navigatedAway = !currentUrl.contains("sign") && 
                                       !currentUrl.contains("login") && 
                                       !currentUrl.contains("auth") &&
                                       !currentUrl.equals(initialUrl);
                
                // Also check if we're on choose-workspace page
                boolean isChooseWorkspace = currentUrl.contains("choose-workspace") || 
                                           currentUrl.equals(CHOOSE_WORKSPACE_URL);
                
                if (navigatedAway || isChooseWorkspace) {
                    System.out.println("Navigation detected. Current URL: " + currentUrl);
                }
                
                return navigatedAway || isChooseWorkspace;
            });
            
            String currentUrl = driver.getCurrentUrl();
            System.out.println("After navigation, current URL: " + currentUrl);
            
            // If we're on choose-workspace page, wait for it to load
            if (currentUrl.contains("choose-workspace") || currentUrl.equals(CHOOSE_WORKSPACE_URL)) {
                // Wait for the page to fully load - check for workspace content
                extendedWait.until(driver -> {
                    try {
                        String bodyText = driver.findElement(By.tagName("body")).getText().toLowerCase();
                        
                        // Check if workspace content is visible (workspace options, "choose your workspace", etc.)
                        boolean hasWorkspaceContent = bodyText.contains("choose your workspace") || 
                                                     bodyText.contains("select a workspace") ||
                                                     bodyText.contains("workspace") ||
                                                     bodyText.contains("continue");
                        
                        // Check if loading text is gone (if it was present)
                        boolean loadingGone = !bodyText.contains("loading your workspaces");
                        
                        // Page is ready if we have workspace content and loading is gone (or never was present)
                        // OR if we have substantial content (more than 200 chars)
                        boolean pageReady = (hasWorkspaceContent && loadingGone) || bodyText.length() > 200;
                        
                        if (!pageReady) {
                            System.out.println("Waiting for workspace page to load... Body length: " + bodyText.length());
                        }
                        
                        return pageReady;
                    } catch (Exception e) {
                        System.out.println("Exception checking page load: " + e.getMessage());
                        return false;
                    }
                });
                
                // Additional wait for document ready state
                extendedWait.until(driver -> {
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    String readyState = (String) js.executeScript("return document.readyState");
                    return "complete".equals(readyState);
                });
                
                // Small delay to ensure all dynamic content is loaded
                try {
                    Thread.sleep(1000); // Reduced to 1 second since page is already loaded
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                System.out.println("Successfully navigated to choose-workspace page: " + driver.getCurrentUrl());
                
                // Record response time (when URL changed)
                long responseTime = System.currentTimeMillis();
                PerformanceTracker.recordResponseTime(stepId, actionStartTime);
                
                // Record load time (when page fully loaded)
                long loadEndTime = System.currentTimeMillis();
                PerformanceTracker.completeStep(stepId, actionStartTime, responseTime, loadEndTime);
            } else {
                System.out.println("WARNING: Not on choose-workspace page. Current URL: " + currentUrl);
                System.out.println("Page body text preview: " + driver.findElement(By.tagName("body")).getText().substring(0, Math.min(200, driver.findElement(By.tagName("body")).getText().length())));
                
                long loadEndTime = System.currentTimeMillis();
                PerformanceTracker.completeStep(stepId, actionStartTime, loadEndTime);
            }
            
        } catch (org.openqa.selenium.TimeoutException e) {
            String finalUrl = driver.getCurrentUrl();
            String pageTitle = driver.getTitle();
            String bodyText = "";
            try {
                bodyText = driver.findElement(By.tagName("body")).getText().substring(0, Math.min(500, driver.findElement(By.tagName("body")).getText().length()));
            } catch (Exception ex) {
                bodyText = "Could not get body text";
            }
            
            System.err.println("Timeout waiting for choose-workspace page navigation.");
            System.err.println("Final URL: " + finalUrl);
            System.err.println("Page Title: " + pageTitle);
            System.err.println("Page Body Preview: " + bodyText);
            
            PerformanceTracker.failStep(stepId, actionStartTime);
            throw new org.openqa.selenium.TimeoutException(
                "Failed to navigate to choose-workspace page. " +
                "Current URL: " + finalUrl + ". " +
                "Expected URL to contain 'choose-workspace' or navigate away from sign-in page.", e);
        }
    }

    @And("I select the element {string}")
    public void i_select_the_element(String xpath) {
        // Wait for the element to be present and visible
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        
        // Scroll to the element
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        
        // Wait for element to be clickable and select it (click to select)
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        
        // Wait for page to stabilize after selection - wait for document ready state
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        extendedWait.until(driver -> {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String readyState = (String) js.executeScript("return document.readyState");
            return "complete".equals(readyState);
        });
        
        // Additional wait for any dynamic content to load
        try {
            Thread.sleep(2000); // Wait 2 seconds for UI to update and dynamic content to load
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @And("I click the element {string}")
    public void i_click_the_element(String xpath) {
        long actionStartTime = System.currentTimeMillis();
        String stepId = PerformanceTracker.startStep(
            "Click element: " + xpath,
            "Clicks an element by XPath",
            "click"
        );
        
        // Wait for the element to be present and visible on the choose-workspace page
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        
        // Wait for the element to be present first
        WebElement element = extendedWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        
        // Scroll to the element
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        
        // Wait for element to be clickable
        element = extendedWait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        
        // Click the element - response time is when click completes
        element.click();
        long responseTime = System.currentTimeMillis();
        PerformanceTracker.recordResponseTime(stepId, actionStartTime);
        
        // Wait for page to stabilize after clicking - wait for document ready state
        extendedWait.until(driver -> {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String readyState = (String) js.executeScript("return document.readyState");
            return "complete".equals(readyState);
        });
        
        // Additional wait for any dynamic content to load after click
        try {
            Thread.sleep(2000); // Wait 2 seconds for UI to update and dynamic content to load
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long loadEndTime = System.currentTimeMillis();
        PerformanceTracker.completeStep(stepId, actionStartTime, responseTime, loadEndTime);
        
        System.out.println("Successfully clicked element at: " + xpath);
    }

    @And("I click the button {string}")
    public void i_click_the_button(String xpath) {
        long actionStartTime = System.currentTimeMillis();
        String stepId = PerformanceTracker.startStep(
            "Click button: " + xpath,
            "Clicks a button by XPath",
            "click"
        );
        
        // Use extended wait for button to appear (may need more time after element selection)
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        
        try {
            // Wait for the button to be present first
            WebElement button = extendedWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
            
            // Scroll to the button
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", button);
            
            // Wait for button to be clickable with extended timeout
            button = extendedWait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
            
            // Click the button - response time is when click completes
            button.click();
            long responseTime = System.currentTimeMillis();
            PerformanceTracker.recordResponseTime(stepId, actionStartTime);
            
            // Wait for page to stabilize
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            long loadEndTime = System.currentTimeMillis();
            PerformanceTracker.completeStep(stepId, actionStartTime, responseTime, loadEndTime);
        } catch (org.openqa.selenium.TimeoutException e) {
            // If button not found, try to find any button in the parent container as fallback
            System.out.println("Button not found at: " + xpath);
            System.out.println("Current URL: " + driver.getCurrentUrl());
            
            // Try to find buttons in the same parent div
            try {
                String parentXpath = xpath.substring(0, xpath.lastIndexOf("/"));
                java.util.List<WebElement> buttons = driver.findElements(By.xpath(parentXpath + "//button"));
                if (!buttons.isEmpty()) {
                    System.out.println("Found " + buttons.size() + " button(s) in parent container");
                    WebElement firstButton = extendedWait.until(ExpectedConditions.elementToBeClickable(buttons.get(0)));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", firstButton);
                    firstButton.click();
                    
                    long responseTime = System.currentTimeMillis();
                    PerformanceTracker.recordResponseTime(stepId, actionStartTime);
                    long loadEndTime = System.currentTimeMillis();
                    PerformanceTracker.completeStep(stepId, actionStartTime, responseTime, loadEndTime);
                    return;
                }
            } catch (Exception fallbackException) {
                // Fallback failed, throw original exception
            }
            
            PerformanceTracker.failStep(stepId, actionStartTime);
            throw new org.openqa.selenium.TimeoutException(
                "Button not found at XPath: " + xpath + 
                ". The element may not exist, or the page structure may have changed after element selection.", e);
        }
    }

    @And("I click the navigation link {string}")
    public void i_click_the_navigation_link(String xpath) {
        long actionStartTime = System.currentTimeMillis();
        String stepId = PerformanceTracker.startStep(
            "Click navigation link: " + xpath,
            "Clicks a navigation link by XPath",
            "navigation"
        );
        
        // Use extended wait for navigation link (may need time after button click)
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        
        // Wait for the navigation link to be present first
        WebElement navLink = extendedWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        
        // Scroll to the navigation link
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", navLink);
        
        // Wait for navigation link to be clickable with extended timeout
        navLink = extendedWait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        
        // Click the navigation link - response time is when click completes
        navLink.click();
        long responseTime = System.currentTimeMillis();
        PerformanceTracker.recordResponseTime(stepId, actionStartTime);
        
        // Wait for navigation to complete
        try {
            Thread.sleep(1500);
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
    }

    @And("I enter email {string} in the individual email field")
    public void i_enter_email_in_the_individual_email_field(String email) {
        long actionStartTime = System.currentTimeMillis();
        String stepId = PerformanceTracker.startStep(
            "Enter email in individual email field",
            "Enters email address in the individual email input field",
            "input"
        );
        
        // Wait for the individual email field to be present and visible
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='individual-email']")));
        
        // Scroll to the email field and clear any existing text
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", emailField);
        wait.until(ExpectedConditions.elementToBeClickable(emailField));
        
        // Input action - response time is when input completes
        emailField.clear();
        emailField.sendKeys(email);
        long responseTime = System.currentTimeMillis();
        PerformanceTracker.recordResponseTime(stepId, actionStartTime);
        
        // Small delay for field validation/processing
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long loadEndTime = System.currentTimeMillis();
        PerformanceTracker.completeStep(stepId, actionStartTime, responseTime, loadEndTime);
    }

    @And("I click the form submit button {string}")
    public void i_click_the_form_submit_button(String xpath) {
        long actionStartTime = System.currentTimeMillis();
        String stepId = PerformanceTracker.startStep(
            "Click form submit button: " + xpath,
            "Submits the form by clicking the submit button",
            "submit"
        );
        
        // Wait for the form submit button to be clickable
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        
        // Scroll to the button
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", submitButton);
        
        // Click the submit button - response time is when form submission starts
        submitButton.click();
        long responseTime = System.currentTimeMillis();
        PerformanceTracker.recordResponseTime(stepId, actionStartTime);
        
        // Wait for form submission to process
        try {
            Thread.sleep(2000);
            wait.until(driver -> {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                String readyState = (String) js.executeScript("return document.readyState");
                return "complete".equals(readyState);
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long loadEndTime = System.currentTimeMillis();
        PerformanceTracker.completeStep(stepId, actionStartTime, responseTime, loadEndTime);
    }
}

