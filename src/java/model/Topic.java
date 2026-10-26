package model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents a topic proposal in NTMCS Workflow 1 (Reporter).
 */
public class Topic implements Serializable {
    private int topicId;
    private String topicCode;
    private String title;
    private String summary;
    private String angle;
    private String genre;
    private String priority;
    private LocalDate deadline;
    private String category;
    private String desk;
    private String status;
    private String reporterEmail;
    private String reporterName;
    private LocalDate createdDate;
    private String rejectionReason;

    public Topic() { }

    public Topic(int topicId, String topicCode, String title, String summary, String angle, String genre,
                 String priority, LocalDate deadline, String category, String desk, String status,
                 String reporterEmail, String reporterName, LocalDate createdDate, String rejectionReason) {
        this.topicId = topicId;
        this.topicCode = topicCode;
        this.title = title;
        this.summary = summary;
        this.angle = angle;
        this.genre = genre;
        this.priority = priority;
        this.deadline = deadline;
        this.category = category;
        this.desk = desk;
        this.status = status;
        this.reporterEmail = reporterEmail;
        this.reporterName = reporterName;
        this.createdDate = createdDate;
        this.rejectionReason = rejectionReason;
    }

    public Topic(int topicId, String title, String summary, String angle, String genre,
                 String priority, LocalDate deadline, String category, String status,
                 String reporterEmail, LocalDate createdDate) {
        this(topicId, null, title, summary, angle, genre, priority, deadline, category, null, status, reporterEmail, null, createdDate, null);
    }

    public int getTopicId() { return topicId; }
    public void setTopicId(int topicId) { this.topicId = topicId; }

    public String getTopicCode() { return topicCode; }
    public void setTopicCode(String topicCode) { this.topicCode = topicCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getAngle() { return angle; }
    public void setAngle(String angle) { this.angle = angle; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDesk() { return desk; }
    public void setDesk(String desk) { this.desk = desk; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReporterEmail() { return reporterEmail; }
    public void setReporterEmail(String reporterEmail) { this.reporterEmail = reporterEmail; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
