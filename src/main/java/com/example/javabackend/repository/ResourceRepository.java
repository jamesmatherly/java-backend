package com.example.javabackend.repository;

import com.example.javabackend.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, String> {
    List<Resource> findByStepId(String stepId);
} 