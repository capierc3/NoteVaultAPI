package org.chase.pierce.notevaultapi.repository;

import org.chase.pierce.notevaultapi.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUserId(String userId);

    List<Note> findByNotebookId(Long notebookId);

    @Query("SELECT DISTINCT n FROM Note n JOIN n.tags t WHERE t.name IN :tagNames")
    List<Note> findByTagNames(@Param("tagNames") Set<String> tagNames);

    @Query("SELECT DISTINCT n FROM Note n JOIN n.tags t WHERE t.name IN :tagNames AND n.userId = :userId")
    List<Note> findByTagNamesAndUserId(@Param("tagNames") Set<String> tagNames, @Param("userId") String userId);

    @Query("SELECT DISTINCT n FROM Note n JOIN n.tags t WHERE t.name IN :tagNames AND n.notebook.id = :notebookId")
    List<Note> findByTagNamesAndNotebookId(@Param("tagNames") Set<String> tagNames, @Param("notebookId") Long notebookId);

    List<Note> findByUserIdAndNotebookId(String userId, Long notebookId);

    @Query("SELECT DISTINCT n FROM Note n JOIN n.tags t WHERE t.name IN :tagNames AND n.userId = :userId AND n.notebook.id = :notebookId")
    List<Note> findByTagNamesAndUserIdAndNotebookId(@Param("tagNames") Set<String> tagNames, @Param("userId") String userId, @Param("notebookId") Long notebookId);
}
