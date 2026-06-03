package com.exam.examPortal.service;

import com.exam.examPortal.entity.Exam;
import com.exam.examPortal.repository.ExamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamService {

    @Autowired
    private ExamRepository examRepository;

    // 1. CREATE: The Manager saves a brand new exam
    public Exam addExam(Exam newExam) {
        return examRepository.save(newExam);
    }

    // 2. READ ALL: The Manager fetches the list for the dashboard
    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    // 3. READ ONE (NEW!): The Manager fetches ONE specific exam when a student clicks it
    public Exam getExamById(Long examId) {
        // findById returns an "Optional" safety box.
        // .orElse(null) tells Java: "If Exam #5 doesn't exist, just return nothing instead of crashing."
        return examRepository.findById(examId).orElse(null);
    }

    // 4. DELETE (NEW!): The Manager tells the worker to delete a specific exam
    public void deleteExam(Long examId) {
        examRepository.deleteById(examId);
    }
}