package org.chase.pierce.notevaultapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chase.pierce.notevaultapi.dto.CreateNoteRequest;
import org.chase.pierce.notevaultapi.dto.UpdateNoteRequest;
import org.chase.pierce.notevaultapi.entity.Note;
import org.chase.pierce.notevaultapi.entity.Role;
import org.chase.pierce.notevaultapi.entity.User;
import org.chase.pierce.notevaultapi.exception.GlobalExceptionHandler;
import org.chase.pierce.notevaultapi.exception.UnauthorizedAccessException;
import org.chase.pierce.notevaultapi.security.UserPrincipal;
import org.chase.pierce.notevaultapi.service.CustomUserDetailsService;
import org.chase.pierce.notevaultapi.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.chase.pierce.notevaultapi.exception.NoteNotFoundException;
import org.chase.pierce.notevaultapi.config.SecurityConfig;
import org.springframework.dao.QueryTimeoutException;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({NoteController.class, GlobalExceptionHandler.class})
@Import(SecurityConfig.class)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Stub the default user so DefaultUserFilter doesn't NPE
        User defaultUser = new User();
        defaultUser.setId(99L);
        defaultUser.setUsername("default_user");
        defaultUser.setPassword("encoded");
        defaultUser.setRole(Role.USER);
        defaultUser.setEnabled(true);
        when(customUserDetailsService.loadUserByUsername("default_user"))
                .thenReturn(new UserPrincipal(defaultUser));
    }

    private UserPrincipal testUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encoded");
        user.setRole(Role.USER);
        user.setEnabled(true);
        return new UserPrincipal(user);
    }

    private UserPrincipal adminUser() {
        User user = new User();
        user.setId(2L);
        user.setUsername("admin");
        user.setPassword("encoded");
        user.setRole(Role.ADMIN);
        user.setEnabled(true);
        return new UserPrincipal(user);
    }

    // --- GET /notes ---

    @Test
    void testGetAllNotesReturnsOk() throws Exception {
        Note note1 = new Note();
        note1.setId(1L);
        note1.setName("Note 1");
        note1.setContent("Content 1");
        note1.setUserId("testuser");

        Note note2 = new Note();
        note2.setId(2L);
        note2.setName("Note 2");
        note2.setContent("Content 2");
        note2.setUserId("testuser");

        when(noteService.getNotesByFilters(any(), eq("testuser"), any())).thenReturn(List.of(note1, note2));

        mockMvc.perform(get("/api/v1/notes").with(user(testUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Note 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Note 2"));
    }

    @Test
    void testGetAllNotesReturnsEmptyList() throws Exception {
        when(noteService.getNotesByFilters(any(), eq("testuser"), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/notes").with(user(testUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testAdminGetsAllNotes() throws Exception {
        Note note = new Note();
        note.setId(1L);
        note.setName("Any Note");

        when(noteService.getAllNotes()).thenReturn(List.of(note));

        mockMvc.perform(get("/api/v1/notes").with(user(adminUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetNotesByTagsReturnsOk() throws Exception {
        Note note = new Note();
        note.setId(1L);
        note.setName("Tagged Note");

        when(noteService.getNotesByFilters(eq(Set.of("work")), any(), any())).thenReturn(List.of(note));

        mockMvc.perform(get("/api/v1/notes").param("tags", "work").with(user(testUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Tagged Note"));
    }

    @Test
    void testGetNotesByNotebookIdReturnsOk() throws Exception {
        Note note = new Note();
        note.setId(1L);
        note.setName("Notebook Note");

        when(noteService.getNotesByFilters(any(), any(), eq(5L))).thenReturn(List.of(note));

        mockMvc.perform(get("/api/v1/notes").param("notebookId", "5").with(user(testUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Notebook Note"));
    }

    @Test
    void testGetNotesReturns400WhenNotebookIdNotNumeric() throws Exception {
        mockMvc.perform(get("/api/v1/notes").param("notebookId", "abc").with(user(testUser())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameter"))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'notebookId'. Expected type: Long"));
    }

    // --- GET /notes/{id} ---

    @Test
    void testGetNoteByIdReturnsOk() throws Exception {
        Note note = new Note();
        note.setId(1L);
        note.setName("Test Note");
        note.setContent("Some content");
        note.setUserId("testuser");

        when(noteService.getNoteById(eq(1L), eq("testuser"), eq(Role.USER))).thenReturn(note);

        mockMvc.perform(get("/api/v1/notes/1").with(user(testUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Note"))
                .andExpect(jsonPath("$.content").value("Some content"))
                .andExpect(jsonPath("$.userId").value("testuser"));
    }

    @Test
    void testGetNoteByIdReturns400WhenIdNotNumeric() throws Exception {
        mockMvc.perform(get("/api/v1/notes/abc").with(user(testUser())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameter"))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'. Expected type: Long"));
    }

    @Test
    void testGetNoteByIdReturns404WhenNotFound() throws Exception {
        when(noteService.getNoteById(eq(99L), any(), any())).thenThrow(new NoteNotFoundException(99L));

        mockMvc.perform(get("/api/v1/notes/99").with(user(testUser())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not found"))
                .andExpect(jsonPath("$.message").value("Note not found with id: 99"));
    }

    @Test
    void testGetNoteByIdReturns403WhenNotOwner() throws Exception {
        when(noteService.getNoteById(eq(1L), any(), any()))
                .thenThrow(new UnauthorizedAccessException("You do not have permission to access note with id: 1"));

        mockMvc.perform(get("/api/v1/notes/1").with(user(testUser())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    // --- DELETE /notes/{id} ---

    @Test
    void testDeleteNoteReturnsNoContent() throws Exception {
        doNothing().when(noteService).deleteNoteById(eq(1L), eq("testuser"), eq(Role.USER));

        mockMvc.perform(delete("/api/v1/notes/1").with(user(testUser())))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteNoteReturns404WhenNotFound() throws Exception {
        doThrow(new NoteNotFoundException(99L)).when(noteService).deleteNoteById(eq(99L), any(), any());

        mockMvc.perform(delete("/api/v1/notes/99").with(user(testUser())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not found"))
                .andExpect(jsonPath("$.message").value("Note not found with id: 99"));
    }

    @Test
    void testDeleteNoteReturns400WhenIdNotNumeric() throws Exception {
        mockMvc.perform(delete("/api/v1/notes/abc").with(user(testUser())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameter"));
    }

    @Test
    void testDeleteNoteReturns403WhenNotOwner() throws Exception {
        doThrow(new UnauthorizedAccessException("You do not have permission to access note with id: 1"))
                .when(noteService).deleteNoteById(eq(1L), any(), any());

        mockMvc.perform(delete("/api/v1/notes/1").with(user(testUser())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    // --- Database errors ---

    @Test
    void testGetAllNotesReturns503WhenDbUnavailable() throws Exception {
        when(noteService.getNotesByFilters(any(), any(), any())).thenThrow(new QueryTimeoutException("Connection refused"));

        mockMvc.perform(get("/api/v1/notes").with(user(testUser())))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Service unavailable"))
                .andExpect(jsonPath("$.message").value("Unable to connect to the database. Please try again later."));
    }

    @Test
    void testCreateNoteReturns503WhenDbUnavailable() throws Exception {
        CreateNoteRequest request = new CreateNoteRequest();
        request.setName("Test Note");
        request.setContent("Some content");

        when(noteService.createNote(any(CreateNoteRequest.class), any()))
                .thenThrow(new QueryTimeoutException("Connection refused"));

        mockMvc.perform(post("/api/v1/notes")
                        .with(user(testUser()))
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

        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setName("Test Note");
        savedNote.setContent("Some content");
        savedNote.setUserId("testuser");

        when(noteService.createNote(any(CreateNoteRequest.class), eq("testuser"))).thenReturn(savedNote);

        mockMvc.perform(post("/api/v1/notes")
                        .with(user(testUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Note"))
                .andExpect(jsonPath("$.content").value("Some content"))
                .andExpect(jsonPath("$.userId").value("testuser"));
    }

    @ParameterizedTest(name = "blank {0} returns 400")
    @MethodSource("blankFieldCases")
    void testBlankFieldReturns400(String fieldName, String name, String content) throws Exception {
        CreateNoteRequest request = new CreateNoteRequest();
        request.setName(name);
        request.setContent(content);

        mockMvc.perform(post("/api/v1/notes")
                        .with(user(testUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields." + fieldName).exists());
    }

    static Stream<Arguments> blankFieldCases() {
        return Stream.of(
                Arguments.of("name", "", "Some content"),
                Arguments.of("name", null, "Some content"),
                Arguments.of("content", "Test Note", ""),
                Arguments.of("content", "Test Note", null)
        );
    }

    @Test
    void testAllFieldsMissingReturns400() throws Exception {
        CreateNoteRequest request = new CreateNoteRequest();

        mockMvc.perform(post("/api/v1/notes")
                        .with(user(testUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists())
                .andExpect(jsonPath("$.fields.content").exists());
    }

    @Test
    void testNameExceedsMaxLength() throws Exception {
        CreateNoteRequest request = new CreateNoteRequest();
        request.setName("a".repeat(256));
        request.setContent("Some content");

        mockMvc.perform(post("/api/v1/notes")
                        .with(user(testUser()))
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
        request.setTags(Set.of("work", "important"));

        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setName("Tagged Note");
        savedNote.setContent("Content");
        savedNote.setUserId("testuser");

        when(noteService.createNote(any(CreateNoteRequest.class), any())).thenReturn(savedNote);

        mockMvc.perform(post("/api/v1/notes")
                        .with(user(testUser()))
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

        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setName("Minimal Note");
        savedNote.setContent("Content");
        savedNote.setUserId("testuser");

        when(noteService.createNote(any(CreateNoteRequest.class), any())).thenReturn(savedNote);

        mockMvc.perform(post("/api/v1/notes")
                        .with(user(testUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    // --- PUT /notes/{id} ---

    @Test
    void testUpdateNoteReturnsOk() throws Exception {
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setName("Updated Note");
        request.setContent("Updated content");

        Note updatedNote = new Note();
        updatedNote.setId(1L);
        updatedNote.setName("Updated Note");
        updatedNote.setContent("Updated content");
        updatedNote.setUserId("testuser");

        when(noteService.updateNote(eq(1L), any(UpdateNoteRequest.class), eq("testuser"), eq(Role.USER)))
                .thenReturn(updatedNote);

        mockMvc.perform(put("/api/v1/notes/1")
                        .with(user(testUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Note"))
                .andExpect(jsonPath("$.content").value("Updated content"))
                .andExpect(jsonPath("$.userId").value("testuser"));
    }

    @Test
    void testUpdateNoteReturns404WhenNotFound() throws Exception {
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setName("Updated Note");
        request.setContent("Updated content");

        when(noteService.updateNote(eq(99L), any(UpdateNoteRequest.class), any(), any()))
                .thenThrow(new NoteNotFoundException(99L));

        mockMvc.perform(put("/api/v1/notes/99")
                        .with(user(testUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not found"))
                .andExpect(jsonPath("$.message").value("Note not found with id: 99"));
    }

    @Test
    void testUpdateNoteReturns400WhenIdNotNumeric() throws Exception {
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setName("Updated Note");
        request.setContent("Updated content");

        mockMvc.perform(put("/api/v1/notes/abc")
                        .with(user(testUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameter"));
    }

    @Test
    void testUpdateNoteReturns403WhenNotOwner() throws Exception {
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setName("Updated Note");
        request.setContent("Updated content");

        when(noteService.updateNote(eq(1L), any(UpdateNoteRequest.class), any(), any()))
                .thenThrow(new UnauthorizedAccessException("You do not have permission to access note with id: 1"));

        mockMvc.perform(put("/api/v1/notes/1")
                        .with(user(testUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @ParameterizedTest(name = "update blank {0} returns 400")
    @MethodSource("updateBlankFieldCases")
    void testUpdateBlankFieldReturns400(String fieldName, String name, String content) throws Exception {
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setName(name);
        request.setContent(content);

        mockMvc.perform(put("/api/v1/notes/1")
                        .with(user(testUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields." + fieldName).exists());
    }

    static Stream<Arguments> updateBlankFieldCases() {
        return Stream.of(
                Arguments.of("name", "", "Some content"),
                Arguments.of("name", null, "Some content"),
                Arguments.of("content", "Test Note", ""),
                Arguments.of("content", "Test Note", null)
        );
    }

    @Test
    void testUpdateNoteNameExceedsMaxLength() throws Exception {
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setName("a".repeat(256));
        request.setContent("Some content");

        mockMvc.perform(put("/api/v1/notes/1")
                        .with(user(testUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").value("Name must be 255 characters or fewer"));
    }

    @Test
    void testUpdateNoteWithTags() throws Exception {
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setName("Updated Note");
        request.setContent("Updated content");
        request.setTags(Set.of("work", "important"));

        Note updatedNote = new Note();
        updatedNote.setId(1L);
        updatedNote.setName("Updated Note");
        updatedNote.setContent("Updated content");
        updatedNote.setUserId("testuser");

        when(noteService.updateNote(eq(1L), any(UpdateNoteRequest.class), any(), any())).thenReturn(updatedNote);

        mockMvc.perform(put("/api/v1/notes/1")
                        .with(user(testUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Note"));
    }

    @Test
    void testUpdateNoteReturns503WhenDbUnavailable() throws Exception {
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setName("Updated Note");
        request.setContent("Updated content");

        when(noteService.updateNote(eq(1L), any(UpdateNoteRequest.class), any(), any()))
                .thenThrow(new QueryTimeoutException("Connection refused"));

        mockMvc.perform(put("/api/v1/notes/1")
                        .with(user(testUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Service unavailable"))
                .andExpect(jsonPath("$.message").value("Unable to connect to the database. Please try again later."));
    }
}
