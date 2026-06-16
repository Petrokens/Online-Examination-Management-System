package com.exam.examPortal.service;

import com.exam.examPortal.entity.AuditLog;
import com.exam.examPortal.entity.User;
import com.exam.examPortal.repository.AuditLogRepository;
import com.exam.examPortal.repository.UserRepository;
import com.exam.examPortal.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder; // Add this line

    // This stores your history
    private List<String> auditLogs = new ArrayList<>();

    public void logAction(String action) {
        // 1. Create the AuditLog object (the 'log' variable you were missing)
        AuditLog log = new AuditLog();

        // 2. Set the data
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm, dd-MM-yyyy"));

        log.setAction(action + " at " + timestamp);

        // 3. Save the actual object to the database
        auditLogRepository.save(log);
    }

    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    // 1. REGISTER: Create a new account
    public User registerUser(User newUser) {
        // 1. Check if email exists
        Optional<User> existingUser = userRepository.findByEmail(newUser.getEmail());
        if (existingUser.isPresent()) {
            return null;
        }

        // 2. HASH THE PASSWORD BEFORE SAVING!
        String encodedPassword = passwordEncoder.encode(newUser.getPassword());
        newUser.setPassword(encodedPassword);

        // 3. Set Status based on Role
        if ("FACULTY".equals(newUser.getRole())) {
            newUser.setStatus("PENDING");
        } else {
            newUser.setStatus("ACTIVE");
        }
        newUser.setRegistrationDate(java.time.LocalDateTime.now());
        return userRepository.save(newUser);
    }

    // 2. LOGIN: Check if credentials are correct
    public User loginUser(String email, String password) {
        Optional<User> foundUser = userRepository.findByEmail(email);

        // Use passwordEncoder.matches() instead of .equals()
        if (foundUser.isPresent() && passwordEncoder.matches(password, foundUser.get().getPassword())) {
            return foundUser.get();
        }

        return null;
    }


    // 3. GET ALL: Used for the Admin Panel list
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 4. GET BY ID: Used to find a specific user to promote/approve them
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // 5. UPDATE/SAVE: Used to change the status (PENDING -> ACTIVE)
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // 6. DELETE: Used to reject/delete a pending request
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Add this to UserService.java
    public List<Integer> getRegistrationTrend() {
        List<Integer> trend = new ArrayList<>();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // Loop for the last 5 days
        for (int i = 4; i >= 0; i--) {
            java.time.LocalDateTime startOfDay = now.minusDays(i).toLocalDate().atStartOfDay();
            java.time.LocalDateTime endOfDay = now.minusDays(i).toLocalDate().atTime(23, 59, 59);

            // Count users created in this range
            // Note: Ensure you have a 'countByRegistrationDateBetween' method in your UserRepository
            long count = userRepository.countByRegistrationDateBetween(startOfDay, endOfDay);
            trend.add((int) count);
        }
        return trend;
    }

    // 1. Centralized Token Extractor
    private String getTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("accessToken".equals(c.getName())) return c.getValue();
            }
        }
        return null;
    }

    // 2. Centralized Authentication Fetcher
    public User getAuthenticatedUser(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null || !jwtUtils.validateToken(token)) {
            return null; // Token is missing or invalid
        }
        String email = jwtUtils.getEmailFromToken(token);
        return findByEmail(email);
    }

    // In UserService.java
    public long countUsersByRoleAndStatus(String role, String status) {
        return userRepository.findAll().stream()
                .filter(u -> role.equals(u.getRole()) && status.equals(u.getStatus()))
                .count();
    }

    public List<User> getPendingUsers() {
        return userRepository.findAll().stream()
                .filter(u -> "PENDING".equals(u.getStatus()))
                .collect(Collectors.toList());
    }

    public List<User> getActiveUsers() {
        return userRepository.findAll().stream()
                .filter(u -> "ACTIVE".equals(u.getStatus()))
                .collect(Collectors.toList());
    }

    public List<User> getAllStudents() {
        // This filters your user list to only return students
        return userRepository.findAll().stream()
                .filter(u -> "STUDENT".equals(u.getRole()))
                .collect(java.util.stream.Collectors.toList());
    }

    // Inside UserService.java
    public List<User> getAdmins() {
        // Assuming you have a UserRepository method: findByRole(String role)
        return userRepository.findByRole("ADMIN");
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}