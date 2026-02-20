package org.chase.pierce.notevaultapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class CreateNoteRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be 255 characters or fewer")
    private String name;

    @NotBlank(message = "Content is required")
    private String content;

    @NotBlank(message = "User ID is required")
    private String userId;

    private Long notebookId;

    private Set<String> tags;
}
