package com.exam.examPortal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student_answers")
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    // --- RELATIONSHIP 1: WHO ANSWERED IT? ---
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // --- RELATIONSHIP 2: WHAT QUESTION IS IT? ---
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // The option they clicked (e.g., "A", "B", "C", or "D")
    private String selectedOption;

    // --- GETTERS AND SETTERS ---

    public Long getAnswerId() { return answerId; }
    public void setAnswerId(Long answerId) { this.answerId = answerId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public String getSelectedOption() { return selectedOption; }
    public void setSelectedOption(String selectedOption) { this.selectedOption = selectedOption; }
}