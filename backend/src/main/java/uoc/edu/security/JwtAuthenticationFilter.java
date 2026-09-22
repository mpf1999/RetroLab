package uoc.edu.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final PrivUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            PrivUserDetailsService userDetailsService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    // executed for every request, checks if it contains a valid JWT and if valid, authenticates the user
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        // if there is no bearer, continue filter chain without authenticating
        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        //Remove bearer to only get jwt
        String token = authorizationHeader.substring(7);

        try {
            String email = jwtService.extractUsername(token);

            // continue only if there is user and no authentication
            if (email != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                //load user
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);

                // validate token agaiunst user, then create Spring Security object containing it and its authorities
                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            }
            // invalid JWT are ignored
        } catch (JwtException | IllegalArgumentException ignored) {
        }
        // continue processing the request
        filterChain.doFilter(request, response);
    }
}