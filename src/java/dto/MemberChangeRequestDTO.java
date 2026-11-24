package dto;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Data Transfer Object representing a reporter's member change proposal
 * (Add or Remove crew member) submitted to the Desk Editor for evaluation.
 * 
 * Actor: Desk Editor (Phạm Vũ Đình Trường - WF-02)
 */
public class MemberChangeRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int notificationId;
    private int topicId;
    private String topicCode;
    private String topicTitle;
    private int deskId;
    private String deskName;
    private int requesterId;
    private String requesterName;
    private int targetUserId;
    private String targetUserName;
    private String targetUserEmail;
    private String actionType; // "ADD" or "REMOVE"
    private String reason;
    private String status;     // "PENDING", "APPROVED", "REJECTED"
    private int pendingTasksCount; // Number of incomplete Fieldwork_Tasks for REMOVE proposal
    private Timestamp createdAt;
    private String rawMessage;

    public MemberChangeRequestDTO() {
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
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

    public String getTopicTitle() {
        return topicTitle;
    }

    public void setTopicTitle(String topicTitle) {
        this.topicTitle = topicTitle;
    }

    public int getDeskId() {
        return deskId;
    }

    public void setDeskId(int deskId) {
        this.deskId = deskId;
    }

    public String getDeskName() {
        return deskName;
    }

    public void setDeskName(String deskName) {
        this.deskName = deskName;
    }

    public int getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(int requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public int getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(int targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public void setTargetUserName(String targetUserName) {
        this.targetUserName = targetUserName;
    }

    public String getTargetUserEmail() {
        return targetUserEmail;
    }

    public void setTargetUserEmail(String targetUserEmail) {
        this.targetUserEmail = targetUserEmail;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPendingTasksCount() {
        return pendingTasksCount;
    }

    public void setPendingTasksCount(int pendingTasksCount) {
        this.pendingTasksCount = pendingTasksCount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }
}
