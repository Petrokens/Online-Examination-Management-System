package com.exam.examPortal.controller;

import com.exam.examPortal.entity.Exam;
import com.exam.examPortal.entity.Result;
import com.exam.examPortal.entity.StudentAnswer;
import com.exam.examPortal.entity.User;
import com.exam.examPortal.repository.ExamRepository;
import com.exam.examPortal.repository.ResultRepository;
import com.exam.examPortal.repository.StudentAnswerRepository;
import com.exam.examPortal.service.ResultService;
import com.exam.examPortal.service.UserService;
import com.exam.examPortal.security.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/result")
public class ResultController {

    @Autowired
    private ResultService resultService;
    @Autowired
    private StudentAnswerRepository studentAnswerRepository;
    @Autowired
    private ExamRepository examRepository;
    @Autowired
    private ResultRepository resultRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/submit/{examId}")
    public String submitExam(@PathVariable Long examId, HttpServletRequest request) {
        // Updated to use centralized service method
        User user = userService.getAuthenticatedUser(request);
        if (user == null) return "redirect:/user/login";

        // Logic to process the submission
        Exam exam = examRepository.findById(examId).orElse(null);
        List<StudentAnswer> examAnswers = studentAnswerRepository.findByUserAndQuestion_Exam(user, exam);

        Result result = new Result();
        result.setUser(user);
        result.setExam(exam);
        result.setSubmissionTime(java.time.LocalDateTime.now());
        if (exam != null) result.setTeacher(exam.getTeacher());

        resultService.calculateAndSaveResult(result, examAnswers);

        return "redirect:/result/view/" + result.getResultId();
    }

    @GetMapping("/view/{id}")
    public String viewResult(@PathVariable Long id, HttpServletRequest request, Model model) {
        // Updated to use centralized service method
        User loggedInUser = userService.getAuthenticatedUser(request);
        if (loggedInUser == null) return "redirect:/user/login";

        Result result = resultRepository.findById(id).orElse(null);

        if (result == null) return "redirect:/teacher/dashboard?error=NotFound";

        boolean isOwner = (result.getTeacher() != null && result.getTeacher().equals(loggedInUser));
        boolean isStudent = (result.getUser() != null && result.getUser().equals(loggedInUser));

        if (!isOwner && !isStudent) return "redirect:/teacher/dashboard?error=Unauthorized";

        model.addAttribute("result", result);
        return "result";
    }
}