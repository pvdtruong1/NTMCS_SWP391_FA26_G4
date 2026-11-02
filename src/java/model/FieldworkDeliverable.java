package model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Domain model representing a Fieldwork Deliverable submission (SCR-4.3).
 */
public class FieldworkDeliverable implements Serializable {
    private static final long serialVersionUID = 1L;

    private int deliverableId;
    private int taskId;
    private int submittedByUserId;
    private String submissionType; // FILE or LINK
    private String deliverableNote;
    private String originalFileName;
    private String storedFilePath;
    private String contentType;
    private Long fileSizeBytes;
    private String externalUrl;
    private Timestamp submittedAt;

    // Optional joins for display
    private String submitterName;
    private String taskDescription;

    public FieldworkDeliverable() {
    }

    public int getDeliverableId() {
        return deliverableId;
    }

    public void setDeliverableId(int deliverableId) {
        this.deliverableId = deliverableId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getSubmittedByUserId() {
        return submittedByUserId;
    }

    public void setSubmittedByUserId(int submittedByUserId) {
        this.submittedByUserId = submittedByUserId;
    }

    public String getSubmissionType() {
        return submissionType;
    }

    public void setSubmissionType(String submissionType) {
        this.submissionType = submissionType;
    }

    public String getDeliverableNote() {
        return deliverableNote;
    }

    public void setDeliverableNote(String deliverableNote) {
        this.deliverableNote = deliverableNote;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStoredFilePath() {
        return storedFilePath;
    }

    public void setStoredFilePath(String storedFilePath) {
        this.storedFilePath = storedFilePath;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public Timestamp getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Timestamp submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getSubmitterName() {
        return submitterName;
    }

    public void setSubmitterName(String submitterName) {
        this.submitterName = submitterName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }
}
