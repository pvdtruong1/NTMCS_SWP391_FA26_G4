
package model;

public class FieldworkTask {
    private int taskId;
    private int topicId;
    private int assigneeUserId;
    private String taskStatus;
    private String description;

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
}
