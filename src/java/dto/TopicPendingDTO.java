package dto;

import java.io.Serializable;
import java.sql.Date;

/**
 * Data Transfer Object for Pending Topic Evaluation Queue (SCR-2.2).
 */
public class TopicPendingDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int topicId;
    private String topicCode;
    private String title;
    private String angle;
    private String status;
    private Date deadline;
    private int categoryId;
    private String categoryName;
    private int deskId;
    private String deskName;
    private int authorUserId;
    private String authorName;
    private String authorEmail;

    public TopicPendingDTO() {
    }

    public TopicPendingDTO(int topicId, String topicCode, String title, String angle, String status,
                           Date deadline, int categoryId, String categoryName, int deskId,
                           String deskName, int authorUserId, String authorName, String authorEmail) {
        this.topicId = topicId;
        this.topicCode = topicCode;
        this.title = title;
        this.angle = angle;
        this.status = status;
        this.deadline = deadline;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.deskId = deskId;
        this.deskName = deskName;
        this.authorUserId = authorUserId;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public int getAuthorUserId() {
        return authorUserId;
    }

    public void setAuthorUserId(int authorUserId) {
        this.authorUserId = authorUserId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }
}
