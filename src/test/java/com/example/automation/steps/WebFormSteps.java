package com.example.automation.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.automation.support.DriverFactory;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebFormSteps {

    private static final String WEB_FORM_URL = "https://www.selenium.dev/selenium/web/web-form.html";
    private final WebDriver driver = DriverFactory.getDriver();
    private final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

    @Given("I am on the Selenium web form page")
    public void i_am_on_the_selenium_web_form_page() {
        driver.navigate().to(WEB_FORM_URL);
        wait.until(ExpectedConditions.titleIs("Web form"));
    }

    @When("I provide valid form details")
    public void i_provide_valid_form_details() {
        driver.findElement(By.id("my-text-id")).sendKeys("Cursor Bot");
        driver.findElement(By.name("my-text")).sendKeys("Exploring automated tests");
        new Select(driver.findElement(By.name("my-select"))).selectByVisibleText("Option 2");
        driver.findElement(By.cssSelector("input[type='checkbox']")).click();
        driver.findElement(By.cssSelector("input[type='radio'][value='option2']")).click();
        driver.findElement(By.name("my-date")).sendKeys("04142025");
    }

    @And("I submit the form")
    public void i_submit_the_form() {
        driver.findElement(By.cssSelector("button")).click();
    }

    @Then("I should see a confirmation message")
    public void i_should_see_a_confirmation_message() {
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("message")));
        assertThat(message.getText()).isEqualTo("Received!");
    }
}

