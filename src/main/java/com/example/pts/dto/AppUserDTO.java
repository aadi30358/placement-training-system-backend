package com.example.pts.dto;

import lombok.Data;

@Data
public class AppUserDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String roll;
    private String company;
    private Boolean isProfileComplete;
    private Boolean isNewUser;
}