package com.exam.examPortal.controller;

import com.exam.examPortal.entity.Exam;
import com.exam.examPortal.entity.Question;
import com.exam.examPortal.service.ExamService;
import com.exam.examPortal.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private QuestionService questionService;

    // --- DASHBOARD ---

    // 1. SHOW ALL EXAMS: When a user logs in and goes to the dashboard
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // The Waiter asks the Chef for the complete list of exams
        List<Exam> allExams = examService.getAllExams();

        // The Waiter puts the entire list on the tray for Thymeleaf
        model.addAttribute("exams", allExams);

        return "dashboard"; // Tells Thymeleaf to look for dashboard.html
    }

    // --- TEACHER ACTIONS ---

    // 2. SHOW CREATION FORM: The blank page where a teacher builds an exam
    @GetMapping("/create")
    public String showCreateExamForm(Model model) {
        // Put an empty Exam object on the tray so the HTML form has a blueprint
        model.addAttribute("exam", new Exam());
        return "create-exam"; // Looks for create-exam.html
    }

    // 3. CATCH NEW EXAM DATA: When the teacher clicks "Save Exam"
    @PostMapping("/create")
    public String createExam(@ModelAttribute Exam exam) {
        // Hand the data to the Chef to save in MySQL
        examService.addExam(exam);

        // Success! Jump straight back to the dashboard to see the updated list
        return "redirect:/exam/dashboard";
    }

    // --- STUDENT ACTIONS ---

    // 4. START AN EXAM: When a student clicks "Take Exam #5"
    @GetMapping("/start/{id}")
    public String startExam(@PathVariable Long id, Model model) {
        // 1. Grab the specific exam from the database using the ID in the URL
        Exam requestedExam = examService.getExamById(id);

        // 2. NEW: Grab all the questions for this specific exam
        // (Note: Make sure your method name here matches what you wrote in QuestionService!)
        List<Question> examQuestions = questionService.getQuestionsByExamId(id);

        // 3. Put BOTH the exam and the questions on the tray
        model.addAttribute("exam", requestedExam);
        model.addAttribute("questions", examQuestions); // THIS FIXES THE 500 ERROR

        return "take-exam"; // Looks for take-exam.html
    }
}