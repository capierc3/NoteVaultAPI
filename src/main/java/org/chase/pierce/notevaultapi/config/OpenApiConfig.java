package org.chase.pierce.notevaultapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI noteVaultOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NoteVault API")
                        .description("REST API for managing notes, notebooks, and tags. "
                                + "Authenticate via HTTP Basic Auth or use the default shared user.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Chase Pierce")))
                .addSecurityItem(new SecurityRequirement().addList("Basic Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Basic Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")));
    }
}
