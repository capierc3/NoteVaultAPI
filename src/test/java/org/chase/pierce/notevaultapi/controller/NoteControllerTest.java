package org.chase.pierce.notevaultapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chase.pierce.notevaultapi.dto.CreateNoteRequest;
import org.chase.pierce.notevaultapi.entity.Note;
import org.chase.pierce.notevaultapi.exception.GlobalExceptionHandler;
import org.chase.pierce.notevaultapi.service.NoteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.dao.QueryTimeoutException;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({NoteController.class, GlobalExceptionHandler.class})
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- GET /notes ---

    @Test
    void testGetAllNotesReturnsOk() throws Exception {
        Note note1 = new Note();
        note1.setId(1L);
        note1.setName("Note 1");
        note1.setContent("Content 1");
        note1.setUserId("user1");

        Note note2 = new Note();
        note2.setId(2L);
        note2.setName("Note 2");
        note2.setContent("Content 2");
        note2.setUserId("user2");

        when(noteService.getAllNotes()).thenReturn(List.of(note1, note2));

        mockMvc.perform(get("/api/v1/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Note 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Note 2"));
    }

    @Test
    void testGetAllNotesReturnsEmptyList() throws Exception {
        when(noteService.getAllNotes()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- Database errors ---

    @Test
    void testGetAllNotesReturns503WhenDbUnavailable() throws Exception {
        when(noteService.getAllNotes()).thenThrow(new QueryTimeoutException("Connection refused"));

        mockMvc.perform(get("/api/v1/notes"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Service unavailable"))
                .andExpect(jsonPath("$.message").value("Unable to connect to the database. Please try again later."));
    }

    @Test
    void testCreateNoteReturns503WhenDbUnavailable() throws Exception {
        CreateNoteRequest request = new CreateNoteRequest();
        request.setName("Test Note");
        request.setContent("Some content");
        request.setUserId("user123");

        when(noteService.createNote(any(CreateNoteRequest.class)))
                .thenThrow(new QueryTimeoutException("Connection refused"));

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Service unavailable"))
                .andExpect(jsonPath("$.message").value("Unable to connect to the database. Please try again later."));
    }

    // --- POST /notes ---

    @Test
    void testValidRequest() throws Exception {
        CreateNoteRequest request = new CreateNoteRequest();
        request.setName("Test Note");
        request.setContent("Some content");
        request.setUserId("user123");

        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setName("Test Note");
        savedNote.setContent("Some content");
        savedNote.setUserId("user123");

        when(noteService.createNote(any(CreateNoteRequest.class))).thenReturn(savedNote);

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Note"))
                .andExpect(jsonPath("$.content").value("Some content"))
                .andExpect(jsonPath("$.userId").value("user123"));
    }

    // --- Single-field validation failures ---

    @ParameterizedTest(name = "blank {0} returns 400")
    @MethodSource("blankFieldCases")
    void testBlankFieldReturns400(String fieldName, String name, String content, String userId) throws Exception {
        CreateNoteRequest request = new CreateNoteRequest();
        request.setName(name);
        request.setContent(content);
        request.setUserId(userId);

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields." + fieldName).exists());
    }

    static Stream<Arguments> blankFieldCases() {
        return Stream.of(
                Arguments.of("name", "", "Some content", "user123"),
                Arguments.of("name", null, "Some content", "user123"),
                Arguments.of("content", "Test Note", "", "user123"),
                Arguments.of("content", "Test Note", null, "user123"),
                Arguments.of("userId", "Test Note", "Some content", ""),
                Arguments.of("userId", "Test Note", "Some content", null)
        );
    }

    @Test
    void testAllFieldsMissingReturns400() throws Exception {
        CreateNoteRequest request = new CreateNoteRequest();

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists())
                .andExpect(jsonPath("$.fields.content").exists())
                .andExpect(jsonPath("$.fields.userId").exists());
    }

    @Test
    void testNameExceedsMaxLength() throws Exception {
        CreateNoteRequest request = new CreateNoteRequest();
        request.setName("a".repeat(256));
        request.setContent("Some content");
        request.setUserId("user123");

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").value("Name must be 255 characters or fewer"));
    }

    @Test
    void testWithTags() throws Exception {
        CreateNoteRequest request = new CreateNoteRequest();
        request.setName("Tagged Note");
        request.setContent("Content");
        request.setUserId("user123");
        request.setTags(Set.of("work", "important"));

        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setName("Tagged Note");
        savedNote.setContent("Content");
        savedNote.setUserId("user123");

        when(noteService.createNote(any(CreateNoteRequest.class))).thenReturn(savedNote);

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Tagged Note"));
    }

    @Test
    void testWithoutOptionalFields() throws Exception {
        CreateNoteRequest request = new CreateNoteRequest();
        request.setName("Minimal Note");
        request.setContent("Content");
        request.setUserId("user123");

        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setName("Minimal Note");
        savedNote.setContent("Content");
        savedNote.setUserId("user123");

        when(noteService.createNote(any(CreateNoteRequest.class))).thenReturn(savedNote);

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }
}
