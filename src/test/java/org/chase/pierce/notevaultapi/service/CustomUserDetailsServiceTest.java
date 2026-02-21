package org.chase.pierce.notevaultapi.service;

import org.chase.pierce.notevaultapi.entity.Role;
import org.chase.pierce.notevaultapi.entity.User;
import org.chase.pierce.notevaultapi.repository.UserRepository;
import org.chase.pierce.notevaultapi.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void testLoadsExistingUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("hashed");
        user.setRole(Role.USER);
        user.setEnabled(true);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

        assertInstanceOf(UserPrincipal.class, result);
        assertEquals("testuser", result.getUsername());
        assertEquals("hashed", result.getPassword());
        assertTrue(result.isEnabled());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testThrowsWhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("unknown")
        );

        verify(userRepository).findByUsername("unknown");
    }

    @Test
    void testAdminUserHasAdminRole() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("hashed");
        user.setRole(Role.ADMIN);
        user.setEnabled(true);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("admin");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}
