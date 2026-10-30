package model;

import java.io.Serializable;

/**
 * Represents an assigned team member on a topic (Topic_Member).
 */
public class TopicMember implements Serializable {
    private int memberId;
    private int topicId;
    private int userId;
    private String roleInTeam;

    // Display fields from join with [User] and Role
    private String userName;
    private String userRole;
    private String userEmail;

    public TopicMember() {
    }

    public TopicMember(int memberId, int topicId, int userId, String roleInTeam) {
        this.memberId = memberId;
        this.topicId = topicId;
        this.userId = userId;
        this.roleInTeam = roleInTeam;
    }

    public TopicMember(int topicId, int userId, String roleInTeam) {
        this(0, topicId, userId, roleInTeam);
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRoleInTeam() {
        return roleInTeam;
    }

    public void setRoleInTeam(String roleInTeam) {
        this.roleInTeam = roleInTeam;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getRoleInTeamDisplay() {
        if ("LEAD_REPORTER".equalsIgnoreCase(roleInTeam)) {
            return "Phóng viên chính";
        } else if ("PHOTO_REPORTER".equalsIgnoreCase(roleInTeam)) {
            return "Phóng viên ảnh";
        } else if ("CAMERAMAN".equalsIgnoreCase(roleInTeam)) {
            return "Quay phim";
        } else if ("ASSISTANT".equalsIgnoreCase(roleInTeam)) {
            return "Trợ lý";
        }
        return roleInTeam != null ? roleInTeam : "";
    }

    @Override
    public String toString() {
        return "TopicMember{" +
                "memberId=" + memberId +
                ", topicId=" + topicId +
                ", userId=" + userId +
                ", roleInTeam='" + roleInTeam + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
