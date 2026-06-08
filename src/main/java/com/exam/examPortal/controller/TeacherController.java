package com.exam.examPortal.controller;

import com.exam.examPortal.entity.Exam;
import com.exam.examPortal.entity.User;
import com.exam.examPortal.repository.ExamRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private ExamRepository examRepository;

    // TEACHER DASHBOARD: View all created exams
    @GetMapping("/dashboard")
    public String showTeacherDashboard(HttpSession session, Model model) {
        // 1. Fetch user from session to ensure security lockdown
        User loggedInUser = (User) session.getAttribute("user");

        // 2. Security Guard: If not logged in, or if they are NOT FACULTY, kick them out
        if (loggedInUser == null || !"FACULTY".equals(loggedInUser.getRole())) {
            return "redirect:/user/login?error=AccessDenied";
        }

        // 3. Fetch all exams from the database
        List<Exam> examsList = examRepository.findAll();

        // 4. Send the list of exams to the Thymeleaf HTML template
        model.addAttribute("exams", examsList);

        // 5. Look for teacher_dashboard.html template
        return "teacher_dashboard";
    }
}