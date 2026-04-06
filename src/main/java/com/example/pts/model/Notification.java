package com.example.pts.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId; // The ID of the user this belongs to
    private String message;
    private String type;
    private String date;
    private Boolean readStatus; // 'read' is a reserved keyword in some DBs, let's use readStatus
}
