Feature: Yuba Website Performance and Compatibility Testing
  As a QA engineer
  I want to verify the Yuba website meets performance and compatibility standards
  So that users have a reliable and fast experience

  @performance
  Scenario: Verify URL and Page Load Criteria
    Given I navigate to the Yuba website
    When the page loads
    Then the website should load successfully
    And the HTTP status code should be 200 OK
    And there should be no server errors
    And the page load time should be less than 10 seconds

  @compatibility @responsive
  Scenario: Verify Desktop Responsive Behavior
    Given I navigate to the Yuba website with desktop viewport
    When the page loads completely
    Then the desktop layout should render correctly
    And there should be no UI misalignment or overlapping elements
    And there should be no horizontal scrolling
    And all main UI components should be visible

  @compatibility @browser
  Scenario Outline: Verify Browser Compatibility
    Given I navigate to the Yuba website using <browser>
    When the page loads
    Then the website should load successfully
    And the page should render without errors

    Examples:
      | browser |
      | Edge    |
      | Chrome  |

