@demo
Feature: Web form submission
  As a curious tester
  I want to submit the Selenium demo form
  So that I can verify our automation wiring works end-to-end

  @demo
  Scenario: Submit a basic form successfully
    Given I am on the Selenium web form page
    When I provide valid form details
    And I submit the form
    Then I should see a confirmation message

