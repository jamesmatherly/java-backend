package com.example.javabackend.repository;

import com.example.javabackend.model.LearningJourney;
import com.example.javabackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningJourneyRepository extends JpaRepository<LearningJourney, String> {
    List<LearningJourney> findByUser(User user);
    List<LearningJourney> findByUserAndPrimaryTrue(User user);
} 