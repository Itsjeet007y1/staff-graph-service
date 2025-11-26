package com.staff.employee.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        log.debug("JwtAuthenticationFilter: request URI='{}'", path);
        // Skip JWT processing for the H2 console, static resources and auth endpoints
        if (path.startsWith("/h2-console") || path.startsWith("/graphiql") || path.startsWith("/swagger-ui") || path.startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.debug("JwtAuthenticationFilter: Authorization header present? {}", header != null && !header.isBlank());
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            log.debug("JwtAuthenticationFilter: Authorization header present");
            String token = header.substring(7);
            try {
                var claims = jwtUtil.parseToken(token);
                String username = claims.getSubject();
                log.debug("JwtAuthenticationFilter: parsed token for username='{}'", username);

                // Safely extract roles from token claims. The claim may be a List<?> or a String.
                Object rolesObj = claims.get("roles");
                List<String> roles = new ArrayList<>();
                if (rolesObj instanceof List<?>) {
                    for (Object o : (List<?>) rolesObj) {
                        if (o != null) {
                            roles.add(o.toString());
                        }
                    }
                } else if (rolesObj instanceof String) {
                    String s = (String) rolesObj;
                    if (!s.isBlank()) {
                        for (String part : s.split(",")) {
                            String p = part.trim();
                            if (!p.isEmpty()) roles.add(p);
                        }
                    }
                }
                log.debug("JwtAuthenticationFilter: roles extracted={} for username='{}'", roles, username);

                List<SimpleGrantedAuthority> authorities = roles.isEmpty()
                        ? Collections.emptyList()
                        : roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

                var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ex) {
                log.warn("Failed to parse JWT: {}", ex.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
