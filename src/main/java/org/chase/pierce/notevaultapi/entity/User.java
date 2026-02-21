package org.chase.pierce.notevaultapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users", schema = "auth")
@Schema(description = "A registered user account")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the user", example = "1")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @Schema(description = "Username for authentication", example = "johndoe")
    private String username;

    @Column(nullable = false)
    @JsonIgnore
    @Schema(hidden = true)
    private String password;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Schema(description = "User role", example = "USER")
    private Role role;

    @Column(nullable = false)
    @Schema(description = "Whether the account is enabled", example = "true")
    private boolean enabled = true;
}
