package uoc.edu.integration_tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import uoc.edu.model.User;
import uoc.edu.repository.UserRepository;
import uoc.edu.security.PrivUserDetailsService;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class PrivUserDetailsServiceIntegrationTest {

    @Autowired
    private PrivUserDetailsService privUserDetailsService;

    @Autowired
    private UserRepository userRepository;

    private User technician;
    private User admin;

    @BeforeEach
    void setUp() {
        technician = userRepository
                .findByEmailIgnoreCase(
                        "technician.one@retrolab.test"
                )
                .orElseThrow(() -> new IllegalStateException(
                        "TestDataInitializer did not create technician.one@retrolab.test"
                ));

        admin = userRepository
                .findByEmailIgnoreCase(
                        "admin@retrolab.test"
                )
                .orElseThrow(() -> new IllegalStateException(
                        "TestDataInitializer did not create admin@retrolab.test"
                ));
    }

    @Test
    void loadUserByUsernameReturnsCorrectUserDetails() {
        UserDetails userDetails =
                privUserDetailsService.loadUserByUsername(
                        technician.getEmail()
                );

        assertAll(
                () -> assertEquals(
                        technician.getEmail(),
                        userDetails.getUsername()
                ),
                () -> assertEquals(
                        technician.getPasswordHash(),
                        userDetails.getPassword()
                ),
                () -> assertTrue(
                        userDetails.getAuthorities().contains(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                ),
                () -> assertTrue(userDetails.isAccountNonExpired()),
                () -> assertTrue(userDetails.isAccountNonLocked()),
                () -> assertTrue(userDetails.isCredentialsNonExpired()),
                () -> assertTrue(userDetails.isEnabled())
        );
    }

    @Test
    void loadUserByUsernameIsCaseInsensitive() {
        UserDetails userDetails =
                privUserDetailsService.loadUserByUsername(
                        "TECHNICIAN.ONE@RETROLAB.TEST"
                );

        assertEquals(
                "technician.one@retrolab.test",
                userDetails.getUsername()
        );
    }

    @Test
    void loadUserByUsernameReturnsAdminAuthority() {
        UserDetails userDetails =
                privUserDetailsService.loadUserByUsername(
                        admin.getEmail()
                );

        assertAll(
                () -> assertEquals(
                        "admin@retrolab.test",
                        userDetails.getUsername()
                ),
                () -> assertTrue(
                        userDetails.getAuthorities().contains(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                )
        );
    }

    @Test
    void loadUserByUsernameThrowsExceptionWhenUserDoesNotExist() {
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> privUserDetailsService.loadUserByUsername(
                        "notexist@test.com"
                )
        );

        assertEquals(
                "User not found",
                exception.getMessage()
        );
    }
}