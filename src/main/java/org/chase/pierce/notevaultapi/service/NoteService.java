package org.chase.pierce.notevaultapi.service;

import org.chase.pierce.notevaultapi.dto.CreateNoteRequest;
import org.chase.pierce.notevaultapi.dto.UpdateNoteRequest;
import org.chase.pierce.notevaultapi.entity.Note;
import org.chase.pierce.notevaultapi.entity.Tag;
import org.chase.pierce.notevaultapi.exception.NoteNotFoundException;
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

    public List<Note> getNotesByFilters(Set<String> tags, String userId, Long notebookId) {
        boolean hasTags = tags != null && !tags.isEmpty();
        boolean hasUser = userId != null && !userId.isBlank();
        boolean hasNotebook = notebookId != null;

        if (hasTags && hasUser && hasNotebook) {
            return noteRepository.findByTagNamesAndUserIdAndNotebookId(tags, userId, notebookId);
        } else if (hasTags && hasUser) {
            return noteRepository.findByTagNamesAndUserId(tags, userId);
        } else if (hasTags && hasNotebook) {
            return noteRepository.findByTagNamesAndNotebookId(tags, notebookId);
        } else if (hasUser && hasNotebook) {
            return noteRepository.findByUserIdAndNotebookId(userId, notebookId);
        } else if (hasTags) {
            return noteRepository.findByTagNames(tags);
        } else if (hasUser) {
            return noteRepository.findByUserId(userId);
        } else if (hasNotebook) {
            return noteRepository.findByNotebookId(notebookId);
        } else {
            return noteRepository.findAll();
        }
    }

    public Note getNoteById(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));
    }

    @Transactional
    public void deleteNoteById(Long id) {
        if (!noteRepository.existsById(id)) {
            throw new NoteNotFoundException(id);
        }
        noteRepository.deleteById(id);
    }

    @Transactional
    public Note createNote(CreateNoteRequest request) {
        Note note = new Note();
        note.setName(InputSanitizer.sanitizePlainText(request.getName()));
        note.setContent(InputSanitizer.sanitizeContent(request.getContent()));
        note.setUserId(InputSanitizer.sanitizePlainText(request.getUserId()));
        note.setTags(resolveTags(request.getTags()));

        return noteRepository.save(note);
    }

    @Transactional
    public Note updateNote(Long id, UpdateNoteRequest request) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));

        note.setName(InputSanitizer.sanitizePlainText(request.getName()));
        note.setContent(InputSanitizer.sanitizeContent(request.getContent()));
        note.setUserId(InputSanitizer.sanitizePlainText(request.getUserId()));
        note.setTags(resolveTags(request.getTags()));

        return noteRepository.save(note);
    }

    private Set<Tag> resolveTags(Set<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        if (tagNames != null && !tagNames.isEmpty()) {
            for (String tagName : tagNames) {
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
        }
        return tags;
    }
}
