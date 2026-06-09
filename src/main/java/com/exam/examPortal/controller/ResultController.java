package com.exam.examPortal.controller;

import com.exam.examPortal.entity.Exam;
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

import java.time.LocalDateTime;
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

        // 3. --- FIX: FETCH THE EXAM OBJECT FIRST ---
        Exam exam = examRepository.findById(examId).orElse(null);

        // 3. Get the start time we saved in Step 2
        LocalDateTime startTime = (LocalDateTime) session.getAttribute("examStartTime");
        LocalDateTime endTime = LocalDateTime.now();

        // 4. Calculate the difference
        long duration = java.time.Duration.between(startTime, endTime).getSeconds();

        Result result = new Result();
        result.setUser(user);
        result.setExam(examRepository.findById(examId).orElse(null));
        result.setSubmissionTime(endTime);
        result.setTimeTakenSeconds(duration); // NOW we have the length!

        if (exam != null) {
            result.setTeacher(exam.getTeacher());
        }

        // 3. Now the list is NOT empty, the Grading Engine will run!
        resultService.calculateAndSaveResult(result, examAnswers);

        return "redirect:/result/view/" + result.getResultId();
    }

    @GetMapping("/view/{id}")
    public String viewResult(@PathVariable Long id, HttpSession session, Model model) {
        // 1. Get the result AND the logged-in teacher
        Result result = resultRepository.findById(id).orElse(null);
        User loggedInTeacher = (User) session.getAttribute("user");

        // SAFETY FIRST:
        // 1. Check if result exists
        // 2. Check if result.getTeacher() is NOT null before calling .equals()
        if (result == null) {
            return "redirect:/teacher/dashboard?error=NotFound";
        }

        // This logic handles the null teacher safely
        boolean isOwner = (result.getTeacher() != null && result.getTeacher().equals(loggedInTeacher));

        // Also allow the STUDENT who took the exam to view their own result
        boolean isStudent = (result.getUser() != null && result.getUser().equals(loggedInTeacher));

        if (!isOwner && !isStudent) {
            return "redirect:/teacher/dashboard?error=Unauthorized";
        }

        model.addAttribute("result", result);
        return "result";
    }
}