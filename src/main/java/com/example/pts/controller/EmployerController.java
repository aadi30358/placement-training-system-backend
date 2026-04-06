package com.example.pts.controller;

import com.example.pts.model.Employer;
import com.example.pts.repository.EmployerRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/employers")
@CrossOrigin(origins = "*")
public class EmployerController {

    private final EmployerRepository employerRepository;

    public EmployerController(EmployerRepository employerRepository) {
        this.employerRepository = employerRepository;
    }

    @GetMapping
    public List<Employer> getAllEmployers() {
        return employerRepository.findAll();
    }

    @PostMapping
    public Employer createEmployer(@RequestBody Employer employer) {
        return employerRepository.save(employer);
    }

    @PutMapping("/{id}")
    public Employer updateEmployer(@PathVariable Long id, @RequestBody Employer details) {
        return employerRepository.findById(id).map(employer -> {
            employer.setName(details.getName());
            employer.setCompany(details.getCompany());
            employer.setEmail(details.getEmail());
            employer.setIndustry(details.getIndustry());
            employer.setLocation(details.getLocation());
            return employerRepository.save(employer);
        }).orElseGet(() -> {
            details.setId(id);
            return employerRepository.save(details);
        });
    }

    @DeleteMapping("/{id}")
    public void deleteEmployer(@PathVariable Long id) {
        employerRepository.deleteById(id);
    }
}
