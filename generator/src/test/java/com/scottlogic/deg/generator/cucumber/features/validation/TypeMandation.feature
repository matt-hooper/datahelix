Feature: Type mandation validation
  Profiles should be rejected if they don't positively specify (to a certain standard) the types of all their fields.

  Scenario: A field with no relevant constraints should fail type mandation
    Given there is a field user_id
    And user_id is greater than 3
    And user_id is less than 10
    And user_id is granular to 1
    And user_id is formatted as "%d"
    # ideally I guess we'd have more here - what's a sensible amount? maybe we should use scenario outlines?
    Then field user_id should fail validation: "Field is untyped; add an ofType, equalTo or inSet constraint, or mark it as null"

  Scenario: An explicit type constraint should satisfy type mandation
    Given there is a field user_id
    And user_id is of type "string"
    Then the profile should be considered valid

  Scenario: An equalTo constraint should satisfy type mandation
    Given there is a field user_id
    And user_id is equal to "banana"
    Then the profile should be considered valid

  Scenario: An inSet constraint should satisfy type mandation
    Given there is a field user_id
    And user_id is in set:
      | "banana" |
      | "cactus" |
    Then the profile should be considered valid

  Scenario: A mandatorily absent field should satisfy type mandation
    Given there is a field user_id
    And user_id is null
    Then the profile should be considered valid

  Scenario: When only some fields fail type mandation, the errors should be specific to which
    Given there is a field user_id
    And user_id is of type "string"
    And there is a field price
    And there is a field purchase_time
    Then field price should fail validation: "Field is untyped; add an ofType, equalTo or inSet constraint, or mark it as null"
    And field purchase_time should fail validation: "Field is untyped; add an ofType, equalTo or inSet constraint, or mark it as null"

  Scenario: An anyOf constraint whose branches all satisfy type mandation should also satisfy type mandation
    Given there is a field user_id
    And there is a constraint:
      """
      { "anyOf": [
        { "field": "user_id", "is": "ofType", "value": "string" },
        { "field": "user_id", "is": "ofType", "value": "integer" }
      ]}
      """
    Then the profile should be considered valid

  Scenario: An anyOf constraint whose branches don't all satisfy type mandation should not satisfy type mandation
    Given there is a field user_id
    And there is a constraint:
      """
      { "anyOf": [
        { "field": "user_id", "is": "ofType", "value": "string" },
        { "not": { "field": "user_id", "is": "null" } }
      ]}
      """
    Then field user_id should fail validation: "Field is untyped; add an ofType, equalTo or inSet constraint, or mark it as null"

  Scenario: An if constraint should be able to satisfy type mandation
    Given there is a field user_id
    And there is a constraint:
      """
      { "if": { "not": { "field": "user_id", "is": "ofType", "value": "string" } },
        "then": { "field": "user_id", "is": "ofType", "value": "integer" } }
      """
    Then the profile should be considered valid

  @ignore # Current algorithm isn't sophisticated enough; see https://github.com/ScottLogic/datahelix/issues/797
  Scenario: A modus-ponens-style inference should satisfy type mandation
    Given there is a field user_id
    And there is a field user_type
    And there is a constraint:
      """
      { "if": { "field": "user_type", "is": "equalTo", "value": "admin" },
        "then": { "field": "user_id", "is": "ofType", "value": "string" } }
      """
    And user_type is equal to "admin"
    Then the profile should be considered valid
