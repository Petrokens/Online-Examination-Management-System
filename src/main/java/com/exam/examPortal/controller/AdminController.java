package com.exam.examPortal.controller;
import com.exam.examPortal.entity.AuditLog;
import com.exam.examPortal.entity.User;
import com.exam.examPortal.repository.AuditLogRepository;
import com.exam.examPortal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    // This handles the request to view the dashboard
    @GetMapping("/dashboard")
    public String viewAdminDashboard(Model model) {
        List<User> allUsers = userService.getAllUsers();
        if (allUsers != null) {
            allUsers.removeIf(u -> u == null);
        }

        // Calculate statistics
        long activeTeachers = allUsers.stream().filter(u -> "FACULTY".equals(u.getRole()) && "ACTIVE".equals(u.getStatus())).count();
        long totalStudents = allUsers.stream().filter(u -> "STUDENT".equals(u.getRole())).count();
        long pendingCount = userService.countUsersByRoleAndStatus("FACULTY", "PENDING");

        model.addAttribute("studentCount", totalStudents);
        model.addAttribute("facultyCount", activeTeachers);
        //model.addAttribute("adminCount", 1); // Assuming 1 admin for now

        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("auditLogs", userService.getAuditLogs(PageRequest.of(0, 5, Sort.by("id").descending())).getContent());

        // NEW: Add this to show an alert banner if there is work to do
        if (pendingCount > 0) {
            model.addAttribute("alertMessage", "Attention: You have " + pendingCount + " pending faculty registrations waiting for review.");
        }

        // Pass data to view
        model.addAttribute("pendingUsers", allUsers.stream().filter(u -> "PENDING".equals(u.getStatus())).collect(Collectors.toList()));
        model.addAttribute("activeUsers", allUsers.stream().filter(u -> "ACTIVE".equals(u.getStatus())).collect(Collectors.toList()));
        model.addAttribute("activeTeachersCount", activeTeachers);
        model.addAttribute("totalStudentsCount", totalStudents);
        model.addAttribute("pendingCount", pendingCount);

        return "admin_panel"; // This tells Spring to look for admin_panel.html
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
}
