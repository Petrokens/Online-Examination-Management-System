package com.exam.examPortal.controller;
import com.exam.examPortal.entity.*;
import com.exam.examPortal.repository.ExamRepository;
import com.exam.examPortal.repository.ResultRepository;
import com.exam.examPortal.repository.StudentAnswerRepository;
import com.exam.examPortal.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpSession;

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
    private ResultRepository resultRepository; // Spring connects this automatically

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository; // Spring connects this automatically

    // --- DASHBOARD ---


    // --- TEACHER ACTIONS ---

    // 1. SHOW CREATION FORM: The blank page where a teacher builds an exam
    @GetMapping("/create")
    public String showCreateExamForm(Model model) {
        // Put an empty Exam object on the tray so the HTML form has a blueprint
        model.addAttribute("exam", new Exam());
        // Add this line to fetch all students so the checklist has data
        // Assuming you have a userService that can return all students
        model.addAttribute("allStudents", userService.getAllStudents());
        return "create-exam"; // Looks for create-exam.html
    }

    // 2. CATCH NEW EXAM DATA: When the teacher clicks "Save Exam"
    @PostMapping("/create")
    public String createExam(@ModelAttribute Exam exam, Model model) {
        if (exam.getMaxAttempts() < 1) {
            // Add an error message to show on the page
            model.addAttribute("error", "The exam must allow at least 1 attempt!");
            return "create-exam";
        }
        // Hand the data to the Chef to save in MySQL
        examService.addExam(exam);

        // Success! Jump straight back to the dashboard to see the updated list
        return "redirect:/teacher/dashboard";
    }

    @PostMapping("/save-exam")
    public String saveExam(@ModelAttribute Exam exam, HttpSession session, Model model) {
        // 1. Get the current logged-in teacher from the session
        User loggedInTeacher = (User) session.getAttribute("user");

        // 2. SAFETY CHECK: Ensure the user is actually a faculty member
        if (loggedInTeacher == null || !"FACULTY".equals(loggedInTeacher.getRole())) {
            return "redirect:/user/login?error=Unauthorized";
        }

        // 3. THE MISSING LINK: Stamp the teacher onto the exam object!
        exam.setTeacher(loggedInTeacher);

        // 4. Validate
        if (exam.getMaxAttempts() != null && exam.getMaxAttempts() < 1) {
            model.addAttribute("error", "The exam must allow at least 1 attempt!");
            return "create-exam";
        }

        // 5. Now save. Because we set the teacher in step 3,
        // the teacher_id will no longer be NULL.
        examRepository.save(exam);

        return "redirect:/teacher/dashboard";
    }



    // --- STUDENT ACTIONS ---

    // 4. START AN EXAM: When a student clicks "Take Exam #5"
    @GetMapping("/start/{id}")
    public String startExam(@PathVariable Long id, HttpSession session,Model model) {
        User user = (User) session.getAttribute("user");
        Exam requestedExam = examService.getExamById(id);

        // --- ADD THIS CHECK ---
        long attemptsTaken = resultRepository.countByUserAndExam(user, requestedExam);
        if (attemptsTaken >= requestedExam.getMaxAttempts()) {
            return "redirect:/student/dashboard?error=limit_reached";
        }
        // -----------------------

        List<Question> examQuestions = questionService.getQuestionsByExamId(id);
        model.addAttribute("exam", requestedExam);
        model.addAttribute("questions", examQuestions);

        // Set the session lock so they can't leave
        session.setAttribute("isExamInProgress", true);
        session.setAttribute("currentExamId", id);
        session.setAttribute("examStartTime", LocalDateTime.now());

        return "take-exam";
    }


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

    @GetMapping("/force-submit")
    public String forceSubmitExam(HttpSession session) {
        Long examId = (Long) session.getAttribute("currentExamId");
        User user = (User) session.getAttribute("user");

        if (examId != null && user != null) {
            Exam exam = examRepository.findById(examId).orElse(null);

            LocalDateTime startTime = (LocalDateTime) session.getAttribute("examStartTime");
// If the session expired (startTime is null), set duration to 0 or "Unknown"
            long duration = (startTime != null) ? java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds() : 0;

            // Create a direct failing result instead of calculating their empty answers
            Result result = new Result();
            result.setUser(user);
            result.setExam(exam);
            result.setScore(0);           // Hardcode zero marks
            result.setScore(0);   // Hardcode zero percent
            result.setStatus("FAIL");      // Or "DISQUALIFIED" if your DB column supports it
            result.setSubmissionTime(java.time.LocalDateTime.now());
            result.setTeacher(exam.getTeacher());
            result.setTimeTakenSeconds(duration);
            // Directly save to the repository, bypassing the service calculations
            resultRepository.save(result);

            // Cleanup session locks
            session.removeAttribute("isExamInProgress");
            session.removeAttribute("currentExamId");
        }

        return "redirect:/student/dashboard?status=terminated";
    }
}