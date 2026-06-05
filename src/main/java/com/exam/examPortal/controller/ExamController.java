package com.exam.examPortal.controller;
import com.exam.examPortal.entity.User;
import com.exam.examPortal.service.StudentAnswerService;
import com.exam.examPortal.service.UserService;
import com.exam.examPortal.entity.Exam;
import com.exam.examPortal.entity.Question;
import com.exam.examPortal.entity.StudentAnswer;
import com.exam.examPortal.service.ExamService;
import com.exam.examPortal.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpSession;
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
    private StudentAnswerService studentAnswerService;

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

// ... inside your ExamController class ...

    @PostMapping("/save-progress")
    @ResponseBody
    public String saveProgress(@RequestBody Map<String, Object> payload, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user");

        // LOGGING: This will print to your IDE console (where the server runs)
        System.out.println("DEBUG: Received payload: " + payload);
        System.out.println("DEBUG: Logged in user: " + (loggedInUser != null ? loggedInUser.getEmail() : "NULL"));

        if (loggedInUser == null) {
            return "error: not logged in";
        }

        Long questionId = Long.valueOf(payload.get("questionId").toString());
        String selectedOption = payload.get("selectedOption").toString();

        studentAnswerService.saveOrUpdate(loggedInUser, questionId, selectedOption);

        return "saved";
    }
}