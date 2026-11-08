package dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object representing a delivered topic waiting for editorial
 * acceptance and CMS handover on SCR-2.1: Editorial Board Executive Dashboard.
 * 
 * Enforces RULE-01 Guard (Zero Unreturned Assets).
 * Actor: Desk Editor (Phạm Vũ Đình Trường - Role 2, Desk 1)
 */
public class TopicDeliverableDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int topicId;
    private String topicCode;
    private String title;
    private String angle;
    private String status;
    private String deadline;
    private int categoryId;
    private String categoryName;
    private int deskId;
    private String deskName;
    private int authorUserId;
    private String authorName;
    private String authorEmail;
    private int leadReporterId;
    private String leadReporterName;
    private int unreturnedAssetCount;
    private List<String> unreturnedAssetDetails;
    private int completedTasksCount;
    private int totalTasksCount;

    public TopicDeliverableDTO() {
        this.unreturnedAssetDetails = new ArrayList<>();
    }

    public TopicDeliverableDTO(int topicId, String topicCode, String title, String angle,
                               String status, String deadline, int categoryId, String categoryName,
                               int deskId, String deskName, int authorUserId, String authorName,
                               String authorEmail, int leadReporterId, String leadReporterName,
                               int unreturnedAssetCount, List<String> unreturnedAssetDetails,
                               int completedTasksCount, int totalTasksCount) {
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
        this.leadReporterId = leadReporterId;
        this.leadReporterName = leadReporterName;
        this.unreturnedAssetCount = unreturnedAssetCount;
        this.unreturnedAssetDetails = (unreturnedAssetDetails != null) ? unreturnedAssetDetails : new ArrayList<>();
        this.completedTasksCount = completedTasksCount;
        this.totalTasksCount = totalTasksCount;
    }

    /**
     * Business Integrity Check (RULE-01 Interlock Guard):
     * Returns true ONLY when all borrowed assets have been returned to warehouse.
     */
    public boolean isRule01Passed() {
        return this.unreturnedAssetCount == 0;
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

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
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

    public int getLeadReporterId() {
        return leadReporterId;
    }

    public void setLeadReporterId(int leadReporterId) {
        this.leadReporterId = leadReporterId;
    }

    public String getLeadReporterName() {
        return leadReporterName;
    }

    public void setLeadReporterName(String leadReporterName) {
        this.leadReporterName = leadReporterName;
    }

    public int getUnreturnedAssetCount() {
        return unreturnedAssetCount;
    }

    public void setUnreturnedAssetCount(int unreturnedAssetCount) {
        this.unreturnedAssetCount = unreturnedAssetCount;
    }

    public List<String> getUnreturnedAssetDetails() {
        return unreturnedAssetDetails;
    }

    public void setUnreturnedAssetDetails(List<String> unreturnedAssetDetails) {
        this.unreturnedAssetDetails = unreturnedAssetDetails;
    }

    public int getCompletedTasksCount() {
        return completedTasksCount;
    }

    public void setCompletedTasksCount(int completedTasksCount) {
        this.completedTasksCount = completedTasksCount;
    }

    public int getTotalTasksCount() {
        return totalTasksCount;
    }

    public void setTotalTasksCount(int totalTasksCount) {
        this.totalTasksCount = totalTasksCount;
    }

    public String getUnreturnedAssetsSummary() {
        if (unreturnedAssetDetails == null || unreturnedAssetDetails.isEmpty()) {
            return "";
        }
        return String.join(", ", unreturnedAssetDetails);
    }
}
