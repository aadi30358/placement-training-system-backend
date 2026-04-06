package com.example.pts.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String token;
    private Boolean isProfileComplete;
    private Boolean isNewUser;
}