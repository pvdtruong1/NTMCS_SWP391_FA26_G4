package model;

import java.io.Serializable;

public class DeskQuota implements Serializable {
    private int quotaId;
    private int deskId;
    private String deskName;     // display only (JOIN with Desk)
    private String period;       // e.g. 2026-10 (month) or 2026-Q4 (quarter)
    private int quotaCount;

    public DeskQuota() {
    }

    public int getQuotaId() { return quotaId; }
    public void setQuotaId(int quotaId) { this.quotaId = quotaId; }

    public int getDeskId() { return deskId; }
    public void setDeskId(int deskId) { this.deskId = deskId; }

    public String getDeskName() { return deskName; }
    public void setDeskName(String deskName) { this.deskName = deskName; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public int getQuotaCount() { return quotaCount; }
    public void setQuotaCount(int quotaCount) { this.quotaCount = quotaCount; }
}
