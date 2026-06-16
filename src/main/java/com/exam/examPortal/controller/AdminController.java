package com.exam.examPortal.controller;

import com.exam.examPortal.entity.AuditLog;
import com.exam.examPortal.entity.User;
import com.exam.examPortal.repository.AuditLogRepository;
import com.exam.examPortal.security.JwtUtils;
import com.exam.examPortal.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuditLogRepository auditLogRepository;

    // =========================
    // DASHBOARD
    // =========================
    @GetMapping("/dashboard")
    public String viewAdminDashboard(HttpServletRequest request, Model model) {

        // Get admin email from JWT cookie

        User admin = userService.getAuthenticatedUser(request);

        // Security check
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/admin/login?error=Unauthorized";
        }

        List<User> allUsers = userService.getAllUsers();

        long activeTeachers = allUsers.stream()
                .filter(u -> "FACULTY".equals(u.getRole())
                        && "ACTIVE".equals(u.getStatus()))
                .count();

        long totalStudents = allUsers.stream()
                .filter(u -> "STUDENT".equals(u.getRole()))
                .count();

        long pendingCount = userService.countUsersByRoleAndStatus(
                "FACULTY",
                "PENDING"
        );

        model.addAttribute("admin", admin);
        model.addAttribute("user", admin);

        model.addAttribute("studentCount", totalStudents);
        model.addAttribute("facultyCount", activeTeachers);
        model.addAttribute("pendingCount", pendingCount);

        model.addAttribute(
                "auditLogs",
                userService.getAuditLogs(
                        PageRequest.of(0, 5, Sort.by("id").descending())
                ).getContent()
        );

        if (pendingCount > 0) {
            model.addAttribute(
                    "alertMessage",
                    "Attention: You have "
                            + pendingCount
                            + " pending faculty registrations."
            );
        }

        model.addAttribute(
                "pendingUsers",
                allUsers.stream()
                        .filter(u -> "PENDING".equals(u.getStatus()))
                        .collect(Collectors.toList())
        );

        model.addAttribute(
                "activeUsers",
                allUsers.stream()
                        .filter(u -> "ACTIVE".equals(u.getStatus()))
                        .collect(Collectors.toList())
        );

        List<Integer> registrationTrend = userService.getRegistrationTrend();
        model.addAttribute("registrationData", registrationTrend);

        return "admin_panel";
    }

    // =========================
    // AUDIT HISTORY
    // =========================
    @GetMapping("/history")
    public String viewAuditHistory(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by("id").descending()
        );

        Page<AuditLog> logPage = userService.getAuditLogs(pageable);

        model.addAttribute("logPage", logPage);

        return "audit_history";
    }

    // =========================
    // APPROVE FACULTY
    // =========================
    @GetMapping("/approve/{id}")
    public String approveTeacher(@PathVariable Long id) {

        User user = userService.getUserById(id);

        if (user != null && "FACULTY".equals(user.getRole())) {

            user.setStatus("ACTIVE");
            userService.saveUser(user);

            userService.logAction(
                    "APPROVED: " + user.getName()
            );
        }

        return "redirect:/admin/dashboard?success=approved";
    }

    // =========================
    // REJECT FACULTY
    // =========================
    @GetMapping("/reject/{id}")
    public String rejectTeacher(@PathVariable Long id) {

        User user = userService.getUserById(id);

        if (user != null
                && "FACULTY".equals(user.getRole())
                && "PENDING".equals(user.getStatus())) {

            String name = user.getName();

            userService.deleteUser(id);

            userService.logAction(
                    "REJECTED: " + name
            );
        }

        return "redirect:/admin/dashboard?success=rejected";
    }

    /*
    // =========================
    // LOGIN PAGE
    // =========================
    @GetMapping("/login")
    public String showAdminLoginForm() {
        return "admin_login";
    }

     */

    // =========================
    // LOGIN (JWT ONLY)
    // =========================
    @PostMapping("/login")
    public String adminLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletResponse response,
            Model model) {

        User user = userService.loginUser(email, password);

        if (user != null && "ADMIN".equals(user.getRole())) {

            String token = jwtUtils.generateAccessToken(
                    user.getEmail(),
                    user.getRole()
            );

            Cookie cookie = new Cookie("accessToken", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);

            // Uncomment if using HTTPS
            // cookie.setSecure(true);

            response.addCookie(cookie);

            response.addCookie(cookie);

            return "redirect:/admin/dashboard";
        }

        model.addAttribute(
                "error",
                "Invalid Admin credentials."
        );

        return "admin_login";
    }

    // =========================
    // LOGOUT
    // =========================
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {

        Cookie cookie = new Cookie("accessToken", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return "redirect:/admin/login?logout";
    }


}