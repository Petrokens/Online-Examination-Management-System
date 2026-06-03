package com.exam.examPortal.service;

import com.exam.examPortal.entity.User;
import com.exam.examPortal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 1. REGISTER: Create a new account
    public User registerUser(User newUser) {

        // --- THE BRAIN (Business Logic) ---

        // Check if a user with this email already exists
        Optional<User> existingUser = userRepository.findByEmail(newUser.getEmail());
        if (existingUser.isPresent()) {
            // Email is already taken! Return null so the Controller knows it failed.
            return null;
        }

        // Security Check: Force the role to "STUDENT" by default.
        // This prevents hackers from sending a request saying their role is "ADMIN".
        if (newUser.getRole() == null || newUser.getRole().isEmpty()) {
            newUser.setRole("STUDENT");
        }

        // --- THE TRIGGER ---
        return userRepository.save(newUser);
    }

    // 2. LOGIN: Check if credentials are correct
    public User loginUser(String email, String password) {

        // Find the user's safety box using their email
        Optional<User> foundUser = userRepository.findByEmail(email);

        // If the box is NOT empty, AND the password exactly matches what they typed:
        if (foundUser.isPresent() && foundUser.get().getPassword().equals(password)) {
            return foundUser.get(); // Success! Hand the user data back to log them in.
        }

        // If the email doesn't exist, or the password was wrong, return null.
        return null;
    }
}