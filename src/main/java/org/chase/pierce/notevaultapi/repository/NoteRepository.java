package org.chase.pierce.notevaultapi.repository;

import org.chase.pierce.notevaultapi.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
}
