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
        int calculatedScore = 0;
        int actualTotalMarks = 0; // NEW: track the sum of marks

        for (StudentAnswer answer : studentAnswers) {
            String selected = answer.getSelectedOption();
            String correct = (answer.getQuestion() != null) ? answer.getQuestion().getCorrectAnswer() : "";

            // Sum the marks of EVERY question in the exam
            actualTotalMarks += answer.getQuestion().getMarks();

            String cleanSelected = (selected != null) ? selected.trim().toUpperCase() : "";
            String cleanCorrect = (correct != null) ? correct.trim().toUpperCase() : "";

            if (cleanSelected.equals(cleanCorrect) && !cleanSelected.isEmpty()) {
                calculatedScore += answer.getQuestion().getMarks();
            }
        }

        studentResult.setScore(calculatedScore);

        // Use the dynamic total instead of the one from the database
        double calculatedPercentage = (actualTotalMarks > 0) ? ((double) calculatedScore / actualTotalMarks) * 100.0 : 0.0;
        studentResult.setPercentage((int) Math.round(calculatedPercentage));

        // Pass/Fail Logic
        double requiredToPass = studentResult.getExam().getPassingPercentage();
        studentResult.setStatus(calculatedPercentage >= requiredToPass ? "PASS" : "FAIL");

        studentResult.setSubmissionTime(LocalDateTime.now());
        return resultRepository.save(studentResult);
    }
}