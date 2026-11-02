package model;

/**
 * Persistent configuration for WF-04 SLA notifications.
 * Thresholds are measured in hours before the topic deadline.
 */
public class SLAConfig {
    private boolean inAppEnabled;
    private int thresholdHours1;
    private int thresholdHours2;
    private int thresholdHours3;
    private String earlyWarningTemplate;
    private String warningTemplate;
    private String urgentTemplate;
    private String nudgeTemplate;

    public SLAConfig() {
        this(true, 24, 12, 2,
            "Đề tài {topicCode} chỉ còn {hours} giờ nữa là đến hạn chót ({deadline}). Bạn có cần xin gia hạn thêm thời gian không?",
            "Đề tài {topicCode} chỉ còn {hours} giờ trước deadline ({deadline}). Vui lòng khẩn trương hoàn thiện tư liệu và bản thảo.",
            "CẢNH BÁO KHẨN: Đề tài {topicCode} đã chạm ngưỡng cảnh báo đỏ. Deadline: {deadline}. Thời gian: {hours}. Vui lòng xử lý ngay.",
            "Đôn đốc khẩn cấp: Vui lòng ưu tiên xử lý nhiệm vụ '{taskDescription}' của đề tài {topicCode} và cập nhật tiến độ sớm nhất.");
    }

    public SLAConfig(int thresholdHours1, int thresholdHours2, int thresholdHours3,
                     String earlyWarningTemplate, String warningTemplate,
                     String urgentTemplate, String nudgeTemplate) {
        this(true, thresholdHours1, thresholdHours2, thresholdHours3,
             earlyWarningTemplate, warningTemplate, urgentTemplate, nudgeTemplate);
    }

    public SLAConfig(boolean inAppEnabled, int thresholdHours1, int thresholdHours2, int thresholdHours3,
                     String earlyWarningTemplate, String warningTemplate,
                     String urgentTemplate, String nudgeTemplate) {
        this.inAppEnabled = inAppEnabled;
        this.thresholdHours1 = thresholdHours1;
        this.thresholdHours2 = thresholdHours2;
        this.thresholdHours3 = thresholdHours3;
        this.earlyWarningTemplate = earlyWarningTemplate;
        this.warningTemplate = warningTemplate;
        this.urgentTemplate = urgentTemplate;
        this.nudgeTemplate = nudgeTemplate;
    }

    public void validate() {
        if (thresholdHours1 < 1 || thresholdHours1 > 168) {
            throw new IllegalArgumentException("Mốc cảnh báo sớm phải từ 1 đến 168 giờ.");
        }
        if (thresholdHours2 < 1 || thresholdHours2 > 72) {
            throw new IllegalArgumentException("Mốc cảnh báo vàng phải từ 1 đến 72 giờ.");
        }
        if (thresholdHours3 < 1 || thresholdHours3 > 24) {
            throw new IllegalArgumentException("Mốc cảnh báo đỏ phải từ 1 đến 24 giờ.");
        }
        if (!(thresholdHours1 > thresholdHours2 && thresholdHours2 > thresholdHours3)) {
            throw new IllegalArgumentException("Các mốc phải theo thứ tự: cảnh báo sớm > cảnh báo vàng > cảnh báo đỏ.");
        }
        requireText(earlyWarningTemplate, "Mẫu cảnh báo sớm");
        requireText(warningTemplate, "Mẫu cảnh báo vàng");
        requireText(urgentTemplate, "Mẫu cảnh báo đỏ");
        requireText(nudgeTemplate, "Mẫu tin nhắn đôn đốc");
    }

    private void requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " không được để trống.");
        }
        if (value.length() > 2000) {
            throw new IllegalArgumentException(label + " không được vượt quá 2000 ký tự.");
        }
    }

    public boolean isInAppEnabled() { return inAppEnabled; }
    public void setInAppEnabled(boolean value) { this.inAppEnabled = value; }
    public int getThresholdHours1() { return thresholdHours1; }
    public void setThresholdHours1(int value) { this.thresholdHours1 = value; }
    public int getThresholdHours2() { return thresholdHours2; }
    public void setThresholdHours2(int value) { this.thresholdHours2 = value; }
    public int getThresholdHours3() { return thresholdHours3; }
    public void setThresholdHours3(int value) { this.thresholdHours3 = value; }
    public String getEarlyWarningTemplate() { return earlyWarningTemplate; }
    public void setEarlyWarningTemplate(String value) { this.earlyWarningTemplate = value; }
    public String getWarningTemplate() { return warningTemplate; }
    public void setWarningTemplate(String value) { this.warningTemplate = value; }
    public String getUrgentTemplate() { return urgentTemplate; }
    public void setUrgentTemplate(String value) { this.urgentTemplate = value; }
    public String getNudgeTemplate() { return nudgeTemplate; }
    public void setNudgeTemplate(String value) { this.nudgeTemplate = value; }
}
