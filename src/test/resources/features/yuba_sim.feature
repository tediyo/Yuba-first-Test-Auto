Feature: Yuba SIM
  As a user
  I want to complete the sign in process with dashboard navigation
  So that I can access the dashboard workspace

  @sim
  Scenario: Complete User Invitration to organization by org Admin
    Given I open the Yuba homepage
    When I click the Sign In button
    And I enter my email "tewodrosberhanu16@gmail.com"
    And I enter my password "Ttbbss12&$"
    And I click the sign in submit button
    And I wait for navigation to choose workspace page
    And I click the element "/html/body/div[1]/div/div/div[2]/div[1]"
    And I click the button "/html/body/div[1]/div/div/div[3]/button[1]"
    And I click the navigation link "/html/body/div[1]/aside/div[2]/nav/ul/li[2]/a"
    And I enter email "tewodrosberhanu19@gmail.com" in the individual email field
    And I click the form submit button "/html/body/div[1]/div/main/div/div[2]/div[1]/div/div[2]/div/div/div/div[2]/form/div[3]/div[2]/div[1]/div/button"
    Then the SIM test should complete successfully

  @ACM
  Scenario: Sign in and workspace selection flow
    Given I open the Yuba homepage
    When I click the Sign In button
    And I enter my email "tewodrosberhanu16@gmail.com"
    And I enter my password "Ttbbss12&$"
    And I click the sign in submit button
    And I wait for navigation to choose workspace page
    And I click the element "/html/body/div[1]/div/div/div[2]/div[1]"
    And I click the button "/html/body/div[1]/div/div/div[3]/button[1]"
    And I click the ACM navigation link "/html/body/div[1]/aside/div[2]/nav/ul/li[6]/a"
    And I click the ACM element "//*[@id=\"radix-«r1k»\"]"
    And I select the ACM element "//*[@id=\"radix-«r1l»\"]/div[3]"
    Then a new page should load for ACM test

  @ARefresh
  Scenario: Sign in and navigate to ACM workspace
    Given I open the Yuba homepage
    When I click the Sign In button
    And I enter my email "tewodrosberhanu16@gmail.com"
    And I enter my password "Ttbbss12&$"
    And I click the sign in submit button
    And I wait for navigation to choose workspace page
    And I click the element "/html/body/div[1]/div/div/div[2]/div[1]"
    And I click the button "/html/body/div[1]/div/div/div[3]/button[1]"
    And I click the ACM navigation link "/html/body/div[1]/aside/div[2]/nav/ul/li[6]/a"
    And I click the refresh button
    Then the page should refresh and load within acceptable time

