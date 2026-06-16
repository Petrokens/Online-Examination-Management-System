package com.exam.examPortal.controller;

import com.exam.examPortal.entity.Exam;
import com.exam.examPortal.entity.Result;
import com.exam.examPortal.entity.User;
import com.exam.examPortal.repository.ExamRepository;
import com.exam.examPortal.repository.ResultRepository;
import com.exam.examPortal.service.UserService;
import com.exam.examPortal.security.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private ExamRepository examRepository;
    @Autowired
    private ResultRepository resultRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/dashboard")
    public String showTeacherDashboard(HttpServletRequest request, Model model) {
        // Use the centralized method
        User teacher = userService.getAuthenticatedUser(request);

        // Security check
        if (teacher == null || !"FACULTY".equals(teacher.getRole())) {
            return "redirect:/user/login?error=Unauthorized";
        }

        model.addAttribute("teacher", teacher);
        model.addAttribute("exams", examRepository.findByTeacher(teacher));
        return "teacher_dashboard";
    }

    @GetMapping("/results")
    public String viewTeacherResults(@RequestParam Long examId,
                                     @RequestParam(defaultValue = "0") int page,
                                     HttpServletRequest request,
                                     Model model) {
        // Use the centralized method
        User teacher = userService.getAuthenticatedUser(request);
        Exam exam = examRepository.findById(examId).orElse(null);

        // Security check
        if (teacher == null || !"FACULTY".equals(teacher.getRole()) ||
                exam == null || !exam.getTeacher().equals(teacher)) {
            return "redirect:/teacher/dashboard?error=Unauthorized";
        }

        Pageable pageable = PageRequest.of(page, 10, Sort.by("submissionTime").descending());
        Page<Result> resultPage = resultRepository.findByExamAndTeacherAndUserRole(exam, teacher, "STUDENT", pageable);

        model.addAttribute("resultPage", resultPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("exam", exam);

        return "teacher_results";
    }

    @GetMapping("/edit/{id}")
    public String showEditExamForm(@PathVariable Long id, HttpServletRequest request, Model model) {
        // 1. Fetch the exam
        Exam exam = examRepository.findById(id).orElse(null);
        User teacher = userService.getAuthenticatedUser(request);

        // 2. Security Check
        if (exam == null || !exam.getTeacher().equals(teacher)) {
            return "redirect:/teacher/dashboard?error=Unauthorized";
        }

        // 3. Inject Data
        model.addAttribute("exam", exam);
        model.addAttribute("allStudents", userService.getAllStudents()); // Fixes the empty list

        return "create-exam"; // Reuses the form
    }

    @PostMapping("/edit-exam/{id}")
    public String updateExam(@PathVariable Long id, @ModelAttribute Exam updatedExam) {
        // 1. Fetch existing
        Exam existingExam = examRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ID"));

        // 2. Map fields manually to keep the Teacher association intact
        existingExam.setExamName(updatedExam.getExamName());
        existingExam.setDurationMinutes(updatedExam.getDurationMinutes());
        existingExam.setTotalMarks(updatedExam.getTotalMarks());
        existingExam.setMaxAttempts(updatedExam.getMaxAttempts());

        // 3. Update the Student Whitelist
        existingExam.setAllowedStudents(updatedExam.getAllowedStudents());

        // 4. Save
        examRepository.save(existingExam);
        return "redirect:/teacher/dashboard";
    }

    @GetMapping("/delete/{id}")
    public String deleteExam(@PathVariable Long id, HttpServletRequest request) {
        // 1. Authenticate Teacher
        User teacher = userService.getAuthenticatedUser(request);
        Exam exam = examRepository.findById(id).orElse(null);

        // 2. Security Check: Ensure the exam exists and belongs to the teacher
        if (teacher == null || !"FACULTY".equals(teacher.getRole()) ||
                exam == null || !exam.getTeacher().equals(teacher)) {
            return "redirect:/teacher/dashboard?error=Unauthorized";
        }

        // 3. Delete the exam
        examRepository.deleteById(id);

        return "redirect:/teacher/dashboard?message=DeletedSuccessfully";
    }
}