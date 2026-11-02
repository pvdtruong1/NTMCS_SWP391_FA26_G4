package model;

import java.io.Serializable;

/**
 * Domain model representing a Fieldwork Task on the Kanban Board (WF-04).
 */
public class FieldworkTask implements Serializable {
    private static final long serialVersionUID = 1L;

    private int taskId;
    private int topicId;
    private int assigneeUserId;
    private String taskStatus;
    private String description;

    // Optional joins for display
    private String topicCode;
    private String topicTitle;
    private String assigneeName;
    private String assigneeEmail;

    public FieldworkTask() {
    }

    public FieldworkTask(int taskId, int topicId, int assigneeUserId,
                         String taskStatus, String description) {
        this.taskId = taskId;
        this.topicId = topicId;
        this.assigneeUserId = assigneeUserId;
        this.taskStatus = taskStatus;
        this.description = description;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getAssigneeUserId() {
        return assigneeUserId;
    }

    public void setAssigneeUserId(int assigneeUserId) {
        this.assigneeUserId = assigneeUserId;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public String getAssigneeEmail() {
        return assigneeEmail;
    }

    public void setAssigneeEmail(String assigneeEmail) {
        this.assigneeEmail = assigneeEmail;
    }
}
