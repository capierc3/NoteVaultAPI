package org.chase.pierce.notevaultapi.service;

import org.chase.pierce.notevaultapi.dto.CreateNoteRequest;
import org.chase.pierce.notevaultapi.entity.Note;
import org.chase.pierce.notevaultapi.entity.Tag;
import org.chase.pierce.notevaultapi.repository.NoteRepository;
import org.chase.pierce.notevaultapi.repository.TagRepository;
import org.chase.pierce.notevaultapi.util.InputSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final TagRepository tagRepository;

    public NoteService(NoteRepository noteRepository, TagRepository tagRepository) {
        this.noteRepository = noteRepository;
        this.tagRepository = tagRepository;
    }

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    @Transactional
    public Note createNote(CreateNoteRequest request) {
        Note note = new Note();
        note.setName(InputSanitizer.sanitizePlainText(request.getName()));
        note.setContent(InputSanitizer.sanitizeContent(request.getContent()));
        note.setUserId(InputSanitizer.sanitizePlainText(request.getUserId()));

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : request.getTags()) {
                String sanitizedName = InputSanitizer.sanitizePlainText(tagName);
                if (sanitizedName != null) {
                    Tag tag = tagRepository.findByName(sanitizedName)
                            .orElseGet(() -> {
                                Tag newTag = new Tag();
                                newTag.setName(sanitizedName);
                                return tagRepository.save(newTag);
                            });
                    tags.add(tag);
                }
            }
            note.setTags(tags);
        }

        return noteRepository.save(note);
    }
}
