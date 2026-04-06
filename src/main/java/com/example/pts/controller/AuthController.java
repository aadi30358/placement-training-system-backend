package com.example.pts.controller;

import com.example.pts.dto.AuthResponseDTO;
import com.example.pts.dto.LoginRequestDTO;
import com.example.pts.dto.RegisterRequestDTO;
import com.example.pts.model.AppUser;
import com.example.pts.model.Student;
import com.example.pts.model.Employer;
import com.example.pts.model.Officer;
import com.example.pts.model.Job;
import com.example.pts.repository.UserRepository;
import com.example.pts.repository.StudentRepository;
import com.example.pts.repository.EmployerRepository;
import com.example.pts.repository.OfficerRepository;
import com.example.pts.repository.JobRepository;
import com.example.pts.security.JwtUtils;
import com.example.pts.service.EmailService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final EmployerRepository employerRepository;
    private final OfficerRepository officerRepository;
    private final JobRepository jobRepository;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Value("${google.clientId}")
    private String googleClientId;

    public AuthController(UserRepository userRepository, 
                          StudentRepository studentRepository, 
                          EmployerRepository employerRepository,
                          OfficerRepository officerRepository,
                          JobRepository jobRepository,
                          EmailService emailService,
                          JwtUtils jwtUtils,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.employerRepository = employerRepository;
        this.officerRepository = officerRepository;
        this.jobRepository = jobRepository;
        this.emailService = emailService;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequestDTO request) {
        try {
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body("Email cannot be empty.");
            }

            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Email already registered.");
            }

            AppUser user = new AppUser();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setRole(request.getRole());
            user.setCompany(request.getCompany());
            user.setRoll(request.getRoll());
            user.setIsNewUser(true);
            user.setIsProfileComplete(false);

            user.setPassword(passwordEncoder.encode(request.getPassword()));

            AppUser savedUser = userRepository.save(user);
            autoCreateRoleEntry(savedUser);

            return ResponseEntity.ok(mapToAuthResponse(savedUser, null));
        } catch (Exception e) {
            logger.error("Registration error for email {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }

    private void autoCreateRoleEntry(AppUser user) {
        try {
            String role = user.getRole();
            if (role != null) {
                String email = user.getEmail();
                String name = user.getName();
                if (role.equals("student") && studentRepository.findByEmail(email).isEmpty()) {
                    Student s = new Student();
                    s.setName(name);
                    s.setEmail(email);
                    s.setRoll(user.getRoll());
                    s.setStatus("Pending");
                    studentRepository.save(s);
                } else if (role.equals("employer") && employerRepository.findByEmail(email).isEmpty()) {
                    Employer e = new Employer();
                    e.setName(name);
                    e.setEmail(email);
                    e.setCompany(user.getCompany());
                    employerRepository.save(e);
                } else if (role.equals("officer") && officerRepository.findByEmail(email).isEmpty()) {
                    Officer o = new Officer();
                    o.setName(name);
                    o.setEmail(email);
                    o.setRole("Placement Officer");
                    officerRepository.save(o);
                }
            }
        } catch (Exception e) {
            logger.error("Error auto-creating role entry for user {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDTO loginRequest) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            Optional<AppUser> userOptional = userRepository.findByEmail(loginRequest.getEmail());
            if (userOptional.isPresent()) {
                AppUser user = userOptional.get();

                // Allow admin role to bypass requested role validations
                if (!user.getRole().equalsIgnoreCase("admin") && !user.getRole().equalsIgnoreCase(loginRequest.getRole())) {
                    return ResponseEntity.status(401).body("Invalid role for this user.");
                }

                logger.info("User {} successfully authenticated with role {}", user.getEmail(), user.getRole());

                String token = jwtUtils.generateToken(user.getEmail(), user.getRole());

                try {
                    emailService.sendLoginNotificationEmail(user.getEmail(), user.getName(), jobRepository.findTop3ByOrderByIdDesc(), false);
                } catch (Exception e) {
                    logger.error("Failed to send login notification: {}", e.getMessage());
                }
return ResponseEntity.ok(mapToAuthResponse(user, token));
            }
            return ResponseEntity.status(404).body("User not found.");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid credentials: " + e.getMessage());
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
        String idTokenString = request.get("credential");
        String requestedRole = request.get("role");

        if (idTokenString == null || idTokenString.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Google ID token.");
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken;
            try {
                idToken = verifier.verify(idTokenString);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid Google Token format.", e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Google Token format.");
            }

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");

                Optional<AppUser> userOpt = userRepository.findByEmail(email);  
                AppUser user;
                boolean isNewUser = false;
                if (userOpt.isPresent()) {
                    user = userOpt.get();
                    if (pictureUrl != null) {
                        user.setProfilePicture(pictureUrl);
                        user = userRepository.save(user);
                    }
                } else {
                    isNewUser = true;
                    user = new AppUser();
                    user.setEmail(email);
                    user.setName(name);
                    user.setProfilePicture(pictureUrl);
                    user.setRole(requestedRole != null ? requestedRole : "student");
                    user.setIsNewUser(true);
                    user.setIsProfileComplete(false);
                    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    user = userRepository.save(user);
                    autoCreateRoleEntry(user);
                }
                
                String token = jwtUtils.generateToken(user.getEmail(), user.getRole());

                try {
                    emailService.sendLoginNotificationEmail(email, name, jobRepository.findTop3ByOrderByIdDesc(), isNewUser);
                } catch (Exception e) {
                    logger.error("Failed to trigger login notification: {}", e.getMessage());
                }

                return ResponseEntity.ok(mapToAuthResponse(user, token));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid ID token. Verification failed.");
            }
        } catch (Exception e) {
            logger.error("Error processing Google login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing Google login: " + e.getMessage());
        }
    }

    private AuthResponseDTO mapToAuthResponse(AppUser user, String token) {
        AuthResponseDTO response = new AuthResponseDTO();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setToken(token != null ? token : user.getToken());
        response.setProfilePicture(user.getProfilePicture());
        response.setIsProfileComplete(user.getIsProfileComplete());
        response.setIsNewUser(user.getIsNewUser());
        return response;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<AppUser> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            String otp = String.format("%06d", new java.util.Random().nextInt(999999));
            user.setResetToken(otp);
            user.setResetTokenExpiry(java.time.LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);
            
            emailService.sendPasswordResetEmail(email, otp);
            return ResponseEntity.ok(Map.of("message", "OTP sent to your email."));
        }
        return ResponseEntity.status(404).body("User with this email not found.");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String otp = request.get("otp");
        Optional<AppUser> userOpt = userRepository.findByResetToken(otp);
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            if (user.getResetTokenExpiry().isAfter(java.time.LocalDateTime.now())) {
                return ResponseEntity.ok(Map.of("message", "OTP Verified"));
            }
            return ResponseEntity.status(HttpStatus.GONE).body("OTP has expired.");
        }
        return ResponseEntity.status(404).body("Invalid OTP.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");
        
        Optional<AppUser> userOpt = userRepository.findByResetToken(otp);
        
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            if (user.getResetTokenExpiry().isAfter(java.time.LocalDateTime.now())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetToken(null);
                user.setResetTokenExpiry(null);
                userRepository.save(user);
                return ResponseEntity.ok(Map.of("message", "Password reset successful."));
            }
            return ResponseEntity.status(HttpStatus.GONE).body("OTP has expired.");
        }
        return ResponseEntity.status(404).body("Invalid OTP.");
    }

    @PutMapping("/profile/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody AppUser updates) {
        Optional<AppUser> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            if (updates.getName() != null) user.setName(updates.getName());
            if (updates.getCompany() != null) user.setCompany(updates.getCompany());
            if (updates.getRoll() != null) user.setRoll(updates.getRoll());
            user.setIsProfileComplete(true);
            return ResponseEntity.ok(userRepository.save(user));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP", "timestamp", java.time.LocalDateTime.now()));
    }
}
