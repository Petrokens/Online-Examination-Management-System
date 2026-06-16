package com.exam.examPortal.controller;

import com.exam.examPortal.entity.User;
import com.exam.examPortal.service.UserService;
import com.exam.examPortal.security.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        if ("FACULTY".equals(user.getRole())) {
            user.setStatus("PENDING");
        } else {
            user.setStatus("ACTIVE");
        }
        userService.registerUser(user);
        return "redirect:/user/login?message=Registration successful!";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email, @RequestParam String password, HttpServletResponse response) {
        User user = userService.loginUser(email, password);
        if (user != null && "ACTIVE".equals(user.getStatus())) {

            // 1. Generate both tokens
            String accessToken = jwtUtils.generateAccessToken(user.getEmail(), user.getRole());
            String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());

            // 2. Set Access Token Cookie (Short lived)
            Cookie accessCookie = new Cookie("accessToken", accessToken);
            accessCookie.setHttpOnly(true);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(3600); // 1 hour
            response.addCookie(accessCookie);

            // 3. Set Refresh Token Cookie (Long lived)
            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
            response.addCookie(refreshCookie);


            if ("ADMIN".equals(user.getRole())) {
                return "redirect:/admin/dashboard";
            } else if ("FACULTY".equals(user.getRole())) {
                return "redirect:/teacher/dashboard";
            } else {
                return "redirect:/student/dashboard";
            }
        }
        return "redirect:/user/login?error=InvalidCredentials";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        // 1. Clear Access Token
        Cookie accessCookie = new Cookie("accessToken", null);
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        // 2. Clear Refresh Token (CRITICAL)
        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        return "redirect:/user/login?message=Logged out successfully.";
    }
}