package com.exam.examPortal.controller;

import com.exam.examPortal.entity.User;
import com.exam.examPortal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        User savedUser = userService.registerUser(user);

        if (savedUser == null) {
            // Email was taken! Send an error message back to the HTML page
            model.addAttribute("error", "This email is already registered.");
            return "register"; // Reload the register page so they can try again
        }

        // Success! Send them to the login page
        return "redirect:/user/login";
    }

    // --- LOGIN ---

    // 1. SHOW THE PAGE
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Tells Thymeleaf to look for a file named "login.html"
    }

    // 2. CATCH THE DATA
    @PostMapping("/login")
    public String loginUser(@RequestParam String email, @RequestParam String password, Model model) {
        User loggedInUser = userService.loginUser(email, password);

        if (loggedInUser == null) {
            model.addAttribute("error", "Invalid email or password.");
            return "login"; // Reload the login page with an error
        }

        // Success! Send them to their dashboard
        return "redirect:/dashboard";
    }
}