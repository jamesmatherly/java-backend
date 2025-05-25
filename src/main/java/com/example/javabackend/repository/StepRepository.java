package com.example.javabackend.repository;

import com.example.javabackend.model.Step;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StepRepository extends JpaRepository<Step, String> {
    List<Step> findByJourneyId(String journeyId);
} 