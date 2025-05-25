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
    private final JwtTokenValidator tokenValidator;

    public CognitoUserDetailsService(UserRepository userRepository, JwtTokenValidator tokenValidator) {
        this.userRepository = userRepository;
        this.tokenValidator = tokenValidator;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> createNewUser(username));
        
        return createUserDetails(user);
    }

    @Transactional
    public UserDetails createUserDetails(String username, List<String> groups, String token) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> createNewUser(username, token));
            
            return createUserDetails(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user details: " + e.getMessage(), e);
        }
    }

    @Transactional
    private User createNewUser(String username, String token) {
        try {
            String email = tokenValidator.getEmailFromToken(token);
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            return userRepository.saveAndFlush(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }

    @Transactional
    private User createNewUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username);
        return userRepository.saveAndFlush(user);
    }

    private UserDetails createUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password("") // No password needed as we're using Cognito
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))) // Default role
                .build();
    }
} 