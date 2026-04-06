package com.example.pts.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobDTO {
    private Long id;
    private String title;
    private String company;
    private String location;
    private String salary;
    private String type;
    private String deadline;
    private String eligibility;
    private LocalDateTime postedAt;
}