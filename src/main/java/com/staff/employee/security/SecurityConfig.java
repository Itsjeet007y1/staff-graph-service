package com.staff.employee.security;

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

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

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
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        // Use the shared PasswordEncoder bean (delegating) so both prefixed and non-prefixed stored passwords are supported
        p.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(p);
    }
}
