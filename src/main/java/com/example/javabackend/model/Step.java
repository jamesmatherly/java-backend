package com.example.javabackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "steps")
public class Step {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String icon;

    @Column(nullable = false)
    private double progress;

    @Column(nullable = false)
    private boolean completed;

    @ManyToOne
    @JoinColumn(name = "journey_id")
    private LearningJourney journey;

    @OneToMany(mappedBy = "step", cascade = CascadeType.ALL)
    private List<Resource> resources;
} 