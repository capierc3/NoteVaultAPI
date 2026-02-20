package org.chase.pierce.notevaultapi.controller;

import jakarta.validation.Valid;
import org.chase.pierce.notevaultapi.dto.CreateNoteRequest;
import org.chase.pierce.notevaultapi.entity.Note;
import org.chase.pierce.notevaultapi.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/notes")
    public ResponseEntity<List<Note>> getAllNotes() {
        List<Note> notes = noteService.getAllNotes();
        return ResponseEntity.ok(notes);
    }

    @PostMapping("/notes")
    public ResponseEntity<Note> createNote(@Valid @RequestBody CreateNoteRequest request) {
        Note savedNote = noteService.createNote(request);
        return new ResponseEntity<>(savedNote, HttpStatus.CREATED);
    }
}
