package com.exam.examPortal.service;

import com.exam.examPortal.entity.Exam;
import com.exam.examPortal.entity.Question;
import com.exam.examPortal.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    public void saveQuestion(Question question) {
        questionRepository.save(question);
    }

    public List<Question> getQuestionsByExamId(Long examId) {
        return questionRepository.findByExam_ExamId(examId);
    }
}