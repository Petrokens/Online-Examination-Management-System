package com.exam.examPortal.service;

import com.exam.examPortal.entity.User;
import com.exam.examPortal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 1. REGISTER: Create a new account
    public User registerUser(User newUser) {

        // 1. Check if email exists
        Optional<User> existingUser = userRepository.findByEmail(newUser.getEmail());
        if (existingUser.isPresent()) {
            return null;
        }

        // 2. Set Status based on Role
        // This makes the registration decision "Atomic" –
        // it happens the moment the data is created.
        if ("FACULTY".equals(newUser.getRole())) {
            newUser.setStatus("PENDING"); // Lock out teachers
        } else {
            newUser.setStatus("ACTIVE");  // Let students in
        }

        // 3. Save and return
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
}