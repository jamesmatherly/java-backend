package com.example.javabackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

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

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @ManyToOne
    @JoinColumn(name = "journey_id")
    @JsonBackReference
    private LearningJourney journey;

    @OneToMany(mappedBy = "step", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Resource> resources;
} 