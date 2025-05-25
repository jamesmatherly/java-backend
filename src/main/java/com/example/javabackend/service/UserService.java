package com.example.javabackend.service;

import com.example.javabackend.model.User;
import java.util.Optional;

public interface UserService extends BaseService<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
} 