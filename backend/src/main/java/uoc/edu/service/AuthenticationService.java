package uoc.edu.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import uoc.edu.security.JwtService;
import uoc.edu.security.PrivUserDetailsService;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final PrivUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            PrivUserDetailsService userDetailsService,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    public String login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);

        return jwtService.generateToken(userDetails);
    }
}