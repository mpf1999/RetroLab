package uoc.edu.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET_KEY =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    private static final long EXPIRATION = 3_600_000L;

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET_KEY, EXPIRATION);

        userDetails = User.builder()
                .username("user@test.com")
                .password("password")
                .roles("USER")
                .build();
    }

    @Test
    void generateTokenShouldReturnToken() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractUsernameShouldReturnTokenSubject() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals("user@test.com", username);
    }

    @Test
    void extractExpirationShouldReturnFutureDate() {
        String token = jwtService.generateToken(userDetails);

        var expirationDate = jwtService.extractExpiration(token);

        assertNotNull(expirationDate);
        assertTrue(expirationDate.getTime() > System.currentTimeMillis());
    }

    @Test
    void isTokenValidShouldReturnTrueForCorrectUser() {
        String token = jwtService.generateToken(userDetails);

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertTrue(valid);
    }

    @Test
    void isTokenValidShouldReturnFalseForDifferentUser() {
        String token = jwtService.generateToken(userDetails);

        UserDetails differentUser = User.builder()
                .username("different@test.com")
                .password("password")
                .roles("USER")
                .build();

        boolean valid = jwtService.isTokenValid(token, differentUser);

        assertFalse(valid);
    }

    @Test
    void getExpirationTimeShouldReturnConfiguredExpiration() {
        long expiration = jwtService.getExpirationTime();

        assertEquals(EXPIRATION, expiration);
    }

    @Test
    void expiredTokenShouldThrowExpiredJwtException() throws InterruptedException {
        JwtService shortExpirationJwtService =
                new JwtService(SECRET_KEY, 1L);

        String token =
                shortExpirationJwtService.generateToken(userDetails);

        Thread.sleep(10);

        assertThrows(
                ExpiredJwtException.class,
                () -> shortExpirationJwtService.extractUsername(token)
        );
    }

    @Test
    void invalidTokenShouldThrowException() {
        String invalidToken =
                "eyJhbGciOiJIUzI1NiJ9.invalid.signature";

        assertThrows(
                Exception.class,
                () -> jwtService.extractUsername(invalidToken)
        );
    }
}