package com.example.javabackend.service.impl;

import com.example.javabackend.model.User;
import com.example.javabackend.repository.UserRepository;
import com.example.javabackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl extends BaseServiceImpl<User, Long, UserRepository> implements UserService {
    
    public UserServiceImpl(UserRepository repository) {
        super(repository);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
} 