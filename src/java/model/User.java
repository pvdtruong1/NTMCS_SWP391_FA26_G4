package model;

import java.io.Serializable;

/**
 * Entity representing internal newsroom personnel.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userId;
    private String fullName;
    private String email;
    private String passwordHash;
    private int roleId;
    private boolean activeStatus;
    private Integer deskId;      // can be null (some admins have no Desk)

    // Display only (filled by JOIN queries)
    private String roleName;
    private String deskName;

    public User() {
    }

    public User(int userId, String fullName, String email, String passwordHash, int roleId, boolean activeStatus, Integer deskId) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roleId = roleId;
        this.activeStatus = activeStatus;
        this.deskId = deskId;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public boolean isActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(boolean activeStatus) {
        this.activeStatus = activeStatus;
    }

    public Integer getDeskId() {
        return deskId;
    }

    public void setDeskId(Integer deskId) {
        this.deskId = deskId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDeskName() {
        return deskName;
    }

    public void setDeskName(String deskName) {
        this.deskName = deskName;
    }

    public String getDisplayName() {
        if (roleName != null && !roleName.isEmpty()) {
            return fullName + " (" + roleName + ")";
        }
        return fullName;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", roleId=" + roleId +
                ", activeStatus=" + activeStatus +
                ", deskId=" + deskId +
                ", roleName='" + roleName + '\'' +
                ", deskName='" + deskName + '\'' +
                '}';
    }
}
