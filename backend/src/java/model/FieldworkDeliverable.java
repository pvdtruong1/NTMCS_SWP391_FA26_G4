package model;

import java.sql.Timestamp;

public class FieldworkDeliverable {
    private int deliverableId;
    private int taskId;
    private int submittedByUserId;
    private String submissionType;
    private String deliverableNote;
    private String originalFileName;
    private String storedFilePath;
    private String contentType;
    private long fileSizeBytes;
    private String externalUrl;
    private Timestamp submittedAt;

    public int getDeliverableId() { return deliverableId; }
    public void setDeliverableId(int deliverableId) { this.deliverableId = deliverableId; }

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public int getSubmittedByUserId() { return submittedByUserId; }
    public void setSubmittedByUserId(int submittedByUserId) { this.submittedByUserId = submittedByUserId; }

    public String getSubmissionType() { return submissionType; }
    public void setSubmissionType(String submissionType) { this.submissionType = submissionType; }

    public String getDeliverableNote() { return deliverableNote; }
    public void setDeliverableNote(String deliverableNote) { this.deliverableNote = deliverableNote; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getStoredFilePath() { return storedFilePath; }
    public void setStoredFilePath(String storedFilePath) { this.storedFilePath = storedFilePath; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getExternalUrl() { return externalUrl; }
    public void setExternalUrl(String externalUrl) { this.externalUrl = externalUrl; }

    public Timestamp getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Timestamp submittedAt) { this.submittedAt = submittedAt; }
}
