package com.example.javabackend.service.impl;

import com.example.javabackend.model.LearningJourney;
import com.example.javabackend.model.User;
import com.example.javabackend.repository.LearningJourneyRepository;
import com.example.javabackend.service.LearningJourneyService;
import com.example.javabackend.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class LearningJourneyServiceImpl implements LearningJourneyService {
    
    private final LearningJourneyRepository repository;
    private final AuthService authService;

    public LearningJourneyServiceImpl(LearningJourneyRepository repository, AuthService authService) {
        this.repository = repository;
        this.authService = authService;
    }

    @Override
    public List<LearningJourney> getAllJourneys() {
        User currentUser = authService.getCurrentUser();
        return repository.findByUser(currentUser);
    }

    @Override
    public LearningJourney getJourney(String journeyId) {
        User currentUser = authService.getCurrentUser();
        return repository.findById(journeyId)
            .filter(journey -> journey.getUser().getId().equals(currentUser.getId()))
            .orElseThrow(() -> new RuntimeException("Journey not found or access denied"));
    }

    @Override
    public LearningJourney createJourney(LearningJourney journey) {
        User currentUser = authService.getCurrentUser();
        journey.setUser(currentUser);
        return repository.save(journey);
    }

    @Override
    public LearningJourney updateJourney(String journeyId, LearningJourney journey) {
        User currentUser = authService.getCurrentUser();
        LearningJourney existingJourney = repository.findById(journeyId)
            .filter(j -> j.getUser().getId().equals(currentUser.getId()))
            .orElseThrow(() -> new RuntimeException("Journey not found or access denied"));
            
        existingJourney.setTitle(journey.getTitle());
        existingJourney.setPrimary(journey.isPrimary());
        return repository.save(existingJourney);
    }

    @Override
    public void deleteJourney(String journeyId) {
        User currentUser = authService.getCurrentUser();
        LearningJourney journey = repository.findById(journeyId)
            .filter(j -> j.getUser().getId().equals(currentUser.getId()))
            .orElseThrow(() -> new RuntimeException("Journey not found or access denied"));
        repository.delete(journey);
    }

    @Override
    public LearningJourney setJourneyAsPrimary(String journeyId) {
        User currentUser = authService.getCurrentUser();
        List<LearningJourney> allJourneys = repository.findByUser(currentUser);
        allJourneys.forEach(journey -> journey.setPrimary(false));
        
        LearningJourney primaryJourney = repository.findById(journeyId)
            .filter(j -> j.getUser().getId().equals(currentUser.getId()))
            .orElseThrow(() -> new RuntimeException("Journey not found or access denied"));
            
        primaryJourney.setPrimary(true);
        
        repository.saveAll(allJourneys);
        return repository.save(primaryJourney);
    }
} 