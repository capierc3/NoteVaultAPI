package org.chase.pierce.notevaultapi.repository;

import org.chase.pierce.notevaultapi.entity.Note;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    @EntityGraph(attributePaths = {"tags", "notebook"})
    List<Note> findAll();

    @EntityGraph(attributePaths = {"tags", "notebook"})
    Optional<Note> findById(Long id);

    @EntityGraph(attributePaths = {"tags", "notebook"})
    List<Note> findByUserId(String userId);

    @EntityGraph(attributePaths = {"tags", "notebook"})
    List<Note> findByNotebookId(Long notebookId);

    @EntityGraph(attributePaths = {"tags", "notebook"})
    @Query("SELECT DISTINCT n FROM Note n JOIN n.tags t WHERE t.name IN :tagNames")
    List<Note> findByTagNames(@Param("tagNames") Set<String> tagNames);

    @EntityGraph(attributePaths = {"tags", "notebook"})
    @Query("SELECT DISTINCT n FROM Note n JOIN n.tags t WHERE t.name IN :tagNames AND n.userId = :userId")
    List<Note> findByTagNamesAndUserId(@Param("tagNames") Set<String> tagNames, @Param("userId") String userId);

    @EntityGraph(attributePaths = {"tags", "notebook"})
    @Query("SELECT DISTINCT n FROM Note n JOIN n.tags t WHERE t.name IN :tagNames AND n.notebook.id = :notebookId")
    List<Note> findByTagNamesAndNotebookId(@Param("tagNames") Set<String> tagNames, @Param("notebookId") Long notebookId);

    @EntityGraph(attributePaths = {"tags", "notebook"})
    List<Note> findByUserIdAndNotebookId(String userId, Long notebookId);

    @EntityGraph(attributePaths = {"tags", "notebook"})
    @Query("SELECT DISTINCT n FROM Note n JOIN n.tags t WHERE t.name IN :tagNames AND n.userId = :userId AND n.notebook.id = :notebookId")
    List<Note> findByTagNamesAndUserIdAndNotebookId(@Param("tagNames") Set<String> tagNames, @Param("userId") String userId, @Param("notebookId") Long notebookId);
}
