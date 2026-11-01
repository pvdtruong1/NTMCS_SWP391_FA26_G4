package model;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Entity representing the Topic aggregate root.
 * Supports Editorial Workflow (WF-02), Governance (WF-05), and Reporter Workflow (WF-01).
 */
public class Topic implements Serializable {
    private static final long serialVersionUID = 1L;

    // Database columns (Core schema)
    private int topicId;
    private String topicCode;
    private String title;
    private String angle;
    private String status;
    private Date deadline;
    private int categoryId;
    private int authorUserId;

    // Database columns (WF-01 Extension approved in SPEC)
    private String summary;
    private String genre;
    private String priority = "NORMAL";
    private Timestamp createdDate;

    // View-helper fields (Mapped via JOINs, not direct columns)
    private String categoryName;
    private String deskName;
    private String reporterName;
    private String reporterEmail;
    private String rejectionReason;

    public Topic() {
    }

    public Topic(int topicId, String topicCode, String title, String angle, String status, Date deadline, int categoryId, int authorUserId) {
        this.topicId = topicId;
        this.topicCode = topicCode;
        this.title = title;
        this.angle = angle;
        this.status = status;
        this.deadline = deadline;
        this.categoryId = categoryId;
        this.authorUserId = authorUserId;
    }

    public Topic(String topicCode, String title, String angle, String status, Date deadline, int categoryId, int authorUserId) {
        this.topicCode = topicCode;
        this.title = title;
        this.angle = angle;
        this.status = status;
        this.deadline = deadline;
        this.categoryId = categoryId;
        this.authorUserId = authorUserId;
    }

    // Extended constructor for Reporter workflows
    public Topic(int topicId, String topicCode, String title, String summary, String angle, String genre,
                 String priority, Date deadline, int categoryId, int authorUserId, String status,
                 Timestamp createdDate, String rejectionReason) {
        this.topicId = topicId;
        this.topicCode = topicCode;
        this.title = title;
        this.summary = summary;
        this.angle = angle;
        this.genre = genre;
        this.priority = priority != null ? priority : "NORMAL";
        this.deadline = deadline;
        this.categoryId = categoryId;
        this.authorUserId = authorUserId;
        this.status = status;
        this.createdDate = createdDate;
        this.rejectionReason = rejectionReason;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public String getTopicCode() {
        return topicCode;
    }

    public void setTopicCode(String topicCode) {
        this.topicCode = topicCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getAngle() {
        return angle;
    }

    public void setAngle(String angle) {
        this.angle = angle;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPriority() {
        return priority != null ? priority : "NORMAL";
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public void setDeadline(LocalDate localDate) {
        this.deadline = localDate != null ? Date.valueOf(localDate) : null;
    }

    public LocalDate getDeadlineLocalDate() {
        return deadline != null ? deadline.toLocalDate() : null;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getAuthorUserId() {
        return authorUserId;
    }

    public void setAuthorUserId(int authorUserId) {
        this.authorUserId = authorUserId;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public void setCreatedDate(LocalDate localDate) {
        this.createdDate = localDate != null ? Timestamp.valueOf(localDate.atStartOfDay()) : null;
    }

    public LocalDate getCreatedDateLocalDate() {
        return createdDate != null ? createdDate.toLocalDateTime().toLocalDate() : null;
    }

    // View-helper getters and setters
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategory() {
        return categoryName;
    }

    public void setCategory(String category) {
        this.categoryName = category;
    }

    public String getDeskName() {
        return deskName;
    }

    public void setDeskName(String deskName) {
        this.deskName = deskName;
    }

    public String getDesk() {
        return deskName;
    }

    public void setDesk(String desk) {
        this.deskName = desk;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "topicId=" + topicId +
                ", topicCode='" + topicCode + '\'' +
                ", title='" + title + '\'' +
                ", angle='" + angle + '\'' +
                ", status='" + status + '\'' +
                ", deadline=" + deadline +
                ", categoryId=" + categoryId +
                ", authorUserId=" + authorUserId +
                ", summary='" + summary + '\'' +
                ", genre='" + genre + '\'' +
                ", priority='" + priority + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}
