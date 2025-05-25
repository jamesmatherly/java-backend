package com.example.javabackend.controller;

import com.example.javabackend.model.LearningJourney;
import com.example.javabackend.service.LearningJourneyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journeys")
public class LearningJourneyController {

    private final LearningJourneyService journeyService;

    public LearningJourneyController(LearningJourneyService journeyService) {
        this.journeyService = journeyService;
    }

    @GetMapping
    public ResponseEntity<List<LearningJourney>> getAllJourneys() {
        return ResponseEntity.ok(journeyService.getAllJourneys());
    }

    @GetMapping("/{journeyId}")
    public ResponseEntity<LearningJourney> getJourney(@PathVariable String journeyId) {
        return ResponseEntity.ok(journeyService.getJourney(journeyId));
    }

    @PostMapping
    public ResponseEntity<LearningJourney> createJourney(@RequestBody LearningJourney journey) {
        return ResponseEntity.ok(journeyService.createJourney(journey));
    }

    @PutMapping("/{journeyId}")
    public ResponseEntity< LearningJourney> updateJourney(
            @PathVariable String journeyId,
            @RequestBody LearningJourney journey) {
        return ResponseEntity.ok(journeyService.updateJourney(journeyId, journey));
    }

    @DeleteMapping("/{journeyId}")
    public ResponseEntity<String> deleteJourney(@PathVariable String journeyId) {
        journeyService.deleteJourney(journeyId);
        return ResponseEntity.ok("success");
    }

    @PutMapping("/{journeyId}/primary")
    public ResponseEntity<LearningJourney> setJourneyAsPrimary(@PathVariable String journeyId) {
        return ResponseEntity.ok(journeyService.setJourneyAsPrimary(journeyId));
    }
} 