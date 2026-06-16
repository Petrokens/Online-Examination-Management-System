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
}