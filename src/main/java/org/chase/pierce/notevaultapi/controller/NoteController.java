package org.chase.pierce.notevaultapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.chase.pierce.notevaultapi.dto.CreateNoteRequest;
import org.chase.pierce.notevaultapi.dto.UpdateNoteRequest;
import org.chase.pierce.notevaultapi.entity.Note;
import org.chase.pierce.notevaultapi.entity.Role;
import org.chase.pierce.notevaultapi.security.UserPrincipal;
import org.chase.pierce.notevaultapi.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Notes", description = "Endpoints for managing notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @Operation(summary = "Get all notes", description = "Returns notes for the authenticated user. Admins can see all notes or filter by user ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notes retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameter type", content = @Content),
            @ApiResponse(responseCode = "503", description = "Database unavailable", content = @Content)
    })
    @GetMapping("/notes")
    public ResponseEntity<List<Note>> getNotes(
            @Parameter(description = "Filter by tag names") @RequestParam(required = false) Set<String> tags,
            @Parameter(description = "Filter by user ID (admin only)") @RequestParam(required = false) String userId,
            @Parameter(description = "Filter by notebook ID") @RequestParam(required = false) Long notebookId,
            @AuthenticationPrincipal UserPrincipal principal) {

        String effectiveUserId = principal.user().getRole() == Role.ADMIN
                ? userId : principal.getUsername();

        List<Note> notes;
        if (tags != null || effectiveUserId != null || notebookId != null) {
            notes = noteService.getNotesByFilters(tags, effectiveUserId, notebookId);
        } else {
            notes = noteService.getAllNotes();
        }
        return ResponseEntity.ok(notes);
    }

    @Operation(summary = "Get note by ID", description = "Returns a single note. Users can only access their own notes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note found"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Note not found", content = @Content),
            @ApiResponse(responseCode = "503", description = "Database unavailable", content = @Content)
    })
    @GetMapping("/notes/{id}")
    public ResponseEntity<Note> getNoteById(
            @Parameter(description = "ID of the note to retrieve") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Note note = noteService.getNoteById(id, principal.getUsername(), principal.user().getRole());
        return ResponseEntity.ok(note);
    }

    @Operation(summary = "Delete note by ID", description = "Deletes a note. Users can only delete their own notes.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Note deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Note not found", content = @Content),
            @ApiResponse(responseCode = "503", description = "Database unavailable", content = @Content)
    })
    @DeleteMapping("/notes/{id}")
    public ResponseEntity<Void> deleteNoteById(
            @Parameter(description = "ID of the note to delete") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        noteService.deleteNoteById(id, principal.getUsername(), principal.user().getRole());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create a new note", description = "Creates a new note owned by the authenticated user. Content is sanitized to prevent XSS.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Note created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "503", description = "Database unavailable", content = @Content)
    })
    @PostMapping("/notes")
    public ResponseEntity<Note> createNote(
            @Valid @RequestBody CreateNoteRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Note savedNote = noteService.createNote(request, principal.getUsername());
        return new ResponseEntity<>(savedNote, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a note", description = "Replaces all fields of an existing note. Users can only update their own notes. Tags are fully replaced, not merged.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or invalid ID format", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Note not found", content = @Content),
            @ApiResponse(responseCode = "503", description = "Database unavailable", content = @Content)
    })
    @PutMapping("/notes/{id}")
    public ResponseEntity<Note> updateNote(
            @Parameter(description = "ID of the note to update") @PathVariable Long id,
            @Valid @RequestBody UpdateNoteRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Note updatedNote = noteService.updateNote(id, request, principal.getUsername(), principal.user().getRole());
        return ResponseEntity.ok(updatedNote);
    }
}
