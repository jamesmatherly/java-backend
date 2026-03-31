package com.example.javabackend.security;

import com.example.javabackend.model.User;
import com.example.javabackend.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CognitoUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CognitoUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Spring's UserDetailsService interface requires this name; the parameter is the Cognito sub (user ID), not a username.
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String cognitoId) throws UsernameNotFoundException {
        return userRepository.findById(cognitoId)
                .map(this::createUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + cognitoId));
    }

    @Transactional
    public void ensureUser(String cognitoId, String email) {
        if (!userRepository.existsById(cognitoId)) {
            User user = new User();
            user.setId(cognitoId);
            user.setUsername(email);
            user.setEmail(email);
            userRepository.saveAndFlush(user);
        }
    }

    private UserDetails createUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password("") // No password needed as we're using Cognito
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))) // Default role
                .build();
    }
} 