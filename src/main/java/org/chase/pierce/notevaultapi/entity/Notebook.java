package org.chase.pierce.notevaultapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "notebook", schema = "ud")
@Schema(description = "A notebook that groups related notes together")
public class Notebook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the notebook", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Name of the notebook", example = "Work Notes")
    private String name;

    @Column(name = "user_id", nullable = false)
    @Schema(description = "ID of the user who owns the notebook", example = "user123")
    private String userId;

    @OneToMany(mappedBy = "notebook", fetch = FetchType.LAZY)
    @JsonIgnore
    @Schema(hidden = true)
    private List<Note> notes = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Timestamp when the notebook was created", example = "2025-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    @Schema(description = "Timestamp when the notebook was last modified", example = "2025-01-15T14:45:00", accessMode = Schema.AccessMode.READ_ONLY)
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
