package com.example.pts.dto;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String name;
    private String email;
    private String password;
    private String role;
    private String roll;
    private String company;
}