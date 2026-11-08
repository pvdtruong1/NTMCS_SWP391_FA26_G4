package dto;

import java.io.Serializable;

/**
 * Data Transfer Object representing a topic that has been accepted and handed over
 * to CMS with an authentic cryptographic HMAC-SHA256 token (RULE-02).
 * 
 * Actor: Desk Editor (Phạm Vũ Đình Trường - Role 2) / Executive Editor (Role 3)
 */
public class CmsHandoverRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int topicId;
    private String topicCode;
    private String title;
    private String categoryName;
    private String deskName;
    private String tokenHash;
    private int cycleNumber;
    private String mintedAt;
    private int editorUserId;
    private String editorName;
    private String note;

    public CmsHandoverRecordDTO() {
    }

    public CmsHandoverRecordDTO(int topicId, String topicCode, String title, String categoryName,
                               String deskName, String tokenHash, int cycleNumber, String mintedAt,
                               int editorUserId, String editorName, String note) {
        this.topicId = topicId;
        this.topicCode = topicCode;
        this.title = title;
        this.categoryName = categoryName;
        this.deskName = deskName;
        this.tokenHash = tokenHash;
        this.cycleNumber = cycleNumber;
        this.mintedAt = mintedAt;
        this.editorUserId = editorUserId;
        this.editorName = editorName;
        this.note = note;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDeskName() {
        return deskName;
    }

    public void setDeskName(String deskName) {
        this.deskName = deskName;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public int getCycleNumber() {
        return cycleNumber;
    }

    public void setCycleNumber(int cycleNumber) {
        this.cycleNumber = cycleNumber;
    }

    public String getMintedAt() {
        return mintedAt;
    }

    public void setMintedAt(String mintedAt) {
        this.mintedAt = mintedAt;
    }

    public int getEditorUserId() {
        return editorUserId;
    }

    public void setEditorUserId(int editorUserId) {
        this.editorUserId = editorUserId;
    }

    public String getEditorName() {
        return editorName;
    }

    public void setEditorName(String editorName) {
        this.editorName = editorName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    /**
     * Returns a shortened representation of the token for clean display (e.g. "a1b2c3d4...9z8y7x6w").
     */
    public String getShortToken() {
        if (tokenHash == null || tokenHash.length() <= 16) {
            return tokenHash;
        }
        return tokenHash.substring(0, 8) + "..." + tokenHash.substring(tokenHash.length() - 8);
    }
}
