package com.example.automation.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.automation.support.DriverFactory;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class YubaSignInSteps {

    private static final String YUBA_URL = "https://yubanow.com/";
    private final WebDriver driver = DriverFactory.getDriver();
    private final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    private long dashboardLoadStartTime;
    private long dashboardLoadEndTime;

    @Given("I open the Yuba homepage")
    public void i_open_the_yuba_homepage() {
        driver.navigate().to(YUBA_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    @When("I click the Sign In button")
    public void i_click_the_sign_in_button() {
        WebElement signIn = locateSignInElement();
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", signIn);
        wait.until(ExpectedConditions.elementToBeClickable(signIn)).click();
    }

    @When("I enter my email {string}")
    public void i_enter_my_email(String email) {
        // Wait for the email field to be present and visible
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='email']")));
        
        // Scroll to the email field and clear any existing text
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", emailField);
        wait.until(ExpectedConditions.elementToBeClickable(emailField));
        
        emailField.clear();
        emailField.sendKeys(email);
    }

    @When("I enter my password {string}")
    public void i_enter_my_password(String password) {
        // Wait for the password field to be present and visible
        WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='password']")));
        
        // Scroll to the password field and clear any existing text
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", passwordField);
        wait.until(ExpectedConditions.elementToBeClickable(passwordField));
        
        passwordField.clear();
        passwordField.sendKeys(password);
    }

    @When("I click the sign in submit button")
    public void i_click_the_sign_in_submit_button() {
        // Wait for the sign in submit button to be clickable
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("/html/body/div[1]/div/div/div[1]/div/div/div[2]/form/div/div[4]/button")));
        
        // Scroll to the button and click it
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", submitButton);
        submitButton.click();
    }

    @Then("I should be signed in successfully")
    public void i_should_be_signed_in_successfully() {
        // Wait for navigation after sign in (could be dashboard, profile, or success page)
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        
        extendedWait.until(driver -> {
            String currentUrl = driver.getCurrentUrl();
            String bodyText = driver.findElement(By.tagName("body")).getText().toLowerCase();
            
            // Check for successful sign in indicators
            return currentUrl.contains("dashboard") || 
                   currentUrl.contains("profile") || 
                   currentUrl.contains("home") ||
                   bodyText.contains("welcome") ||
                   bodyText.contains("dashboard") ||
                   bodyText.contains("signed in") ||
                   !currentUrl.contains("sign") && !currentUrl.contains("login"); // Not on sign in page anymore
        });
        
        // Verify sign in was successful
        String currentUrl = driver.getCurrentUrl();
        String bodyText = driver.findElement(By.tagName("body")).getText().toLowerCase();
        
        boolean isSignedIn = currentUrl.contains("dashboard") || 
                            currentUrl.contains("profile") || 
                            currentUrl.contains("home") ||
                            bodyText.contains("welcome") ||
                            bodyText.contains("dashboard") ||
                            bodyText.contains("signed in") ||
                            (!currentUrl.contains("sign") && !currentUrl.contains("login"));
        
        assertThat(isSignedIn)
            .as("Should be signed in successfully. Current URL: " + currentUrl)
            .isTrue();
        
        // Stay on the page to see the result
        try {
            Thread.sleep(10000); // Stay on page for 10 seconds so user can see the result
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("I should see the sign in experience")
    public void i_should_see_the_sign_in_experience() {
        // Wait for navigation to sign in page or sign in content to appear
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        extendedWait.until(driver -> {
            String currentUrl = driver.getCurrentUrl();
            return (!currentUrl.equalsIgnoreCase(YUBA_URL) && 
                   (currentUrl.contains("sign") || currentUrl.contains("login") || currentUrl.contains("auth"))) 
                   || pageHasSignInText();
        });
        
        // Verify we're on the sign-in page
        String currentUrl = driver.getCurrentUrl();
        boolean isOnSignInPage = pageHasSignInText() || 
                                currentUrl.contains("sign") || 
                                currentUrl.contains("login") ||
                                currentUrl.contains("auth");
        
        assertThat(isOnSignInPage)
            .as("Should be on sign in page. Current URL: " + currentUrl)
            .isTrue();
        
        // Wait on the sign-in page to keep browser open and visible
        // This allows the user to see the sign-in page before the browser closes
        try {
            Thread.sleep(10000); // Stay on page for 10 seconds so user can see it
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean pageHasSignInText() {
        String bodyText = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return bodyText.contains("sign in") || bodyText.contains("log in") || bodyText.contains("login");
    }

    private WebElement locateSignInElement() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, document.body.scrollHeight / 2);");

        List<By> locatorCandidates = List.of(
            By.linkText("Sign In"),
            By.linkText("Sign in"),
            By.partialLinkText("Sign In"),
            By.partialLinkText("Sign in"),
            By.xpath("//a[contains(translate(@href, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'signin')]"),
            By.xpath("//a[contains(translate(@href, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'sign-in')]"),
            By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'sign in')]")
        );

        for (By locator : locatorCandidates) {
            List<WebElement> matches = driver.findElements(locator);
            if (!matches.isEmpty()) {
                return matches.get(0);
            }
        }

        throw new NoSuchElementException("Unable to find the Sign In button on the page.");
    }

    @When("I click the dashboard navigation button")
    public void i_click_the_dashboard_navigation_button() {
        // Record the start time before clicking the button
        dashboardLoadStartTime = System.currentTimeMillis();
        
        // Wait for the specific button to be clickable
        WebElement dashboardButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("/html/body/div[1]/div/div/div[3]/button[1]")));
        
        // Scroll to the button and click it
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", dashboardButton);
        dashboardButton.click();
        
        System.out.println("Dashboard navigation button clicked at: " + dashboardLoadStartTime + "ms");
    }

    @Then("I should reach the dashboard workspace page")
    public void i_should_reach_the_dashboard_workspace_page() {
        // Wait for the dashboard page to load completely
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        
        extendedWait.until(driver -> {
            String currentUrl = driver.getCurrentUrl();
            String bodyText = driver.findElement(By.tagName("body")).getText().toLowerCase();
            
            // Check for dashboard indicators
            boolean isDashboard = currentUrl.contains("dashboard") || 
                                 currentUrl.contains("workspace") ||
                                 bodyText.contains("dashboard") ||
                                 bodyText.contains("workspace") ||
                                 bodyText.contains("welcome to") ||
                                 // Check if page is fully loaded by looking for common dashboard elements
                                 !driver.findElements(By.xpath("//*[contains(@class, 'loading') or contains(@class, 'spinner')]")).isEmpty() == false;
            
            if (isDashboard) {
                // Record end time when dashboard is detected
                dashboardLoadEndTime = System.currentTimeMillis();
            }
            
            return isDashboard;
        });
        
        // Verify we're on the dashboard page
        String currentUrl = driver.getCurrentUrl();
        String bodyText = driver.findElement(By.tagName("body")).getText().toLowerCase();
        
        boolean isDashboard = currentUrl.contains("dashboard") || 
                             currentUrl.contains("workspace") ||
                             bodyText.contains("dashboard") ||
                             bodyText.contains("workspace") ||
                             bodyText.contains("welcome to");
        
        assertThat(isDashboard)
            .as("Should be on dashboard/workspace page. Current URL: " + currentUrl)
            .isTrue();
        
        System.out.println("Dashboard page fully loaded at: " + dashboardLoadEndTime + "ms");
    }

    @Then("the dashboard loading time should be tracked")
    public void the_dashboard_loading_time_should_be_tracked() {
        // Calculate the loading time
        long loadingTime = dashboardLoadEndTime - dashboardLoadStartTime;
        double loadingTimeSeconds = loadingTime / 1000.0;
        
        System.out.println("=== DASHBOARD LOADING TIME TRACKING ===");
        System.out.println("Button clicked at: " + dashboardLoadStartTime + "ms");
        System.out.println("Dashboard loaded at: " + dashboardLoadEndTime + "ms");
        System.out.println("Total loading time: " + loadingTime + "ms (" + loadingTimeSeconds + " seconds)");
        System.out.println("=======================================");
        
        // Verify that loading time is reasonable (less than 30 seconds)
        assertThat(loadingTimeSeconds)
            .as("Dashboard loading time should be reasonable (less than 30 seconds)")
            .isLessThan(30.0);
        
        // Also verify that we actually measured some time (not instantaneous)
        assertThat(loadingTime)
            .as("Loading time should be greater than 0ms")
            .isGreaterThan(0);
        
        // Stay on the dashboard page to see the result
        try {
            Thread.sleep(15000); // Stay on dashboard for 15 seconds so user can see it
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

