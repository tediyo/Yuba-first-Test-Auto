@sim
Feature: Yuba SIM
  As a user
  I want to complete the sign in process with dashboard navigation
  So that I can access the dashboard workspace

  @sim
  Scenario: Complete sign in process with dashboard navigation
    Given I open the Yuba homepage
    When I click the Sign In button
    And I enter my email "tewodrosberhanu16@gmail.com"
    And I enter my password "Ttbbss12&$"
    And I click the sign in submit button
  

