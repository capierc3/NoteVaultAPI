package org.chase.pierce.notevaultapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.chase.pierce.notevaultapi.security.DefaultUserFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final DefaultUserFilter defaultUserFilter;

    public SecurityConfig(DefaultUserFilter defaultUserFilter) {
        this.defaultUserFilter = defaultUserFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(basic -> basic
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        Map<String, Object> body = new HashMap<>();
                        body.put("timestamp", LocalDateTime.now().toString());
                        body.put("status", 401);
                        body.put("error", "Unauthorized");
                        body.put("message", "Invalid credentials");
                        new ObjectMapper().writeValue(response.getOutputStream(), body);
                    }))
            .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll()
            )
            .exceptionHandling(ex -> ex
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        Map<String, Object> body = new HashMap<>();
                        body.put("timestamp", LocalDateTime.now().toString());
                        body.put("status", 403);
                        body.put("error", "Forbidden");
                        body.put("message", "Access denied");
                        new ObjectMapper().writeValue(response.getOutputStream(), body);
                    }))
            .addFilterAfter(defaultUserFilter, BasicAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
