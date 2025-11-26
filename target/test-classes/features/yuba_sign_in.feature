@signin
Feature: Yuba Sign In
  As a user
  I want to sign in to Yuba
  So that I can access the platform

  @signin
  Scenario: Complete sign in process with dashboard navigation
    Given I open the Yuba homepage
    When I click the Sign In button
    And I enter my email "thedronberhanu05@gmail.com"
    And I enter my password "Ttbbss&$12"
    And I click the sign in submit button
    Then I should be signed in successfully
    When I click the dashboard navigation button
    Then I should reach the dashboard workspace page
    And the dashboard loading time should be tracked

