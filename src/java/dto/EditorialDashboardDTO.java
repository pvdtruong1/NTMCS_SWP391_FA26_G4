package dto;

import java.io.Serializable;

/**
 * Data Transfer Object encapsulating executive KPI metrics for
 * SCR-2.1: Editorial Board Executive Dashboard.
 * 
 * Actor: Desk Editor (Phạm Vũ Đình Trường - Role 2, Desk 1) / Executive Editor (TBT - Role 3)
 */
public class EditorialDashboardDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int deskId;
    private String deskName;
    private int pendingReviewCount;      // SUBMITTED + PENDING_EXECUTIVE_APPROVAL
    private int activeFieldworkCount;    // APPROVED + IN_PROGRESS
    private int pendingHandoverCount;    // DELIVERED
    private int acceptedCmsCount;        // ACCEPTED_READY_FOR_CMS
    private int rejectedCount;           // REJECTED + ABORTED
    private double avgReviewLatencyHours;
    private double approvalRatePercent;

    public EditorialDashboardDTO() {
    }

    public EditorialDashboardDTO(int deskId, String deskName, int pendingReviewCount, 
                                 int activeFieldworkCount, int pendingHandoverCount, 
                                 int acceptedCmsCount, int rejectedCount, 
                                 double avgReviewLatencyHours, double approvalRatePercent) {
        this.deskId = deskId;
        this.deskName = deskName;
        this.pendingReviewCount = pendingReviewCount;
        this.activeFieldworkCount = activeFieldworkCount;
        this.pendingHandoverCount = pendingHandoverCount;
        this.acceptedCmsCount = acceptedCmsCount;
        this.rejectedCount = rejectedCount;
        this.avgReviewLatencyHours = avgReviewLatencyHours;
        this.approvalRatePercent = approvalRatePercent;
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

    public int getPendingReviewCount() {
        return pendingReviewCount;
    }

    public void setPendingReviewCount(int pendingReviewCount) {
        this.pendingReviewCount = pendingReviewCount;
    }

    public int getActiveFieldworkCount() {
        return activeFieldworkCount;
    }

    public void setActiveFieldworkCount(int activeFieldworkCount) {
        this.activeFieldworkCount = activeFieldworkCount;
    }

    public int getPendingHandoverCount() {
        return pendingHandoverCount;
    }

    public void setPendingHandoverCount(int pendingHandoverCount) {
        this.pendingHandoverCount = pendingHandoverCount;
    }

    public int getAcceptedCmsCount() {
        return acceptedCmsCount;
    }

    public void setAcceptedCmsCount(int acceptedCmsCount) {
        this.acceptedCmsCount = acceptedCmsCount;
    }

    public int getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(int rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public double getAvgReviewLatencyHours() {
        return avgReviewLatencyHours;
    }

    public void setAvgReviewLatencyHours(double avgReviewLatencyHours) {
        this.avgReviewLatencyHours = avgReviewLatencyHours;
    }

    public double getApprovalRatePercent() {
        return approvalRatePercent;
    }

    public void setApprovalRatePercent(double approvalRatePercent) {
        this.approvalRatePercent = approvalRatePercent;
    }

    public String getFormattedLatency() {
        return String.format(java.util.Locale.US, "%.1f", avgReviewLatencyHours);
    }

    public String getFormattedApprovalRate() {
        return String.format(java.util.Locale.US, "%.1f%%", approvalRatePercent);
    }

    public int getTotalEvaluated() {
        return acceptedCmsCount + activeFieldworkCount + rejectedCount;
    }
}
