package com.example.pts.controller;

import com.example.pts.dto.AppUserDTO;
import com.example.pts.model.AppUser;
import com.example.pts.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public AdminController(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalStudents = userRepository.findAll().stream().filter(u -> "student".equalsIgnoreCase(u.getRole())).count();
        long totalEmployers = userRepository.findAll().stream().filter(u -> "employer".equalsIgnoreCase(u.getRole())).count();
        long totalOfficers = userRepository.findAll().stream().filter(u -> "officer".equalsIgnoreCase(u.getRole())).count();
        
        return ResponseEntity.ok(Map.of(
            "totalUsers", totalUsers,
            "totalStudents", totalStudents,
            "totalEmployers", totalEmployers,
            "totalOfficers", totalOfficers
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<List<AppUserDTO>> getAllUsers() {
        List<AppUserDTO> users = userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, AppUserDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<AppUserDTO> updateUser(@PathVariable Long id, @RequestBody AppUserDTO userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            user.setRole(userDetails.getRole());
            user.setCompany(userDetails.getCompany());
            user.setRoll(userDetails.getRoll());
            AppUser saved = userRepository.save(user);
            return ResponseEntity.ok(modelMapper.map(saved, AppUserDTO.class));
        }).orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id " + id);
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}
