package com.exam.examPortal.controller;

import java.util.List;
import com.exam.examPortal.entity.Exam;
import com.exam.examPortal.entity.Question;
import com.exam.examPortal.entity.User;
import com.exam.examPortal.repository.QuestionRepository;
import com.exam.examPortal.service.ExamService;
import com.exam.examPortal.service.QuestionService;
import com.exam.examPortal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private ExamService examService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserService userService;

    private boolean isNotAuthorized(HttpServletRequest request) {
        User user = userService.getAuthenticatedUser(request);
        return user == null || !"FACULTY".equals(user.getRole());
    }

    // 1. SHOW THE FORM (Using your custom repository method!)
    @GetMapping("/add/{examId}")
    public String showAddQuestionForm(@PathVariable("examId") Long examId, Model model, HttpServletRequest request) {
        if (isNotAuthorized(request)) return "redirect:/user/login?error=Unauthorized";

        Exam exam = examService.getExamById(examId);
        Question question = new Question();

        // Pass the examId directly to your new service method
        List<Question> existingQuestions = questionService.getQuestionsByExamId(examId);

        int nextQuestionNumber = existingQuestions.size() + 1;

        model.addAttribute("exam", exam);
        model.addAttribute("question", question);
        model.addAttribute("questionNumber", nextQuestionNumber);

        model.addAttribute("questions", existingQuestions);

        return "add-question";
    }

    // 2. SAVE THE QUESTION TO THE DATABASE
    @PostMapping("/add/{examId}")
    public String saveQuestion(@PathVariable("examId") Long examId, @ModelAttribute Question question, HttpServletRequest request) {
        if (isNotAuthorized(request)) return "redirect:/user/login?error=Unauthorized";
        Exam exam = examService.getExamById(examId);

        // Link the question to the exam!
        question.setExam(exam);

        questionService.saveQuestion(question);

        // Redirect back to the same form so the teacher can quickly add another
        return "redirect:/question/add/" + examId;
    }
    @GetMapping("/edit/{questionId}")
    public String showEditQuestionForm(@PathVariable Long questionId, Model model, HttpServletRequest request) {
        if (isNotAuthorized(request)) return "redirect:/user/login?error=Unauthorized";
        Question question = questionRepository.findById(questionId).orElseThrow();

        Exam exam = question.getExam();

        List<Question> existingQuestions =
                questionService.getQuestionsByExamId(exam.getExamId());

        model.addAttribute("question", question);
        model.addAttribute("exam", exam);
        model.addAttribute("questions", existingQuestions);
        model.addAttribute("questionNumber", existingQuestions.size());

        return "add-question";
    }

    @PostMapping("/update")
    public String updateQuestion(@ModelAttribute Question question, HttpServletRequest request) {
        if (isNotAuthorized(request)) return "redirect:/user/login?error=Unauthorized";
        questionRepository.save(question);
        // Redirect back to that specific exam's question list
        return "redirect:/question/add/" + question.getExam().getExamId();
    }
}