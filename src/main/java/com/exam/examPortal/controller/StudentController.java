package com.exam.examPortal.controller;

import com.exam.examPortal.entity.User;
import com.exam.examPortal.repository.ExamRepository;
import com.exam.examPortal.repository.ResultRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ResultRepository resultRepository;

    @GetMapping("/dashboard")
    public String studentDashboard(HttpSession session, Model model) {
        // 1. Verify User
        User user = (User) session.getAttribute("user");
        if (user == null || !"STUDENT".equals(user.getRole())) {
            return "redirect:/login";
        }

        // 2. Load Data for the dashboard
        model.addAttribute("exams", examRepository.findAll());
        // We will filter these results later in the view

        return "student_dashboard";
    }
}