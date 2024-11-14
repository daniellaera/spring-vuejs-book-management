package com.daniellaera.backend.service.impl;

import com.daniellaera.backend.model.User;
import com.daniellaera.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        Optional<User> userOptional = userRepository.findByEmail(username);
        if (userOptional.isEmpty()) {
            log.error("Username not found: " + username);
            throw new UsernameNotFoundException("could not found user..!!");
        }
        return userOptional.get();
		/*
		User user = userOptional.get();
    Set<GrantedAuthority> authorities = new HashSet<>();
    // Add user role as authority
    authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

    // Check if the user has the required role
    if (!authorities.contains(new SimpleGrantedAuthority("ROLE_USER"))) {
        throw new UsernameNotFoundException("User does not have the required role");
    }

    return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            authorities
    );
		 */
    }
}
