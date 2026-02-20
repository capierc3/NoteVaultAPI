package org.chase.pierce.notevaultapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "note", schema = "ud")
@Schema(description = "A note containing text content that can be tagged and organized into notebooks")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the note", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Name of the note", example = "Meeting Notes")
    private String name;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Content of the note (may contain safe HTML)", example = "<p>Discussion points from today's meeting</p>")
    private String content;

    @Column(name = "user_id", nullable = false)
    @Schema(description = "ID of the user who last modified the note", example = "user123")
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notebook_id")
    @JsonIgnoreProperties({"notes"})
    @Schema(description = "Notebook this note belongs to")
    private Notebook notebook;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "note_tags",
            schema = "ud",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonIgnoreProperties({"notes"})
    @Schema(description = "Tags associated with this note")
    private Set<Tag> tags = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Timestamp when the note was created", example = "2025-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    @Schema(description = "Timestamp when the note was last modified", example = "2025-01-15T14:45:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime modifiedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        modifiedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedAt = LocalDateTime.now();
    }
}
