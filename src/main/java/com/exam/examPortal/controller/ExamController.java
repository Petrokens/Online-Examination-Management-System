package com.exam.examPortal.controller;

import com.exam.examPortal.entity.*;
import com.exam.examPortal.repository.ExamRepository;
import com.exam.examPortal.repository.ResultRepository;
import com.exam.examPortal.repository.StudentAnswerRepository;
import com.exam.examPortal.service.*;
import com.exam.examPortal.security.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    private ExamService examService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private UserService userService;
    @Autowired
    private ResultService resultService;
    @Autowired
    private StudentAnswerService studentAnswerService;
    @Autowired
    private ResultRepository resultRepository;
    @Autowired
    private ExamRepository examRepository;
    @Autowired
    private StudentAnswerRepository studentAnswerRepository;
    @Autowired
    private JwtUtils jwtUtils;

    // --- TEACHER ACTIONS ---

    @GetMapping("/create")
    public String showCreateExamForm(HttpServletRequest request, Model model) {
        // Updated to use centralized service method
        User user = userService.getAuthenticatedUser(request);
        if (user == null || !"FACULTY".equals(user.getRole())) {
            return "redirect:/student/dashboard?error=Unauthorized";
        }

        model.addAttribute("exam", new Exam());
        model.addAttribute("allStudents", userService.getAllStudents());
        return "create-exam";
    }

    @PostMapping("/create")
    public String createExam(@ModelAttribute Exam exam, Model model) {
        if (exam.getMaxAttempts() < 1) {
            model.addAttribute("error", "The exam must allow at least 1 attempt!");
            return "create-exam";
        }
        examService.addExam(exam);
        return "redirect:/teacher/dashboard";
    }

    @PostMapping("/save-exam")
    public String saveExam(@ModelAttribute Exam exam, HttpServletRequest request, Model model) {
        // Updated to use centralized service method
        User loggedInTeacher = userService.getAuthenticatedUser(request);

        if (loggedInTeacher == null || !"FACULTY".equals(loggedInTeacher.getRole())) {
            return "redirect:/user/login?error=Unauthorized";
        }

        exam.setTeacher(loggedInTeacher);

        if (exam.getMaxAttempts() != null && exam.getMaxAttempts() < 1) {
            model.addAttribute("error", "The exam must allow at least 1 attempt!");
            return "create-exam";
        }

        examRepository.save(exam);
        return "redirect:/teacher/dashboard";
    }

    // --- STUDENT ACTIONS ---

    @GetMapping("/start/{id}")
    public String startExam(@PathVariable Long id, HttpServletRequest request, Model model) {
        // Updated to use centralized service method
        User user = userService.getAuthenticatedUser(request);
        if (user == null) return "redirect:/user/login";

        Exam requestedExam = examService.getExamById(id);

        long attemptsTaken = resultRepository.countByUserAndExam(user, requestedExam);
        if (attemptsTaken >= requestedExam.getMaxAttempts()) {
            return "redirect:/student/dashboard?error=limit_reached";
        }

        model.addAttribute("student", user);
        List<Question> examQuestions = questionService.getQuestionsByExamId(id);
        model.addAttribute("exam", requestedExam);
        model.addAttribute("questions", examQuestions);

        return "take-exam";
    }

    @PostMapping("/save-progress")
    @ResponseBody
    public String saveProgress(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        // Updated to use centralized service method
        User loggedInUser = userService.getAuthenticatedUser(request);

        if (loggedInUser == null) return "error: not logged in";

        Long questionId = Long.valueOf(payload.get("questionId").toString());
        String selectedOption = payload.get("selectedOption").toString();

        studentAnswerService.saveOrUpdate(loggedInUser, questionId, selectedOption);
        return "saved";
    }

    @GetMapping("/force-submit/{examId}")
    public String forceSubmitExam(@PathVariable Long examId, HttpServletRequest request) {
        // Updated to use centralized service method
        User user = userService.getAuthenticatedUser(request);
        Exam exam = examRepository.findById(examId).orElse(null);

        if (exam != null && user != null) {
            Result result = new Result();
            result.setUser(user);
            result.setExam(exam);
            result.setScore(0);
            result.setStatus("FAIL");
            result.setSubmissionTime(LocalDateTime.now());
            result.setTeacher(exam.getTeacher());
            resultRepository.save(result);
        }
        return "redirect:/student/dashboard?status=terminated";
    }
}