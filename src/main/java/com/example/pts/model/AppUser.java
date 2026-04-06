package com.example.pts.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Data
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Column(unique = true)
    private String email;

    private String password;
    private String role;
    
    private String profilePicture;

    // Optional fields depending on role setup
    private String roll; // for student
    private String company; // for employer
    private Boolean isProfileComplete;
    private Boolean isNewUser;

    // Reset password fields
    private String resetToken;
    private java.time.LocalDateTime resetTokenExpiry;

    @Transient
    private String token;
}
