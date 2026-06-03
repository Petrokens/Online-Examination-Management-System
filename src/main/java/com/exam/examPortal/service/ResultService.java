package com.exam.examPortal.service;

import com.exam.examPortal.entity.Result;
import com.exam.examPortal.entity.StudentAnswer;
import com.exam.examPortal.repository.ResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResultService {

    @Autowired
    private ResultRepository resultRepository;

    // Notice we now ask for the Result AND the List of all their answers
    public Result calculateAndSaveResult(Result studentResult, List<StudentAnswer> studentAnswers) {

        // --- 1. THE AUTO-GRADER LOOP ---
        int calculatedScore = 0;

        for (StudentAnswer answer : studentAnswers) {
            String selected = answer.getSelectedOption();
            String correct = answer.getQuestion().getCorrectAnswer();

            // Safety check: Make sure neither answer is blank before comparing
            if (selected != null && correct != null) {
                // .trim() chops off accidental spaces
                // .equalsIgnoreCase() makes "True" match "true"
                if (selected.trim().equalsIgnoreCase(correct.trim())) {
                    // It's a match! Add the specific marks for this question
                    calculatedScore += answer.getQuestion().getMarks();
                }
            }
        }

        // Lock in the final calculated score
        studentResult.setScore(calculatedScore);

        // --- 2. THE PERCENTAGE MATH ---
        int totalMarks = studentResult.getExam().getTotalMarks();

        // Calculate the percentage
        double calculatedPercentage = ((double) calculatedScore / totalMarks) * 100;
        studentResult.setPercentage((int)calculatedPercentage);

        // --- 3. THE PASS/FAIL DECISION ---
        double requiredToPass = studentResult.getExam().getPassingPercentage();

        if (calculatedPercentage >= requiredToPass) {
            studentResult.setStatus("PASS");
        } else {
            studentResult.setStatus("FAIL");
        }

        // Automatically stamp the exact date and time
        studentResult.setSubmissionTime(LocalDateTime.now());

        // --- 4. THE TRIGGER ---
        return resultRepository.save(studentResult);
    }
}