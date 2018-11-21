Feature: User can specify that a field is null or absent

  Background:
    Given the generation strategy is full

  Scenario: Using the 'null' operator generates null values
    Given there is a field foo
    And foo is null
    Then the following data should be generated:
      | foo  |
      | null |

  Scenario: Negating the 'null' operator generates non-null values
    Given there is a field foo
    And foo is anything but null
    Then the following data should not be included in what is generated:
      | foo  |
      | null |
