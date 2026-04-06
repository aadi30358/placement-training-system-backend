package com.example.pts.controller;

import com.example.pts.model.Officer;
import com.example.pts.repository.OfficerRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/officers")
@CrossOrigin(origins = "*")
public class OfficerController {

    private final OfficerRepository officerRepository;

    public OfficerController(OfficerRepository officerRepository) {
        this.officerRepository = officerRepository;
    }

    @GetMapping
    public List<Officer> getAllOfficers() {
        return officerRepository.findAll();
    }

    @PostMapping
    public Officer createOfficer(@RequestBody Officer officer) {
        return officerRepository.save(officer);
    }

    @PutMapping("/{id}")
    public Officer updateOfficer(@PathVariable Long id, @RequestBody Officer details) {
        return officerRepository.findById(id).map(officer -> {
            officer.setName(details.getName());
            officer.setEmail(details.getEmail());
            officer.setRole(details.getRole());
            officer.setDepts(details.getDepts());
            return officerRepository.save(officer);
        }).orElseGet(() -> {
            details.setId(id);
            return officerRepository.save(details);
        });
    }

    @DeleteMapping("/{id}")
    public void deleteOfficer(@PathVariable Long id) {
        officerRepository.deleteById(id);
    }
}
