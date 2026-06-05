package com.exam.examPortal.controller;

import com.exam.examPortal.entity.Result;
import com.exam.examPortal.entity.StudentAnswer;
import com.exam.examPortal.entity.User;
import com.exam.examPortal.repository.ExamRepository;
import com.exam.examPortal.repository.ResultRepository;
import com.exam.examPortal.repository.StudentAnswerRepository;
import com.exam.examPortal.service.ResultService;
import jakarta.servlet.http.HttpSession;
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
    private StudentAnswerRepository studentAnswerRepository; // Need this to find their answers

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ResultRepository resultRepository;

    @PostMapping("/submit/{examId}")
    public String submitExam(@PathVariable Long examId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // 1. Get ALL answers for this user
        List<StudentAnswer> allAnswers = studentAnswerRepository.findByUser(user);

        // 2. Filter them manually for the specific Exam ID
        // This uses the Question entity, which is definitely populated
        List<StudentAnswer> examAnswers = allAnswers.stream()
                .filter(a -> a.getQuestion() != null && a.getQuestion().getExam().getExamId().equals(examId))
                .toList();

        System.out.println("DEBUG: Filtered " + examAnswers.size() + " answers for exam " + examId);

        Result result = new Result();
        result.setUser(user);
        result.setExam(examRepository.findById(examId).orElse(null));

        // 3. Now the list is NOT empty, the Grading Engine will run!
        resultService.calculateAndSaveResult(result, examAnswers);

        return "redirect:/result/view/" + result.getResultId();
    }

    @GetMapping("/view/{id}")
    public String viewResult(@PathVariable Long id, Model model) {
        Result result = resultRepository.findById(id).orElse(null);
        model.addAttribute("result", result);
        return "result";
    }
}