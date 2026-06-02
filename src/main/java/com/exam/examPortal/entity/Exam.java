package com.exam.examPortal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examId;

    private String examName;

    // We will store the duration in minutes (e.g., 60 for an hour)
    private int durationMinutes;

    private int totalMarks;

    // Added from FR-6 in your SRS
    private int passingPercentage;

    // --- GETTERS AND SETTERS ---

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public int getPassingPercentage() { return passingPercentage; }
    public void setPassingPercentage(int passingPercentage) { this.passingPercentage = passingPercentage; }
}