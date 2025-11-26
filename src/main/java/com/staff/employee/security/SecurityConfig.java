package com.staff.employee.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // Read allowed origins from application properties; default to the deployed frontend
    // Use SpEL to split comma-separated values into a String[] safely
    @Value("#{'${app.cors.allowed-origins:https://staff-portal-ui-ez7c.vercel.app}'.split(',')}")
    private String[] allowedOrigins;

    public SecurityConfig(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Provide bcrypt for new encodings and noop for legacy/non-prefixed stored passwords
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        encoders.put("noop", NoOpPasswordEncoder.getInstance());
        DelegatingPasswordEncoder delegating = new DelegatingPasswordEncoder("bcrypt", encoders);
        // Treat non-prefixed stored passwords as noop (matches raw password directly)
        delegating.setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder.getInstance());
        return delegating;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtil);

        http
                // Enable CORS and provide configuration via CorsConfigurationSource bean
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Disable CSRF entirely for this stateless JWT API
                .csrf(csrf -> csrf.disable())
                // Return 401 Unauthorized for unauthenticated requests instead of default 403
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests((authz) -> authz
                        // Allow framework error endpoint so malformed requests don't return 403
                        .requestMatchers(new AntPathRequestMatcher("/error")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/auth/login")).permitAll()
                        // Allow OpenAPI / Swagger UI
                        .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**"), new AntPathRequestMatcher("/swagger-ui/**"), new AntPathRequestMatcher("/swagger-ui.html"), new AntPathRequestMatcher("/swagger-ui/index.html")).permitAll()
                        // Allow H2 Console (use AntPathRequestMatcher to avoid ambiguity when multiple servlets exist)
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                        // Allow HTTP access to /graphql and enforce authorization at method level (@PreAuthorize in resolvers)
                        .requestMatchers(new AntPathRequestMatcher("/graphql")).permitAll()
                        .anyRequest().permitAll()
                )
                .sessionManagement((s) -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Allow frames from same origin so H2 console loads
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                // Apply JWT filter but ensure it ignores H2 console by checking path inside the filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        ;
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Allow the frontend origins from configuration. Use Arrays.asList to convert the array.
        config.setAllowedOrigins(Arrays.asList(allowedOrigins));
        // Standard methods including OPTIONS for preflight
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        // Allow common headers from the client (Authorization will be included)
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        // Allow credentials (cookies, authorization headers). Set to false if not needed.
        config.setAllowCredentials(true);
        // Expose Authorization header if clients need to read it from responses
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        // Use the shared PasswordEncoder bean (delegating) so both prefixed and non-prefixed stored passwords are supported
        p.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(p);
    }
}
