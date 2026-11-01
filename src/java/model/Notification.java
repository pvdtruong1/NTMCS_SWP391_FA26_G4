package model;

import java.io.Serializable;

/**
 * Model mapping Notification table in SQL Server.
 */
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    private int notificationId;
    private int userId;
    private Integer topicId;
    private String message;
    private boolean isRead;
    private String createdAt;

    public Notification() {
    }

    public Notification(int notificationId, int userId, Integer topicId, String message, boolean isRead) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.topicId = topicId;
        this.message = message;
        this.isRead = isRead;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getTopicId() {
        return topicId;
    }

    public void setTopicId(Integer topicId) {
        this.topicId = topicId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public boolean isIsRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "notificationId=" + notificationId +
                ", userId=" + userId +
                ", topicId=" + topicId +
                ", message='" + message + '\'' +
                ", isRead=" + isRead +
                '}';
    }
}
