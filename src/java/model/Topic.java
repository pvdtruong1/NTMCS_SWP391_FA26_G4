package model;

import java.io.Serializable;
import java.sql.Date;

/**
 * Entity representing the Topic aggregate root.
 */
public class Topic implements Serializable {
    private static final long serialVersionUID = 1L;

    private int topicId;
    private String topicCode;
    private String title;
    private String angle;
    private String status;
    private Date deadline;
    private int categoryId;
    private int authorUserId;

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

    public String getAngle() {
        return angle;
    }

    public void setAngle(String angle) {
        this.angle = angle;
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
                '}';
    }
}
