package uoc.edu.unit_tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import uoc.edu.model.Role;
import uoc.edu.model.User;
import uoc.edu.repository.UserRepository;
import uoc.edu.security.PrivUserDetailsService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PrivUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PrivUserDetailsService userDetailsService;

    @Test
    void loadUserByUsernameReturnsUserDetails() {
        User user = new User();
        user.setEmail("rayquaza2004@test.com");
        user.setName("rayquaza2004");
        user.setPasswordHash("ABCDEeE12.");
        user.setRole(Role.ADMIN);

        when(userRepository.findByEmailIgnoreCase("rayquaza2004@test.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("rayquaza2004@test.com");

        assertNotNull(result);
        assertEquals("rayquaza2004@test.com", result.getUsername());
        assertEquals("ABCDEeE12.", result.getPassword());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        verify(userRepository).findByEmailIgnoreCase("rayquaza2004@test.com");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUserByUsernameReturnsUserRole() {
        User user = new User();
        user.setName("pikachu");
        user.setEmail("pikachu@test.com");
        user.setPasswordHash("Password123.");
        user.setRole(Role.USER);

        when(userRepository.findByEmailIgnoreCase("pikachu@test.com"))
                .thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("pikachu@test.com");

        assertEquals("ROLE_USER", result.getAuthorities().iterator().next().getAuthority());

        verify(userRepository).findByEmailIgnoreCase("pikachu@test.com");
    }

    @Test
    void loadUserByUsernameThrowsExceptionWhenUserDoesNotExist() {

        when(userRepository.findByEmailIgnoreCase("missing@test.com"))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing@test.com")
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository)
                .findByEmailIgnoreCase("missing@test.com");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUserByUsernameReturnsCorrectPassword() {

        User user = new User();
        user.setName("charizard");
        user.setEmail("charizard@test.com");
        user.setPasswordHash("MyEncodedPassword");
        user.setRole(Role.USER);

        when(userRepository.findByEmailIgnoreCase("charizard@test.com"))
                .thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("charizard@test.com");

        assertEquals("MyEncodedPassword", result.getPassword());

        verify(userRepository).findByEmailIgnoreCase("charizard@test.com");
    }

    @Test
    void loadUserByUsernameUsesRepositoryIgnoringCase() {

        User user = new User();
        user.setName("Mew");
        user.setEmail("mew@test.com");
        user.setPasswordHash("password");
        user.setRole(Role.ADMIN);

        when(userRepository.findByEmailIgnoreCase("MEW@TEST.COM")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("MEW@TEST.COM");

        assertEquals("mew@test.com", result.getUsername());

        verify(userRepository).findByEmailIgnoreCase("MEW@TEST.COM");
    }


}
