package com.staff.employee.controller;

import com.staff.employee.security.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserDetailsService userDetailsService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        log.debug("Attempting login for username='{}'", loginRequest.getUsername());
        try {
            UserDetails user = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            String presented = loginRequest.getPassword();
            if (!passwordEncoder.matches(presented, user.getPassword())) {
                log.debug("Password mismatch for user='{}' (presented='{}')", loginRequest.getUsername(), presented);
                throw new BadCredentialsException("Bad credentials");
            }
            // Build authentication and set into context for this request
            var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            var roles = user.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.toList());
            String token = jwtUtil.generateToken(user.getUsername(), roles);
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (BadCredentialsException ex) {
            log.debug("Authentication failed for user='{}': {}", loginRequest.getUsername(), ex.getMessage());
            try {
                UserDetails stored = userDetailsService.loadUserByUsername(loginRequest.getUsername());
                log.debug("Stored user password (debug) for '{}': {}", loginRequest.getUsername(), stored.getPassword());
            } catch (Exception e) {
                log.debug("Could not load stored user for '{}': {}", loginRequest.getUsername(), e.getMessage());
            }
            throw ex;
        }
    }

    @Data
    static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    @AllArgsConstructor
    public static class LoginResponse {
        private String token;
    }
}
