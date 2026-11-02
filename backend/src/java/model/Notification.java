package model;

public class Notification {
    private final int notificationId;
    private final int userId;
    private final Integer topicId;
    private final String message;
    private final boolean read;
    private final String createdAt;

    public Notification(int notificationId, int userId, Integer topicId,
                        String message, boolean read, String createdAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.topicId = topicId;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public int getNotificationId() { return notificationId; }
    public int getUserId() { return userId; }
    public Integer getTopicId() { return topicId; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public String getCreatedAt() { return createdAt; }
}
