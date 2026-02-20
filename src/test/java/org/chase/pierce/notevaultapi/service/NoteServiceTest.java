package org.chase.pierce.notevaultapi.service;

import org.chase.pierce.notevaultapi.dto.CreateNoteRequest;
import org.chase.pierce.notevaultapi.dto.UpdateNoteRequest;
import org.chase.pierce.notevaultapi.entity.Note;
import org.chase.pierce.notevaultapi.entity.Tag;
import org.chase.pierce.notevaultapi.repository.NoteRepository;
import org.chase.pierce.notevaultapi.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.chase.pierce.notevaultapi.exception.NoteNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private NoteService noteService;

    private CreateNoteRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new CreateNoteRequest();
        validRequest.setName("Test Note");
        validRequest.setContent("Some content");
        validRequest.setUserId("user123");
    }

    @Test
    void testGetAllNotesReturnsAllNotes() {
        Note note1 = new Note();
        note1.setId(1L);
        note1.setName("Note 1");

        Note note2 = new Note();
        note2.setId(2L);
        note2.setName("Note 2");

        when(noteRepository.findAll()).thenReturn(List.of(note1, note2));

        List<Note> result = noteService.getAllNotes();

        assertEquals(2, result.size());
        assertEquals("Note 1", result.get(0).getName());
        assertEquals("Note 2", result.get(1).getName());
        verify(noteRepository).findAll();
    }

    @Test
    void testGetAllNotesReturnsEmptyList() {
        when(noteRepository.findAll()).thenReturn(List.of());

        List<Note> result = noteService.getAllNotes();

        assertTrue(result.isEmpty());
        verify(noteRepository).findAll();
    }

    @Test
    void testFilterByTagsOnly() {
        Note note = new Note();
        note.setId(1L);
        Set<String> tags = Set.of("work");

        when(noteRepository.findByTagNames(tags)).thenReturn(List.of(note));

        List<Note> result = noteService.getNotesByFilters(tags, null, null);

        assertEquals(1, result.size());
        verify(noteRepository).findByTagNames(tags);
    }

    @Test
    void testFilterByUserIdOnly() {
        Note note = new Note();
        note.setId(1L);

        when(noteRepository.findByUserId("user123")).thenReturn(List.of(note));

        List<Note> result = noteService.getNotesByFilters(null, "user123", null);

        assertEquals(1, result.size());
        verify(noteRepository).findByUserId("user123");
    }

    @Test
    void testFilterByNotebookIdOnly() {
        Note note = new Note();
        note.setId(1L);

        when(noteRepository.findByNotebookId(5L)).thenReturn(List.of(note));

        List<Note> result = noteService.getNotesByFilters(null, null, 5L);

        assertEquals(1, result.size());
        verify(noteRepository).findByNotebookId(5L);
    }

    @Test
    void testFilterByTagsAndUserId() {
        Note note = new Note();
        note.setId(1L);
        Set<String> tags = Set.of("work");

        when(noteRepository.findByTagNamesAndUserId(tags, "user123")).thenReturn(List.of(note));

        List<Note> result = noteService.getNotesByFilters(tags, "user123", null);

        assertEquals(1, result.size());
        verify(noteRepository).findByTagNamesAndUserId(tags, "user123");
    }

    @Test
    void testFilterByTagsAndNotebookId() {
        Note note = new Note();
        note.setId(1L);
        Set<String> tags = Set.of("work");

        when(noteRepository.findByTagNamesAndNotebookId(tags, 5L)).thenReturn(List.of(note));

        List<Note> result = noteService.getNotesByFilters(tags, null, 5L);

        assertEquals(1, result.size());
        verify(noteRepository).findByTagNamesAndNotebookId(tags, 5L);
    }

    @Test
    void testFilterByUserIdAndNotebookId() {
        Note note = new Note();
        note.setId(1L);

        when(noteRepository.findByUserIdAndNotebookId("user123", 5L)).thenReturn(List.of(note));

        List<Note> result = noteService.getNotesByFilters(null, "user123", 5L);

        assertEquals(1, result.size());
        verify(noteRepository).findByUserIdAndNotebookId("user123", 5L);
    }

    @Test
    void testFilterByAllThree() {
        Note note = new Note();
        note.setId(1L);
        Set<String> tags = Set.of("work");

        when(noteRepository.findByTagNamesAndUserIdAndNotebookId(tags, "user123", 5L)).thenReturn(List.of(note));

        List<Note> result = noteService.getNotesByFilters(tags, "user123", 5L);

        assertEquals(1, result.size());
        verify(noteRepository).findByTagNamesAndUserIdAndNotebookId(tags, "user123", 5L);
    }

    @Test
    void testFilterByNoParams() {
        when(noteRepository.findAll()).thenReturn(List.of());

        List<Note> result = noteService.getNotesByFilters(null, null, null);

        assertTrue(result.isEmpty());
        verify(noteRepository).findAll();
    }

    @Test
    void testGetNoteById() {
        Note note = new Note();
        note.setId(1L);
        note.setName("Test Note");

        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        Note result = noteService.getNoteById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Test Note", result.getName());
        verify(noteRepository).findById(1L);
    }

    @Test
    void testGetNoteByIdNotFound() {
        when(noteRepository.findById(99L)).thenReturn(Optional.empty());

        NoteNotFoundException exception = assertThrows(
                NoteNotFoundException.class,
                () -> noteService.getNoteById(99L)
        );

        assertEquals("Note not found with id: 99", exception.getMessage());
        verify(noteRepository).findById(99L);
    }

    // --- deleteNoteById ---

    @Test
    void testDeleteNoteByIdSucceeds() {
        when(noteRepository.existsById(1L)).thenReturn(true);

        noteService.deleteNoteById(1L);

        verify(noteRepository).existsById(1L);
        verify(noteRepository).deleteById(1L);
    }

    @Test
    void testDeleteNoteByIdThrowsWhenNotFound() {
        when(noteRepository.existsById(99L)).thenReturn(false);

        NoteNotFoundException exception = assertThrows(
                NoteNotFoundException.class,
                () -> noteService.deleteNoteById(99L)
        );

        assertEquals("Note not found with id: 99", exception.getMessage());
        verify(noteRepository).existsById(99L);
        verify(noteRepository, never()).deleteById(any());
    }

    // --- createNote ---

    @Test
    void testSavesNoteWithSanitizedFields() {
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note result = noteService.createNote(validRequest);

        assertEquals("Test Note", result.getName());
        assertEquals("Some content", result.getContent());
        assertEquals("user123", result.getUserId());
        verify(noteRepository).save(any(Note.class));
    }

    @ParameterizedTest(name = "name \"{0}\" → \"{1}\"")
    @MethodSource("nameSanitizationCases")
    void testSanitizesName(String inputName, String expectedName) {
        validRequest.setName(inputName);
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note result = noteService.createNote(validRequest);

        assertEquals(expectedName, result.getName());
    }

    static Stream<Arguments> nameSanitizationCases() {
        return Stream.of(
                Arguments.of("<script>alert('xss')</script>My Note", "alert('xss')My Note"),
                Arguments.of("<b>Bold Name</b>", "Bold Name"),
                Arguments.of("<div><p>Nested</p></div>", "Nested"),
                Arguments.of("Plain Name", "Plain Name"),
                Arguments.of("<img onerror=\"hack()\">Title", "Title")
        );
    }

    @ParameterizedTest(name = "content \"{0}\" → \"{1}\"")
    @MethodSource("contentSanitizationCases")
    void testSanitizesContent(String inputContent, String expectedContent) {
        validRequest.setContent(inputContent);
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note result = noteService.createNote(validRequest);

        assertEquals(expectedContent, result.getContent());
    }

    static Stream<Arguments> contentSanitizationCases() {
        return Stream.of(
                Arguments.of("<p>Hello</p><script>alert('xss')</script>", "<p>Hello</p>alert('xss')"),
                Arguments.of("<p>Hello <b>World</b></p>", "<p>Hello <b>World</b></p>"),
                Arguments.of("<iframe src=\"evil.com\"></iframe>Safe text", "Safe text"),
                Arguments.of("<a href=\"https://ok.com\">Link</a>", "<a href=\"https://ok.com\">Link</a>"),
                Arguments.of("<div onclick=\"steal()\">Click</div>", "<div >Click</div>")
        );
    }

    @Test
    void testSavesWithoutTags() {
        validRequest.setTags(null);
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note result = noteService.createNote(validRequest);

        assertTrue(result.getTags().isEmpty());
        verify(tagRepository, never()).findByName(any());
    }

    @Test
    void testSavesEmptyTags() {
        validRequest.setTags(Set.of());
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note result = noteService.createNote(validRequest);

        assertTrue(result.getTags().isEmpty());
        verify(tagRepository, never()).findByName(any());
    }

    @Test
    void testReusesTag() {
        Tag existingTag = new Tag();
        existingTag.setId(1L);
        existingTag.setName("work");

        validRequest.setTags(Set.of("work"));
        when(tagRepository.findByName("work")).thenReturn(Optional.of(existingTag));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note result = noteService.createNote(validRequest);

        assertEquals(1, result.getTags().size());
        assertTrue(result.getTags().contains(existingTag));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void testCreatesAndSavesTag() {
        Tag savedTag = new Tag();
        savedTag.setId(1L);
        savedTag.setName("newtag");

        validRequest.setTags(Set.of("newtag"));
        when(tagRepository.findByName("newtag")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenReturn(savedTag);
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note result = noteService.createNote(validRequest);

        assertEquals(1, result.getTags().size());
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void testSanitizesTagName() {
        validRequest.setTags(Set.of("<b>work</b>"));
        when(tagRepository.findByName("work")).thenReturn(Optional.empty());
        Tag savedTag = new Tag();
        savedTag.setId(1L);
        savedTag.setName("work");
        when(tagRepository.save(any(Tag.class))).thenReturn(savedTag);
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        noteService.createNote(validRequest);

        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).save(tagCaptor.capture());
        assertEquals("work", tagCaptor.getValue().getName());
    }

    // --- updateNote ---

    @Test
    void testUpdateNoteSuccessfully() {
        Note existingNote = new Note();
        existingNote.setId(1L);
        existingNote.setName("Old Name");
        existingNote.setContent("Old content");
        existingNote.setUserId("user123");

        UpdateNoteRequest updateRequest = new UpdateNoteRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setContent("Updated content");
        updateRequest.setUserId("user456");

        when(noteRepository.findById(1L)).thenReturn(Optional.of(existingNote));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note result = noteService.updateNote(1L, updateRequest);

        assertEquals("Updated Name", result.getName());
        assertEquals("Updated content", result.getContent());
        assertEquals("user456", result.getUserId());
        verify(noteRepository).findById(1L);
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void testUpdateNoteThrowsWhenNotFound() {
        UpdateNoteRequest updateRequest = new UpdateNoteRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setContent("Updated content");
        updateRequest.setUserId("user123");

        when(noteRepository.findById(99L)).thenReturn(Optional.empty());

        NoteNotFoundException exception = assertThrows(
                NoteNotFoundException.class,
                () -> noteService.updateNote(99L, updateRequest)
        );

        assertEquals("Note not found with id: 99", exception.getMessage());
        verify(noteRepository).findById(99L);
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void testUpdateNoteSanitizesFields() {
        Note existingNote = new Note();
        existingNote.setId(1L);
        existingNote.setName("Old Name");
        existingNote.setContent("Old content");
        existingNote.setUserId("user123");

        UpdateNoteRequest updateRequest = new UpdateNoteRequest();
        updateRequest.setName("<script>alert('xss')</script>Updated");
        updateRequest.setContent("<p>Safe</p><script>evil()</script>");
        updateRequest.setUserId("<b>user456</b>");

        when(noteRepository.findById(1L)).thenReturn(Optional.of(existingNote));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note result = noteService.updateNote(1L, updateRequest);

        assertEquals("alert('xss')Updated", result.getName());
        assertEquals("<p>Safe</p>evil()", result.getContent());
        assertEquals("user456", result.getUserId());
    }

    @Test
    void testUpdateNoteWithTags() {
        Note existingNote = new Note();
        existingNote.setId(1L);
        existingNote.setName("Old Name");
        existingNote.setContent("Old content");
        existingNote.setUserId("user123");
        existingNote.setTags(new HashSet<>());

        Tag existingTag = new Tag();
        existingTag.setId(1L);
        existingTag.setName("work");

        UpdateNoteRequest updateRequest = new UpdateNoteRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setContent("Updated content");
        updateRequest.setUserId("user123");
        updateRequest.setTags(Set.of("work"));

        when(noteRepository.findById(1L)).thenReturn(Optional.of(existingNote));
        when(tagRepository.findByName("work")).thenReturn(Optional.of(existingTag));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note result = noteService.updateNote(1L, updateRequest);

        assertEquals(1, result.getTags().size());
        assertTrue(result.getTags().contains(existingTag));
    }

    @Test
    void testUpdateNoteClearsTags() {
        Tag oldTag = new Tag();
        oldTag.setId(1L);
        oldTag.setName("old-tag");

        Note existingNote = new Note();
        existingNote.setId(1L);
        existingNote.setName("Old Name");
        existingNote.setContent("Old content");
        existingNote.setUserId("user123");
        existingNote.setTags(new HashSet<>(Set.of(oldTag)));

        UpdateNoteRequest updateRequest = new UpdateNoteRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setContent("Updated content");
        updateRequest.setUserId("user123");
        updateRequest.setTags(null);

        when(noteRepository.findById(1L)).thenReturn(Optional.of(existingNote));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note result = noteService.updateNote(1L, updateRequest);

        assertTrue(result.getTags().isEmpty());
    }

    @Test
    void testUpdateNoteCreatesNewTag() {
        Note existingNote = new Note();
        existingNote.setId(1L);
        existingNote.setName("Old Name");
        existingNote.setContent("Old content");
        existingNote.setUserId("user123");

        Tag savedTag = new Tag();
        savedTag.setId(1L);
        savedTag.setName("newtag");

        UpdateNoteRequest updateRequest = new UpdateNoteRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setContent("Updated content");
        updateRequest.setUserId("user123");
        updateRequest.setTags(Set.of("newtag"));

        when(noteRepository.findById(1L)).thenReturn(Optional.of(existingNote));
        when(tagRepository.findByName("newtag")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenReturn(savedTag);
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note result = noteService.updateNote(1L, updateRequest);

        assertEquals(1, result.getTags().size());
        verify(tagRepository).save(any(Tag.class));
    }
}
