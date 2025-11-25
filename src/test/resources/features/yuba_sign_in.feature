@signin
Feature: Yuba Sign In
  Scenario: Launch Yuba site and click Sign In
    Given I open the Yuba homepage
    When I click the Sign In button
    Then I should see the sign in experience

