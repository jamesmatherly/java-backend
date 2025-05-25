package com.example.javabackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "learning_journeys")
public class LearningJourney {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(name = "is_primary")
    private boolean primary;

    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL)
    private List<Step> steps;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
} 