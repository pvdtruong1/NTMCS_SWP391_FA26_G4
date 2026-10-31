package model;

import java.io.Serializable;
import java.sql.Timestamp;

public class AuditLog implements Serializable {
    private int logId;
    private int actorUserId;
    private String actorName;     // display only (JOIN with User)
    private String action;
    private String entityType;
    private Integer entityId;     // can be null
    private String details;
    private Timestamp createdAt;

    public AuditLog() {
    }

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getActorUserId() { return actorUserId; }
    public void setActorUserId(int actorUserId) { this.actorUserId = actorUserId; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Integer getEntityId() { return entityId; }
    public void setEntityId(Integer entityId) { this.entityId = entityId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
