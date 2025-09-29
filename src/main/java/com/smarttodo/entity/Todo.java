package com.smarttodo.entity;

import jakarta.annotation.Priority;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "todos")
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Titulo é obrigatorio")
    @Size(max = 200)
    @Column(nullable = false)
    private String title;

    @Size(max = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "required_mood")
    private String requiredMood;

    @Column(name = "location_context")
    private String locationContext;

    @Column(name = "estimated_energy")
    private Integer estimatedEnergy;

    public Todo(){}
    public Todo(String title, String description, Priority priority){
        this.title = title;
        this.description = description;
        this.priority = priority;
    }

    @Column(name = "auto_adjusted")
    private Boolean autoAdjusted = false;

    @Column(name = "last_mood_check")
    private LocalDateTime lastMoodCheck;

    @Column(name = "mood_compatibility_score")
    private Double moodCompatibilityScore = 0.0;

    @Column(name = "optimal_environment")
    private String optimalEnvironment; // "quiet", "collaborative", "bright", "any"

    @Column(name = "cognitive_load")
    private Integer cognitiveLoad; // 1-5 (1=simples, 5=complexo)

    @Column(name = "suggested_order")
    private Integer suggestedOrder = 0;

    public enum Priority{
        LOW, MEDIUM, HIGH, URGENT
    }

    public enum TaskStatus{
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getRequiredMood() {
        return requiredMood;
    }

    public void setRequiredMood(String requiredMood) {
        this.requiredMood = requiredMood;
    }

    public String getLocationContext() {
        return locationContext;
    }

    public void setLocationContext(String locationContext) {
        this.locationContext = locationContext;
    }

    public Integer getEstimatedEnergy() {
        return estimatedEnergy;
    }

    public void setEstimatedEnergy(Integer estimatedEnergy) {
        this.estimatedEnergy = estimatedEnergy;
    }

    public Boolean getAutoAdjusted() {
        return autoAdjusted;
    }

    public void setAutoAdjusted(Boolean autoAdjusted) {
        this.autoAdjusted = autoAdjusted;
    }

    public LocalDateTime getLastMoodCheck() {
        return lastMoodCheck;
    }

    public void setLastMoodCheck(LocalDateTime lastMoodCheck) {
        this.lastMoodCheck = lastMoodCheck;
    }

    public Double getMoodCompatibilityScore() {
        return moodCompatibilityScore;
    }

    public void setMoodCompatibilityScore(Double moodCompatibilityScore) {
        this.moodCompatibilityScore = moodCompatibilityScore;
    }

    public String getOptimalEnvironment() {
        return optimalEnvironment;
    }

    public void setOptimalEnvironment(String optimalEnvironment) {
        this.optimalEnvironment = optimalEnvironment;
    }

    public Integer getCognitiveLoad() {
        return cognitiveLoad;
    }

    public void setCognitiveLoad(Integer cognitiveLoad) {
        this.cognitiveLoad = cognitiveLoad;
    }

    public Integer getSuggestedOrder() {
        return suggestedOrder;
    }

    public void setSuggestedOrder(Integer suggestedOrder) {
        this.suggestedOrder = suggestedOrder;
    }
}
