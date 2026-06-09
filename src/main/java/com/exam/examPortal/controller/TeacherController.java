    package com.exam.examPortal.controller;

    import com.exam.examPortal.entity.Exam;
    import com.exam.examPortal.entity.Result;
    import com.exam.examPortal.entity.User;
    import com.exam.examPortal.repository.ExamRepository;
    import com.exam.examPortal.repository.ResultRepository;
    import com.exam.examPortal.service.UserService;
    import jakarta.servlet.http.HttpSession;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.domain.*;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.data.domain.Pageable;

    import java.util.List;

    @Controller
    @RequestMapping("/teacher")
    public class TeacherController {

        @Autowired
        private ExamRepository examRepository;

        @Autowired
        private ResultRepository resultRepository;

        @Autowired
        private UserService userService;

        // TEACHER DASHBOARD: View all created exams
        @GetMapping("/dashboard")
        public String showTeacherDashboard(HttpSession session, Model model) {
            // 1. Fetch user from session to ensure security lockdown
            User loggedInUser = (User) session.getAttribute("user");

            // 2. Security Guard: If not logged in, or if they are NOT FACULTY, kick them out
            if (loggedInUser == null || !"FACULTY".equals(loggedInUser.getRole())) {
                return "redirect:/user/login?error=AccessDenied";
            }

            // 3. ADD THE USER TO THE MODEL
            model.addAttribute("teacher", loggedInUser);

            // --- CHANGED FROM .findAll() TO .findByTeacher() ---
            // This ensures they only see exams with their "stamp" on them
            List<Exam> myExams = examRepository.findByTeacher(loggedInUser);
            model.addAttribute("exams", myExams);

            // 5. Look for teacher_dashboard.html template
            return "teacher_dashboard";
        }

        @GetMapping("/delete/{id}")
        public String deleteExam(@PathVariable Long id, HttpSession session) {
            // Security Check
            User loggedInUser = (User) session.getAttribute("user");
            Exam exam = examRepository.findById(id).orElse(null);

            // Security Guard: Does the exam exist AND does it belong to this teacher?
            if (exam == null || !exam.getTeacher().equals(loggedInUser)) {
                return "redirect:/teacher/dashboard?error=Unauthorized";
            }
            // Delete the exam
            examRepository.deleteById(id);

            // Redirect back to dashboard
            return "redirect:/teacher/dashboard";
        }

        // 1. Show the Edit Form using the correct, existing template
        @GetMapping("/edit/{id}")
        public String showEditExamForm(@PathVariable Long id, Model model) {
            // Find the exam, or fallback to null safely
            Exam exam = examRepository.findById(id).orElse(null);

            if (exam == null) {
                return "redirect:/teacher/dashboard?error=ExamNotFound";
            }

            model.addAttribute("exam", exam);

            // --- ADD THIS LINE ---
            // You must fetch the students again so the checklist populates
            model.addAttribute("allStudents", userService.getAllStudents());

            return "create-exam"; // Reuses the template you modified!
        }

        // 2. Save the Updates (Unified route for handling both inserts and updates)
        @PostMapping("/save-exam")
        public String saveExam(@ModelAttribute Exam exam, Model model) {
            if (exam.getMaxAttempts() != null && exam.getMaxAttempts() < 1) {
                model.addAttribute("error", "The exam must allow at least 1 attempt!");
                return "create-exam";
            }

            // If examId exists, Hibernate automatically runs an UPDATE query
            // If examId is null, Hibernate runs an INSERT query
            examRepository.save(exam);
            return "redirect:/teacher/dashboard";
        }

        @PostMapping("/edit-exam/{id}")
        public String updateExam(@PathVariable Long id, @ModelAttribute Exam updatedExam, HttpSession session) {
            // 1. Fetch the existing exam from the DB so we have the 'teacher' object intact
            Exam existingExam = examRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid exam ID:" + id));

            // 2. Update the fields that came from the form (manually copying them)
            existingExam.setExamName(updatedExam.getExamName());
            existingExam.setDurationMinutes(updatedExam.getDurationMinutes());
            existingExam.setTotalMarks(updatedExam.getTotalMarks());
            existingExam.setPassingPercentage(updatedExam.getPassingPercentage());
            existingExam.setMaxAttempts(updatedExam.getMaxAttempts());

            // 3. Update the Whitelist (the checkbox list)
            existingExam.setAllowedStudents(updatedExam.getAllowedStudents());

            // 4. IMPORTANT: We do NOT call setTeacher() here because
            // existingExam already has the correct teacher linked from the database!

            // 5. Save the merged object
            examRepository.save(existingExam);

            return "redirect:/teacher/dashboard";
        }

        @GetMapping("/results")
        public String viewTeacherResults(
                @RequestParam Long examId,
                @RequestParam(defaultValue = "0") int page,
                HttpSession session,
                Model model) {

            User teacher = (User) session.getAttribute("user");
            Exam exam = examRepository.findById(examId).orElse(null);

            // Security: Ensure this exam belongs to this teacher!
            if (exam == null || !exam.getTeacher().equals(teacher)) {
                return "redirect:/teacher/dashboard?error=Unauthorized";
            }

            // Create a page request (10 results per page, sorted by time)Pageable pageable = PageRequest.of(page, 10, Sort.by("submissionTime").descending());
            Pageable pageable = PageRequest.of(page, 10, Sort.by("submissionTime").descending());
            // FETCH ONLY STUDENTS (Filter out teachers from the report)
            Page<Result> resultPage = resultRepository.findByExamAndTeacherAndUserRole(
                    exam, teacher, "STUDENT", pageable);
            model.addAttribute("resultPage", resultPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("exam", exam); // Pass exam to show the title in the view

            return "teacher_results";
        }


    }
