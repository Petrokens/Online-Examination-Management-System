package com.exam.examPortal.controller;

import com.exam.examPortal.entity.User;
import com.exam.examPortal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller // Changed from @RestController!
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // --- REGISTRATION ---

    // 1. SHOW THE PAGE: When the user types localhost:8080/user/register in their browser
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // We hand Thymeleaf an empty 'User' object so it can tie the HTML form to our Java class
        model.addAttribute("user", new User());
        return "register"; // Tells Thymeleaf to look for a file named "register.html"
    }

    // 2. CATCH THE DATA: When the user clicks "Submit" on the form
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {

        // 1. SPRING BOOT LOGIC: Inject status constraints automatically before saving
        if ("FACULTY".equals(user.getRole())) {
            user.setStatus("PENDING"); // Teachers are locked out until you approve them manually in DB
        } else {
            user.setStatus("ACTIVE");  // Students are allowed in immediately
        }

        // 2. Hand over the data to your service layer to be saved in MySQL
        User savedUser = userService.registerUser(user);

        if (savedUser == null) {
            model.addAttribute("error", "This email is already registered.");
            return "register";
        }

        // 3. UI Response: Add a friendly guidance query parameter if they registered as a teacher
        if ("FACULTY".equals(user.getRole())) {
            return "redirect:/user/login?message=Registration successful! Please wait for Admin approval.";
        }

        return "redirect:/user/login?message=Registration successful! You can now log in.";
    }

    // --- LOGIN ---

    // 1. SHOW THE PAGE
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Tells Thymeleaf to look for a file named "login.html"
    }

    // 2. CATCH THE DATA
    @PostMapping("/login") // Ensure this matches your login.html form action
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        User user = userService.loginUser(email, password);

        if (user == null) {
            model.addAttribute("error", "Invalid email or password.");
            return "login"; // This returns the login.html page again
        }

        // Check for "PENDING" status for Teachers
        if ("FACULTY".equals(user.getRole()) && !"ACTIVE".equals(user.getStatus())) {
            model.addAttribute("error", "Your account is pending admin approval.");
            return "login";
        }

        session.setAttribute("user", user);

        // Route to the correct dashboard
        if ("FACULTY".equals(user.getRole())) {
            return "redirect:/teacher/dashboard";
        } else {
            return "redirect:/student/dashboard";
        }
    }
}