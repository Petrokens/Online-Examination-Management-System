package com.exam.examPortal.controller;

import com.exam.examPortal.entity.Exam;
import com.exam.examPortal.entity.User;
import com.exam.examPortal.repository.ExamRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/delete/{id}")
    public String deleteExam(@PathVariable Long id, HttpSession session) {
        // Security Check
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null || !"FACULTY".equals(loggedInUser.getRole())) {
            return "redirect:/user/login";
        }

        // Delete the exam
        examRepository.deleteById(id);

        // Redirect back to dashboard
        return "redirect:/teacher/dashboard";
    }

    // 1. Show the Edit Form using the correct, existing template
    @GetMapping("/edit/{id}")
    public String showEditExamForm(@PathVariable Long id, Model model) {
        // Find the exam, or fallback to null safely
        Exam exam = examRepository.findById(id).orElse(null);

        if (exam == null) {
            return "redirect:/teacher/dashboard?error=ExamNotFound";
        }

        model.addAttribute("exam", exam);
        return "create-exam"; // Reuses the template you modified!
    }

    // 2. Save the Updates (Unified route for handling both inserts and updates)
    @PostMapping("/save-exam")
    public String saveExam(@ModelAttribute Exam exam, Model model) {
        if (exam.getMaxAttempts() != null && exam.getMaxAttempts() < 1) {
            model.addAttribute("error", "The exam must allow at least 1 attempt!");
            return "create-exam";
        }

        // If examId exists, Hibernate automatically runs an UPDATE query
        // If examId is null, Hibernate runs an INSERT query
        examRepository.save(exam);
        return "redirect:/teacher/dashboard";
    }


}
