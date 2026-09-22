package uoc.edu.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import uoc.edu.security.JwtAuthenticationFilter;

import java.util.List;

//security and CORS configuration
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    //RetroLab does Spring Security --> CORS --> JWT --> Auth --> Controller so we need all the pieces
    //three dependencies, JwtAuthenticationFilter to read and validate JWTs
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    // UserDetailService to load users
    private final UserDetailsService userDetailsService;
    // compare passwords through Bcrypt
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    // We define what to do with each http request
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider
    ){

        //use the corsConfigurer to decide which cross-origin requests are allowed
        return http
                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )
                // disable csrf as our API is stateless
                .csrf(
                        AbstractHttpConfigurer::disable
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        // Login and all authentication, swagger
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // the <img> elements do not have the JWT. Only make public the reading of images.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/uploads/**"
                        )
                        .permitAll()

                        // allows preflight requests by the browser, we use OPTIONS to ask which methods and headers are
                        // allowed for the resource in corsConfigurationSource
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        )
                        .permitAll()

                        // Rest of the API requires to be authenticated
                        .anyRequest()
                        .authenticated()
                )

                .authenticationProvider(
                        authenticationProvider
                )
                // handle 401 and 403
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        (
                                                request,
                                                response,
                                                authException
                                        ) ->
                                                response.sendError(
                                                        HttpServletResponse
                                                                .SC_UNAUTHORIZED,
                                                        "Unauthorized"
                                                )
                                )

                                .accessDeniedHandler(
                                        (
                                                request,
                                                response,
                                                accessDeniedException
                                        ) ->
                                                response.sendError(
                                                        HttpServletResponse
                                                                .SC_FORBIDDEN,
                                                        "Forbidden"
                                                )
                                )
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }

    @Bean
    public CorsConfigurationSource
    corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        // Accepts all vite ports
        configuration.setAllowedOriginPatterns(
                List.of(
                        "http://localhost:*",
                        "http://127.0.0.1:*"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );
        // All authorized headers, and the only one that is exposed
        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept"
                )
        );

        configuration.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );

        // register CORS for all routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    // how credentials will be passed through login
    @Bean
    public AuthenticationProvider authenticationProvider() {

        //Spring DaoAuthenticationProvider, mail + password and looks for users
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );
        //encode using this encoder
        provider.setPasswordEncoder(
                passwordEncoder
        );

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }
}