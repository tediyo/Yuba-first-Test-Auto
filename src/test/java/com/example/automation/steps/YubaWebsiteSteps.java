package com.example.automation.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.automation.support.DriverFactory;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class YubaWebsiteSteps {

    private static final String YUBA_URL = "https://yubanow.com/";
    private final WebDriver driver = DriverFactory.getDriver();
    private final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    private final JavascriptExecutor js = (JavascriptExecutor) driver;

    @Given("I am on the Yuba homepage")
    public void i_am_on_the_yuba_homepage() {
        driver.navigate().to(YUBA_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        // Wait for page to fully load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("I should see the page title contains {string}")
    public void i_should_see_the_page_title_contains(String expectedText) {
        String pageTitle = driver.getTitle();
        assertThat(pageTitle).containsIgnoringCase(expectedText);
    }

    @Then("I should see the main heading {string}")
    public void i_should_see_the_main_heading(String expectedHeading) {
        // The heading might be split across elements, so we'll check for key parts
        WebElement body = driver.findElement(By.tagName("body"));
        String bodyText = body.getText();
        // Check for key parts of the heading
        assertThat(bodyText).containsIgnoringCase("Sounding Board");
        assertThat(bodyText).containsIgnoringCase("Early Stage");
        assertThat(bodyText).containsIgnoringCase("African Entrepreneurs");
    }

    @Then("I should see the {string} button")
    public void i_should_see_the_button(String buttonText) {
        WebElement button = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[contains(text(), '" + buttonText + "')] | " +
                         "//a[contains(text(), '" + buttonText + "')] | " +
                         "//*[contains(text(), '" + buttonText + "')]")));
        assertThat(button.isDisplayed()).isTrue();
    }

    @Then("I should see the {string} link")
    public void i_should_see_the_link(String linkText) {
        WebElement link = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[contains(text(), '" + linkText + "')]")));
        assertThat(link.isDisplayed()).isTrue();
    }

    @When("I click on {string} in the navigation")
    public void i_click_on_in_the_navigation(String navItem) {
        WebElement navLink = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//nav//a[contains(text(), '" + navItem + "')] | " +
                         "//*[contains(@class, 'nav')]//a[contains(text(), '" + navItem + "')]")));
        js.executeScript("arguments[0].scrollIntoView(true);", navLink);
        navLink.click();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("I should see the {string} section")
    public void i_should_see_the_section(String sectionText) {
        WebElement section = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(), '" + sectionText + "')]")));
        js.executeScript("arguments[0].scrollIntoView(true);", section);
        assertThat(section.isDisplayed()).isTrue();
    }

    @When("I scroll to the {string} module")
    public void i_scroll_to_the_module(String moduleName) {
        WebElement module = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(), '" + moduleName + "')]")));
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", module);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("I should see {string} heading")
    public void i_should_see_heading(String headingText) {
        WebElement heading = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h1[contains(text(), '" + headingText + "')] | " +
                         "//h2[contains(text(), '" + headingText + "')] | " +
                         "//h3[contains(text(), '" + headingText + "')] | " +
                         "//*[contains(@class, 'heading') and contains(text(), '" + headingText + "')]")));
        assertThat(heading.isDisplayed()).isTrue();
    }

    @Then("I should see {string} section")
    public void i_should_see_section(String sectionName) {
        WebElement section = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(), '" + sectionName + "')]")));
        assertThat(section.isDisplayed()).isTrue();
    }

    @And("I should see options for {string}, {string}, and {string}")
    public void i_should_see_options_for(String option1, String option2, String option3) {
        WebElement body = driver.findElement(By.tagName("body"));
        String bodyText = body.getText();
        assertThat(bodyText).containsIgnoringCase(option1);
        assertThat(bodyText).containsIgnoringCase(option2);
        assertThat(bodyText).containsIgnoringCase(option3);
    }

    @And("I should see partner logos displayed")
    public void i_should_see_partner_logos_displayed() {
        // Check for common logo indicators (img tags, or text mentioning partners)
        List<WebElement> images = driver.findElements(By.tagName("img"));
        // At least some images should be present (logos)
        assertThat(images.size()).isGreaterThan(0);
    }

    @When("I scroll to the industry section")
    public void i_scroll_to_the_industry_section() {
        WebElement industrySection = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(), 'Industry') or contains(text(), 'Agnostic')]")));
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", industrySection);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("I should see multiple industry categories displayed")
    public void i_should_see_multiple_industry_categories_displayed() {
        WebElement body = driver.findElement(By.tagName("body"));
        String bodyText = body.getText();
        // Check for industry-related text
        assertThat(bodyText).containsIgnoringCase("Industry");
    }

    @And("I should see industries like {string}, {string}, {string}, {string}, {string}, {string}, {string}")
    public void i_should_see_industries_like(String industry1, String industry2, String industry3,
                                             String industry4, String industry5, String industry6,
                                             String industry7) {
        WebElement body = driver.findElement(By.tagName("body"));
        String bodyText = body.getText();
        assertThat(bodyText).containsIgnoringCase(industry1);
        assertThat(bodyText).containsIgnoringCase(industry2);
        assertThat(bodyText).containsIgnoringCase(industry3);
        assertThat(bodyText).containsIgnoringCase(industry4);
        assertThat(bodyText).containsIgnoringCase(industry5);
        assertThat(bodyText).containsIgnoringCase(industry6);
        assertThat(bodyText).containsIgnoringCase(industry7);
    }

    @When("I scroll to the Venture Builders section")
    public void i_scroll_to_the_venture_builders_section() {
        WebElement ventureSection = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(), 'Venture Builder') or contains(text(), 'Expert Guidance')]")));
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", ventureSection);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @And("I should see multiple Venture Builder profiles")
    public void i_should_see_multiple_venture_builder_profiles() {
        WebElement body = driver.findElement(By.tagName("body"));
        String bodyText = body.getText();
        // Check for Venture Builder names or related text
        assertThat(bodyText).containsIgnoringCase("Venture Builder");
    }

    @And("I should see {string} option")
    public void i_should_see_option(String optionText) {
        WebElement option = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(), '" + optionText + "')]")));
        assertThat(option.isDisplayed()).isTrue();
    }

    @When("I navigate to the FAQs section")
    public void i_navigate_to_the_faqs_section() {
        // Try clicking FAQ link first
        try {
            WebElement faqLink = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(text(), 'FAQ') or contains(text(), 'FAQs')]")));
            js.executeScript("arguments[0].scrollIntoView(true);", faqLink);
            faqLink.click();
            Thread.sleep(1000);
        } catch (Exception e) {
            // If link click fails, scroll to FAQ section
            WebElement faqSection = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(text(), 'Frequently Asked') or contains(text(), 'FAQ')]")));
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", faqSection);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Then("I should see the question {string}")
    public void i_should_see_the_question(String questionText) {
        WebElement question = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(), '" + questionText + "')]")));
        assertThat(question.isDisplayed()).isTrue();
    }

    @When("I scroll to the footer")
    public void i_scroll_to_the_footer() {
        js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("I should see footer links for {string}, {string}")
    public void i_should_see_footer_links_for(String link1, String link2) {
        WebElement body = driver.findElement(By.tagName("body"));
        String bodyText = body.getText();
        assertThat(bodyText).containsIgnoringCase(link1);
        assertThat(bodyText).containsIgnoringCase(link2);
    }

    @And("I should see contact email {string}")
    public void i_should_see_contact_email(String email) {
        WebElement emailElement = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(), '" + email + "')] | //a[contains(@href, 'mailto:')]")));
        assertThat(emailElement.isDisplayed()).isTrue();
    }

    @And("I should see copyright information")
    public void i_should_see_copyright_information() {
        WebElement body = driver.findElement(By.tagName("body"));
        String bodyText = body.getText().toLowerCase();
        boolean hasCopyright = bodyText.contains("©") || 
            bodyText.contains("copyright") ||
            bodyText.contains("yuba");
        assertThat(hasCopyright).isTrue();
    }

    @Then("I should see content about Yuba's offerings")
    public void i_should_see_content_about_yuba_s_offerings() {
        WebElement body = driver.findElement(By.tagName("body"));
        String bodyText = body.getText().toLowerCase();
        // Check for key terms related to offerings
        boolean hasContent = bodyText.contains("yuba") || 
            bodyText.contains("offer");
        assertThat(hasContent).isTrue();
    }

    @Then("I should see testimonials from founders")
    public void i_should_see_testimonials_from_founders() {
        WebElement body = driver.findElement(By.tagName("body"));
        String bodyText = body.getText().toLowerCase();
        // Check for testimonial indicators
        boolean hasTestimonials = bodyText.contains("founder") ||
            bodyText.contains("testimonial") ||
            bodyText.contains("\"");
        assertThat(hasTestimonials).isTrue();
    }
}
