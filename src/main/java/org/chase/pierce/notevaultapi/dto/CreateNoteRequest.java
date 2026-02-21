package org.chase.pierce.notevaultapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Schema(description = "Request body for creating a new note")
public class CreateNoteRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be 255 characters or fewer")
    @Schema(description = "Name of the note", example = "Meeting Notes", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "Content is required")
    @Schema(description = "Content of the note (safe HTML allowed)", example = "<p>Discussion points from today's meeting</p>", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "ID of the notebook this note belongs to", example = "1")
    private Long notebookId;

    @Schema(description = "Tags to associate with the note", example = "[\"work\", \"important\"]")
    private Set<String> tags;
}
