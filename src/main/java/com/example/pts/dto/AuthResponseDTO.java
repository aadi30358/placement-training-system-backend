package com.example.pts.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String token;
    private String profilePicture;
    private Boolean isProfileComplete;
    private Boolean isNewUser;
}