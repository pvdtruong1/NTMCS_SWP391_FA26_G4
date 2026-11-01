package dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object representing reporter workload metrics for
 * SCR-2.5: Reporter Dispatch & Workload Desk.
 * 
 * Actor: Desk Editor (Trường - Role 2, Desk 1: Ban Thời sự - Chính trị)
 */
public class ReporterWorkloadDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userId;
    private String fullName;
    private String email;
    private int deskId;
    private String deskName;
    private int activeTopicsCount;
    private int overdueTopicsCount;
    private int completedTopicsCount;
    private List<String> activeTopicTitles;

    public ReporterWorkloadDTO() {
        this.activeTopicTitles = new ArrayList<>();
    }

    public ReporterWorkloadDTO(int userId, String fullName, String email, int deskId, String deskName,
                               int activeTopicsCount, int overdueTopicsCount, int completedTopicsCount) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.deskId = deskId;
        this.deskName = deskName;
        this.activeTopicsCount = activeTopicsCount;
        this.overdueTopicsCount = overdueTopicsCount;
        this.completedTopicsCount = completedTopicsCount;
        this.activeTopicTitles = new ArrayList<>();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public int getActiveTopicsCount() {
        return activeTopicsCount;
    }

    public void setActiveTopicsCount(int activeTopicsCount) {
        this.activeTopicsCount = activeTopicsCount;
    }

    public int getOverdueTopicsCount() {
        return overdueTopicsCount;
    }

    public void setOverdueTopicsCount(int overdueTopicsCount) {
        this.overdueTopicsCount = overdueTopicsCount;
    }

    public int getCompletedTopicsCount() {
        return completedTopicsCount;
    }

    public void setCompletedTopicsCount(int completedTopicsCount) {
        this.completedTopicsCount = completedTopicsCount;
    }

    public List<String> getActiveTopicTitles() {
        return activeTopicTitles;
    }

    public void setActiveTopicTitles(List<String> activeTopicTitles) {
        this.activeTopicTitles = activeTopicTitles != null ? activeTopicTitles : new ArrayList<>();
    }

    public void addActiveTopicTitle(String title) {
        if (title != null && !title.trim().isEmpty()) {
            this.activeTopicTitles.add(title.trim());
        }
    }

    /**
     * Determines workload status category:
     * - OVERLOADED: overdue topics > 0 OR active >= 4
     * - MODERATE: active topics 2-3
     * - OPTIMAL: active topics <= 1
     */
    public String getWorkloadStatus() {
        if (overdueTopicsCount > 0 || activeTopicsCount >= 4) {
            return "OVERLOADED";
        } else if (activeTopicsCount >= 2) {
            return "MODERATE";
        } else {
            return "OPTIMAL";
        }
    }

    /**
     * Formats Vietnamese label for UI badge.
     */
    public String getWorkloadStatusLabel() {
        if (overdueTopicsCount > 0) {
            return "Chậm tiến độ (" + overdueTopicsCount + " đề tài)";
        } else if (activeTopicsCount >= 4) {
            return "Quá tải (" + activeTopicsCount + " đề tài)";
        } else if (activeTopicsCount >= 2) {
            return "Tải trung bình (" + activeTopicsCount + " đề tài)";
        } else if (activeTopicsCount == 1) {
            return "Tải nhẹ (1 đề tài)";
        } else {
            return "Sẵn sàng nhận việc";
        }
    }

    /**
     * Returns CSS class for workload status badge.
     */
    public String getWorkloadBadgeClass() {
        if (overdueTopicsCount > 0 || activeTopicsCount >= 4) {
            return "badge-danger";
        } else if (activeTopicsCount >= 2) {
            return "badge-warning";
        } else {
            return "badge-success";
        }
    }

    /**
     * Returns true if reporter is in healthy capacity to receive directive topic.
     */
    public boolean isAvailable() {
        return overdueTopicsCount == 0 && activeTopicsCount < 4;
    }

    @Override
    public String toString() {
        return "ReporterWorkloadDTO{" +
                "userId=" + userId +
                ", fullName='" + fullName + '\'' +
                ", deskName='" + deskName + '\'' +
                ", active=" + activeTopicsCount +
                ", overdue=" + overdueTopicsCount +
                ", completed=" + completedTopicsCount +
                ", status=" + getWorkloadStatus() +
                '}';
    }
}
