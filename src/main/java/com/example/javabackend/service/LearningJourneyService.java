package com.example.javabackend.service;

import com.example.javabackend.model.LearningJourney;
import com.example.javabackend.model.Step;
import java.util.List;

public interface LearningJourneyService {
    List<LearningJourney> getAllJourneys();
    LearningJourney getJourney(String journeyId);
    LearningJourney createJourney(LearningJourney journey);
    LearningJourney updateJourney(String journeyId, LearningJourney journey);
    void deleteJourney(String journeyId);
    LearningJourney setJourneyAsPrimary(String journeyId);
    LearningJourney addStep(Step step, String journeyId);
} 