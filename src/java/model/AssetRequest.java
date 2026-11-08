package model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Entity representing an Asset Request by a Reporter for a Topic.
 * Mapped to table [Asset_Request].
 */
public class AssetRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    // Database columns (table Asset_Request)
    private int requestId;
    private int topicId;
    private int assetId;
    private int requesterUserId;
    private Timestamp startTime;
    private Timestamp endTime;
    private String status;

    // View-helper fields (Mapped via JOINs)
    private String topicTitle;
    private String topicCode;
    private String assetName;
    private String assetCategory;
    private String assetCode;
    private String assetStatus;
    private String requesterName;
    private String requesterEmail;

    public AssetRequest() {
    }

    public AssetRequest(int requestId, int topicId, int assetId, int requesterUserId, 
                        Timestamp startTime, Timestamp endTime, String status) {
        this.requestId = requestId;
        this.topicId = topicId;
        this.assetId = assetId;
        this.requesterUserId = requesterUserId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public int getRequesterUserId() {
        return requesterUserId;
    }

    public void setRequesterUserId(int requesterUserId) {
        this.requesterUserId = requesterUserId;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTopicTitle() {
        return topicTitle;
    }

    public void setTopicTitle(String topicTitle) {
        this.topicTitle = topicTitle;
    }

    public String getTopicCode() {
        return topicCode;
    }

    public void setTopicCode(String topicCode) {
        this.topicCode = topicCode;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetCategory() {
        return assetCategory;
    }

    public void setAssetCategory(String assetCategory) {
        this.assetCategory = assetCategory;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public String getAssetStatus() {
        return assetStatus;
    }

    public void setAssetStatus(String assetStatus) {
        this.assetStatus = assetStatus;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
    }
}
