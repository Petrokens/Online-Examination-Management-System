package com.exam.examPortal.controller;

import com.exam.examPortal.entity.User;
import com.exam.examPortal.repository.ExamRepository;
import com.exam.examPortal.repository.ResultRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.data.domain.Page;           // Add this
import org.springframework.data.domain.PageRequest;    // Add this
import org.springframework.data.domain.Pageable;       // Add this
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ResultRepository resultRepository;

    @GetMapping("/dashboard")
    public String studentDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"STUDENT".equals(user.getRole())) {
            return "redirect:/login";
        }

        // 1. Fetch data safely
        var results = resultRepository.findByUser(user);
        var exams = examRepository.findAll();

        // 2. Safely calculate map, even if results is empty
        java.util.Map<Long, Long> attemptCountMap = results.stream()
                .filter(r -> r.getExam() != null) // Ensure result has an exam
                .collect(java.util.stream.Collectors.groupingBy(
                        r -> r.getExam().getExamId(),
                        java.util.stream.Collectors.counting()
                ));

        // 3. Add to model
        model.addAttribute("user", user);
        model.addAttribute("exams", exams);
        model.addAttribute("results", results);
        model.addAttribute("attemptCounts", attemptCountMap);

        return "student_dashboard";
    }

    // Make sure this matches the link in your HTML (th:href="@{/student/results/all}")
    @GetMapping("/results/all")
    public String viewAllResults(
            @RequestParam(defaultValue = "0") int page,
            HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null || !"STUDENT".equals(user.getRole())) {
            return "redirect:/login";
        }

        // Set page size to 10
        var pageResults = resultRepository.findByUser(user,
                PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "submissionTime")));

        model.addAttribute("results", pageResults.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResults.getTotalPages());

        return "all_results";
    }
}