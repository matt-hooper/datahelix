Feature: User can specify that datetime fields are granular to a certain unit

  Background:
    Given the generation strategy is full
    And there is a field foo
    And foo is anything but null
    And foo is of type "datetime"


  Scenario Outline: User is able to specify supported temporal granularities with after operator
    Given foo is granular to <unit>
    And foo is after 2000-01-01T00:00:00.000Z
    And the generator can generate at most 1 rows
    Then the following data should be generated:
      | foo       |
      | <output>  |

    Examples:
      | unit      | output                    |
      | "millis"  | 2000-01-01T00:00:00.001Z  |
      | "seconds" | 2000-01-01T00:00:01.000Z  |
      | "minutes" | 2000-01-01T00:01:00.000Z  |
      | "hours"   | 2000-01-01T01:00:00.000Z  |
      | "days"    | 2000-01-02T00:00:00.000Z  |
      | "months"  | 2000-02-01T00:00:00.000Z  |
      | "years"   | 2001-01-01T00:00:00.000Z  |

  Scenario Outline: User is able to specify supported temporal granularities with after or at operator
    Given foo is granular to <unit>
    And foo is after or at 2000-01-01T00:00:00.000Z
    And the generator can generate at most 2 rows
    Then the following data should be generated:
      | foo       |
      | <output1>  |
      | <output2>  |

    Examples:
      | unit      | output1                   | output2                   |
      | "millis"  | 2000-01-01T00:00:00.000Z  | 2000-01-01T00:00:00.001Z  |
      | "seconds" | 2000-01-01T00:00:00.000Z  | 2000-01-01T00:00:01.000Z  |
      | "minutes" | 2000-01-01T00:00:00.000Z  | 2000-01-01T00:01:00.000Z  |
      | "hours"   | 2000-01-01T00:00:00.000Z  | 2000-01-01T01:00:00.000Z  |
      | "days"    | 2000-01-01T00:00:00.000Z  | 2000-01-02T00:00:00.000Z  |
      | "months"  | 2000-01-01T00:00:00.000Z  | 2000-02-01T00:00:00.000Z  |
      | "years"   | 2000-01-01T00:00:00.000Z  | 2001-01-01T00:00:00.000Z  |

  Scenario Outline: User is able to specify supported temporal granularities with before operator
    Given foo is granular to <unit>
    And foo is before 2002-02-02T01:01:01.001Z
    And the generator can generate at most 2 rows
    Then the following data should be generated:
      | foo       |
      | <output1>  |
      | <output2>  |

    Examples:
      | unit      | output1                   | output2                   |
      | "millis"  | 0001-01-01T00:00:00.000Z  | 0001-01-01T00:00:00.001Z  |
      | "seconds" | 0001-01-01T00:00:00.000Z  | 0001-01-01T00:00:01.000Z  |
      | "minutes" | 0001-01-01T00:00:00.000Z  | 0001-01-01T00:01:00.000Z  |
      | "hours"   | 0001-01-01T00:00:00.000Z  | 0001-01-01T01:00:00.000Z  |
      | "days"    | 0001-01-01T00:00:00.000Z  | 0001-01-02T00:00:00.000Z  |
      | "months"  | 0001-01-01T00:00:00.000Z  | 0001-02-01T00:00:00.000Z  |
      | "years"   | 0001-01-01T00:00:00.000Z  | 0002-01-01T00:00:00.000Z  |

  Scenario Outline: User is able to specify supported temporal granularities with before or at operator
    Given foo is granular to <unit>
    And foo is before or at 2002-02-02T01:01:01.001Z
    And the generator can generate at most 2 rows
    Then the following data should be generated:
      | foo       |
      | <output1>  |
      | <output2>  |

    Examples:
      | unit      | output1                   | output2                   |
      | "millis"  | 0001-01-01T00:00:00.000Z  | 0001-01-01T00:00:00.001Z  |
      | "seconds" | 0001-01-01T00:00:00.000Z  | 0001-01-01T00:00:01.000Z  |
      | "minutes" | 0001-01-01T00:00:00.000Z  | 0001-01-01T00:01:00.000Z  |
      | "hours"   | 0001-01-01T00:00:00.000Z  | 0001-01-01T01:00:00.000Z  |
      | "days"    | 0001-01-01T00:00:00.000Z  | 0001-01-02T00:00:00.000Z  |
      | "months"  | 0001-01-01T00:00:00.000Z  | 0001-02-01T00:00:00.000Z  |
      | "years"   | 0001-01-01T00:00:00.000Z  | 0002-01-01T00:00:00.000Z  |

  Scenario: Applying two valid datetime granularTo constraints generates data that matches both (coarser)
    Given foo is granular to "millis"
    And foo is granular to "seconds"
    And foo is after 2000-01-01T00:00:00.000Z
    And the generator can generate at most 1 rows
    Then the following data should be generated:
      | foo                       |
      | 2000-01-01T00:00:01.000Z  |


  Scenario: Applying an invalid datetime granularTo constraint fails with an appropriate error
    Given foo is granular to "decades"
    Then the profile is invalid because "Field \[foo\]: Couldn't recognise granularity value, it must be either a negative power of ten or one of the supported datetime units."
    And no data is created

  Scenario: Applying a decimal granularTo constraint does not affect granularity
    Given foo is granular to 0.1
    And the generator can generate at most 1 rows
    And foo is after 2000-01-01T00:00:00.000Z
    Then the following data should be generated:
      | foo                       |
      | 2000-01-01T00:00:00.001Z  |
