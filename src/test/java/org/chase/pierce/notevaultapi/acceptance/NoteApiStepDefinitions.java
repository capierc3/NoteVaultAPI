package org.chase.pierce.notevaultapi.acceptance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NoteApiStepDefinitions {

    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ApiClient.ApiResponse lastResponse;
    private Long savedNoteId;

    @When("I create a note with name {string} and content {string}")
    public void createNote(String name, String content) {
        lastResponse = apiClient.post("/api/v1/notes",
                Map.of("name", name, "content", content));
    }

    @When("I get the note by saved ID")
    public void getNoteById() {
        lastResponse = apiClient.get("/api/v1/notes/" + savedNoteId);
    }

    @When("I get all notes")
    public void getAllNotes() {
        lastResponse = apiClient.get("/api/v1/notes");
    }

    @When("I update the note with name {string} and content {string}")
    public void updateNote(String name, String content) {
        lastResponse = apiClient.put("/api/v1/notes/" + savedNoteId,
                Map.of("name", name, "content", content));
    }

    @When("I delete the note by saved ID")
    public void deleteNote() {
        lastResponse = apiClient.delete("/api/v1/notes/" + savedNoteId);
    }

    @And("the response should contain the name {string}")
    public void verifyName(String expectedName) throws Exception {
        JsonNode json = objectMapper.readTree(lastResponse.body());
        assertEquals(expectedName, json.get("name").asText());
    }

    @And("the response should contain the content {string}")
    public void verifyContent(String expectedContent) throws Exception {
        JsonNode json = objectMapper.readTree(lastResponse.body());
        assertEquals(expectedContent, json.get("content").asText());
    }

    @And("I save the note ID from the response")
    public void saveNoteId() throws Exception {
        JsonNode json = objectMapper.readTree(lastResponse.body());
        savedNoteId = json.get("id").asLong();
        assertNotNull(savedNoteId, "Note ID should not be null");
    }

    @And("the response should be a non-empty list")
    public void verifyNonEmptyList() throws Exception {
        JsonNode json = objectMapper.readTree(lastResponse.body());
        assertTrue(json.isArray(), "Response should be an array");
        assertFalse(json.isEmpty(), "Response array should not be empty");
    }

    @Then("the response status should be {int}")
    public void verifyStatus(int expectedStatus) {
        assertEquals(expectedStatus, lastResponse.statusCode(),
                "Unexpected status. Body: " + lastResponse.body());
    }
}
