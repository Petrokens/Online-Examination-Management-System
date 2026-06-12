package com.exam.examPortal.controller;
import com.exam.examPortal.entity.AuditLog;
import com.exam.examPortal.entity.User;
import com.exam.examPortal.repository.AuditLogRepository;
import com.exam.examPortal.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping("/dashboard")
    public String viewAdminDashboard(HttpSession session, Model model) {
        // 1. Security Check: Is the user logged in and an ADMIN?
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/admin/login?error=Unauthorized";
        }

        // 2. Fetch data (from your original viewAdminDashboard logic)
        List<User> allUsers = userService.getAllUsers();
        long activeTeachers = allUsers.stream().filter(u -> "FACULTY".equals(u.getRole()) && "ACTIVE".equals(u.getStatus())).count();
        long totalStudents = allUsers.stream().filter(u -> "STUDENT".equals(u.getRole())).count();
        long pendingCount = userService.countUsersByRoleAndStatus("FACULTY", "PENDING");

        // 3. Add to model
        model.addAttribute("admin", user); // Pass the admin object
        model.addAttribute("studentCount", totalStudents);
        model.addAttribute("facultyCount", activeTeachers);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("auditLogs", userService.getAuditLogs(PageRequest.of(0, 5, Sort.by("id").descending())).getContent());

        // Add banner if needed
        if (pendingCount > 0) {
            model.addAttribute("alertMessage", "Attention: You have " + pendingCount + " pending faculty registrations.");
        }

        model.addAttribute("pendingUsers", allUsers.stream().filter(u -> "PENDING".equals(u.getStatus())).collect(Collectors.toList()));
        model.addAttribute("activeUsers", allUsers.stream().filter(u -> "ACTIVE".equals(u.getStatus())).collect(Collectors.toList()));

        return "admin_panel";
    }

    @GetMapping("/history")
    public String viewAuditHistory(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // Use the service method we updated earlier
        Pageable pageable = PageRequest.of(page, 10, Sort.by("id").descending());
        Page<AuditLog> logPage = userService.getAuditLogs(pageable);

        model.addAttribute("logPage", logPage);
        return "audit_history";
    }

    // This handles the "Approve" button
    @GetMapping("/approve/{id}")
    public String approveTeacher(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null && "FACULTY".equals(user.getRole())) {
            user.setStatus("ACTIVE");
            userService.saveUser(user);
            // Log the approval
            userService.logAction("APPROVED: " + user.getName());
        }
        return "redirect:/admin/dashboard?success=approved";
    }

    @GetMapping("/reject/{id}")
    public String rejectTeacher(@PathVariable Long id) {
        // We fetch the user first to make sure they exist
        User user = userService.getUserById(id);

        // Only delete if they are indeed a pending faculty member
        if (user != null && "FACULTY".equals(user.getRole()) && "PENDING".equals(user.getStatus())) {
            userService.deleteUser(id);
        }
        // Log the rejection
        userService.logAction("REJECTED: " + user.getName());

        // Redirect back to dashboard with a success message
        return "redirect:/admin/dashboard?success=rejected";
    }
    // Add these to AdminController.java
    @GetMapping("/login")
    public String showAdminLoginForm() {
        return "admin_login";
    }

    @PostMapping("/login")
    public String adminLogin(@RequestParam String email,
                             @RequestParam String password,
                             HttpSession session,
                             Model model) {
        User user = userService.loginUser(email, password);
        if (user != null && "ADMIN".equals(user.getRole())) {
            session.setAttribute("user", user);
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("error", "Invalid Admin credentials.");
        return "admin_login";
    }


}
