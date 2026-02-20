package org.chase.pierce.notevaultapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "tags", schema = "ud")
@Schema(description = "A tag that can be applied to notes for categorization")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the tag", example = "1")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Name of the tag", example = "meeting")
    private String name;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    @JsonIgnore
    @Schema(hidden = true)
    private Set<Note> notes = new HashSet<>();
}
