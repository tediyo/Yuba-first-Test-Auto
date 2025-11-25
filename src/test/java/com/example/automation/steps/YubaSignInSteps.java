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
}

