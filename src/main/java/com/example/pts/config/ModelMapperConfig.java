package com.example.pts.config;

@org.springframework.context.annotation.Configuration
public class ModelMapperConfig {
    @org.springframework.context.annotation.Bean
    public org.modelmapper.ModelMapper modelMapper() {
        return new org.modelmapper.ModelMapper();
    }
}