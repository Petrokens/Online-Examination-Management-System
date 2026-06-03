package com.exam.examPortal.controller;

import com.exam.examPortal.entity.Result;
import com.exam.examPortal.service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/result")
public class ResultController {

    @Autowired
    private ResultService resultService;

    // --- SUBMITTING THE EXAM ---

    // 1. CATCH THE ANSWERS: When the student clicks "Submit Test"
    @PostMapping("/submit")
    public String submitExam() {

        // TODO: We will catch the student's selected answers from the HTML form here.
        // Once we have them, we will pass them to the Auto-Grader like this:
        // Result savedResult = resultService.calculateAndSaveResult(studentResult, studentAnswers);

        // After grading is done, instantly redirect the student to see their score!
        return "redirect:/result/view/1"; // (Using a placeholder ID '1' for now)
    }


    // --- VIEWING THE SCORE ---

    // 2. SHOW THE REPORT CARD: Display the Pass/Fail and Percentage
    @GetMapping("/view/{id}")
    public String viewResult(@PathVariable Long id, Model model) {

        // TODO: Later, we will use the ID to fetch their actual grade from the database.
        // For now, we are just telling the Host where to send the user.

        return "result"; // Tells Thymeleaf to look for 'result.html'
    }
}