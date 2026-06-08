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
    public String studentDashboard(
            @RequestParam(name = "status", required = false) String status, // <--- ADD THIS PARAMETER
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null || !"STUDENT".equals(user.getRole())) {
            return "redirect:/login";
        }

        // 1. Fetch data
        var results = resultRepository.findByUser(user, Sort.by(Sort.Direction.DESC, "submissionTime"));
        var exams = examRepository.findAll();

        // 2. Calculate map
        java.util.Map<Long, Long> attemptCountMap = results.stream()
                .filter(r -> r.getExam() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        r -> r.getExam().getExamId(),
                        java.util.stream.Collectors.counting()
                ));

        // 3. Add to model
        model.addAttribute("user", user);
        model.addAttribute("exams", exams);
        model.addAttribute("results", results);
        model.addAttribute("attemptCounts", attemptCountMap);
        model.addAttribute("status", status); // <--- ADD THIS LINE TO PASS IT TO THE HTML

        return "student_dashboard";
    }
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

        return "all_results"; // Ensure you have all_results.html in your templates folder
    }
}