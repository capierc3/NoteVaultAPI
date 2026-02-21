Feature: Notes API
  As an API consumer
  I want to create, read, update, and delete notes
  So that I can manage my notes through the REST API

  Scenario: Note can be created, found, updated and deleted

    # Create note
    When I create a note with name "Hello from Cucumber" and content "This is a test from Cucumber"
    Then the response status should be 201
    And the response should contain the name "Hello from Cucumber"
    And the response should contain the content "This is a test from Cucumber"
    And I save the note ID from the response

    # Find note with Id
    When I get the note by saved ID
    Then the response status should be 200
    And the response should contain the name "Hello from Cucumber"

    # List all notes
    When I get all notes
    Then the response status should be 200
    And the response should be a non-empty list

    # Update note
    When I update the note with name "Hello from Cucumber part deux" and content "This is still a test from Cucumber"
    Then the response status should be 200
    And the response should contain the name "Hello from Cucumber part deux"
    And the response should contain the content "This is still a test from Cucumber"

    # Note is deleted
    When I delete the note by saved ID
    Then the response status should be 204
    When I get the note by saved ID
    Then the response status should be 404
