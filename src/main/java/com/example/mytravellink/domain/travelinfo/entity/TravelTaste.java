package com.example.mytravellink.domain.travelinfo.entity;

import com.example.mytravellink.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

import org.hibernate.annotations.UuidGenerator;

/**
 * 여행 취향 (Travel_Taste)
 */

 @Entity
 @Table(name = "travel_taste")
 @Getter
 @Setter
 @NoArgsConstructor
 @AllArgsConstructor
 public class TravelTaste {
        
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(length = 36)
     private String id;
 
     @ManyToOne
     @JoinColumn(name = "email", nullable = false)
     private User user;
 
     @Column(nullable = false)
     private boolean landmark;
 
     @Column(nullable = false)
     private boolean relax;
 
     @Column(nullable = false)
     private boolean food;
 
     @Column(nullable = false)
     private boolean alone;
 
     @Column(nullable = false)
     private boolean romance;
 
     @Column(nullable = false)
     private boolean friend;
 
     @Column(nullable = false)
     private boolean child;
 
     @Column(nullable = false)
     private boolean parents;
 
     @Column(name = "travel_days", nullable = false)
     private int travelDays;
 
     @Column(name = "options_input", length = 255, nullable = false)
     private String optionsInput;
 
     @Column(name = "create_at", nullable = false)
     private LocalDateTime createAt = LocalDateTime.now();
 }
