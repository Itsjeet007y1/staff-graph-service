package com.staff.employee.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class InMemoryUserDetailsService implements UserDetailsService {

    // For demo purposes only. In production, use a persistent user store.
    private final Map<String, UserDetails> users = Map.of(
            "admin", User.withUsername("admin").password("{noop}adminpass").roles("ADMIN").build(),
            "employee", User.withUsername("employee").password("{noop}emppass").roles("EMPLOYEE").build()
    );

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var u = users.get(username);
        if (u == null) {
            log.debug("loadUserByUsername: user not found -> {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }
        log.debug("loadUserByUsername: found user='{}', password='{}'", username, u.getPassword());
        return u;
    }
}
