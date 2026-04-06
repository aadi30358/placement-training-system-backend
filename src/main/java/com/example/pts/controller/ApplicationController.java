package com.example.pts.controller;

import com.example.pts.model.Application;
import com.example.pts.repository.ApplicationRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    private final ApplicationRepository applicationRepository;

    public ApplicationController(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @GetMapping
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    @GetMapping("/student/{studentId}")
    public List<Application> getStudentApplications(@PathVariable Long studentId) {
        return applicationRepository.findByStudentId(studentId);
    }

    @PostMapping
    public Application createApplication(@RequestBody Application application) {
        if (application.getAppliedDate() == null) {
            application.setAppliedDate(LocalDate.now().toString());
        }
        return applicationRepository.save(application);
    }

    @PutMapping("/{id}/status")
    public Application updateStatus(@PathVariable Long id, @RequestBody Application statusUpdate) {
        return applicationRepository.findById(id).map(app -> {
            app.setStatus(statusUpdate.getStatus());
            return applicationRepository.save(app);
        }).orElseThrow(() -> new RuntimeException("Application not found"));
    }

    @DeleteMapping("/{id}")
    public void withdrawApplication(@PathVariable Long id) {
        applicationRepository.deleteById(id);
    }
}
