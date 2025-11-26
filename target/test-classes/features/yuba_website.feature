@navigation
Feature: Yuba Website Navigation and Functionality
  As a user
  I want to navigate and interact with the Yuba website
  So that I can verify the website functionality works correctly

  Background:
    Given I am on the Yuba homepage

  @navigation
  Scenario: Verify homepage loads with key elements
    Then I should see the page title contains "Yuba"
    And I should see the main heading "A Sounding Board forEarly Stage African Entrepreneurs"
    And I should see the "Get Started" button
    And I should see the "See how it works" link

  Scenario: Navigate to different sections
    When I click on "Our Process" in the navigation
    Then I should see the "From Problem Discovery to Market Validation" section
    When I click on "What We Offer" in the navigation
    Then I should see content about Yuba's offerings
    When I click on "Testimonials" in the navigation
    Then I should see testimonials from founders
    When I click on "FAQs" in the navigation
    Then I should see the Frequently Asked Questions section

  Scenario: Verify Problem Discovery module is accessible
    When I scroll to the "Problem Discovery" module
    Then I should see "MODULE 1" heading
    And I should see "Problem Discovery" section
    And I should see options for "Problem Explorer", "Idea Refiner", and "Problem Validator"

  Scenario: Verify trusted partners section displays
    Then I should see the "Trusted by early stage founders" section
    And I should see partner logos displayed

  Scenario: Verify industry agnostic section
    When I scroll to the industry section
    Then I should see multiple industry categories displayed
    And I should see industries like "Agriculture", "Finance", "Retail", "Education", "Healthcare", "Technology", "Manufacturing"

  Scenario: Verify Venture Builders section
    When I scroll to the Venture Builders section
    Then I should see "Context Aware Expert Guidance & Support" heading
    And I should see multiple Venture Builder profiles
    And I should see "Book a session" option

  Scenario: Verify FAQs section functionality
    When I navigate to the FAQs section
    Then I should see the question "What is Yuba?"
    And I should see the question "How Does Yuba Work?"
    And I should see the question "Who is Yuba For?"

  Scenario: Verify footer elements
    When I scroll to the footer
    Then I should see footer links for "Product", "Company"
    And I should see contact email "office@yubanow.com"
    And I should see copyright information
