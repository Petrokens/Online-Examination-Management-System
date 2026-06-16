package com.exam.examPortal.controller;

import com.exam.examPortal.entity.User;
import com.exam.examPortal.repository.ExamRepository;
import com.exam.examPortal.repository.ResultRepository;
import com.exam.examPortal.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private com.exam.examPortal.security.JwtUtils jwtUtils;

    @GetMapping("/dashboard")
    public String studentDashboard(
            @RequestParam(name = "status", required = false) String status,
            HttpServletRequest request,
            Model model) {

        // 1. Use centralized authentication
        User user = userService.getAuthenticatedUser(request);

        // 2. Validate User and Role
        if (user == null || !"STUDENT".equals(user.getRole())) {
            return "redirect:/user/login";
        }

        // 3. Data Fetching
        var pageable = PageRequest.of(0, 100, Sort.by("submissionTime").descending());
        var results = resultRepository.findByUser(user, pageable);
        var exams = examRepository.findAll();

        java.util.Map<Long, Long> attemptCountMap = results.stream()
                .filter(r -> r.getExam() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        r -> r.getExam().getExamId(),
                        java.util.stream.Collectors.counting()
                ));

        model.addAttribute("user", user);
        model.addAttribute("exams", exams);
        model.addAttribute("results", results);
        model.addAttribute("attemptCounts", attemptCountMap);
        model.addAttribute("status", status);

        return "student_dashboard";
    }

    @GetMapping("/results/all")
    public String viewAllResults(
            @RequestParam(defaultValue = "0") int page,
            HttpServletRequest request, // REQUIRED to get the token
            Model model) {

        User user = userService.getAuthenticatedUser(request);
        if (user == null || !"STUDENT".equals(user.getRole())) {
            return "redirect:/user/login?error=Unauthorized";
        }

        // 3. APPLY PAGINATION
        // We create the Pageable object here using the 'page' parameter
        var pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "submissionTime"));

        // 4. Call the repository with the pageable object
        var pageResults = resultRepository.findByUser(user, pageable);

        // 5. Add to Model
        model.addAttribute("results", pageResults.getContent()); // Data for this page
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResults.getTotalPages());

        return "all_results";
    }

}