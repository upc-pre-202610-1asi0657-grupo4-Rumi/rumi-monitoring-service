Feature: Structural monitoring readings
  As a building administrator
  I want to review sensor readings
  So that I can understand the current condition and history of my building

  Scenario: Show the latest reading of each zone
    Given the monitoring database contains readings for two zones and multiple timestamps
    When I request the latest readings for the building
    Then the response contains the newest reading for each zone

  Scenario: Retrieve building readings for a date range
    Given the monitoring database contains readings inside and outside a date range
    When I request the reading history for that date range
    Then the response contains only readings in the range in chronological order
