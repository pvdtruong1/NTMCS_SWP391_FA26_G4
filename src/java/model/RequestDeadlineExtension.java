package model;

import java.io.Serializable;
import java.sql.Date;

/**
 * Entity representing a Deadline Extension Request submitted by Reporter to Desk Editor.
 * Mapped to table [Request_Deadline_Extension].
 */
public class RequestDeadlineExtension implements Serializable {
    private static final long serialVersionUID = 1L;

    // Database columns (table Request_Deadline_Extension)
    private int requestId;
    private int topicId;
    private int requestedByUserId;
    private Date currentDeadline;
    private Date requestedDeadline;
    private String reason;
    private String status;

    // View-helper fields (Mapped via JOINs)
    private String requesterName;

    public RequestDeadlineExtension() {
    }

    public RequestDeadlineExtension(int requestId, int topicId, int requestedByUserId, 
                                    Date currentDeadline, Date requestedDeadline, 
                                    String reason, String status) {
        this.requestId = requestId;
        this.topicId = topicId;
        this.requestedByUserId = requestedByUserId;
        this.currentDeadline = currentDeadline;
        this.requestedDeadline = requestedDeadline;
        this.reason = reason;
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

    public int getRequestedByUserId() {
        return requestedByUserId;
    }

    public void setRequestedByUserId(int requestedByUserId) {
        this.requestedByUserId = requestedByUserId;
    }

    public Date getCurrentDeadline() {
        return currentDeadline;
    }

    public void setCurrentDeadline(Date currentDeadline) {
        this.currentDeadline = currentDeadline;
    }

    public Date getRequestedDeadline() {
        return requestedDeadline;
    }

    public void setRequestedDeadline(Date requestedDeadline) {
        this.requestedDeadline = requestedDeadline;
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

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }
}
