<%@ page contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="dal.DBContext, model.User, util.AuthUtil, java.sql.Connection, java.sql.PreparedStatement, java.sql.ResultSet" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    User loggedUser = AuthUtil.getLoggedUser(request);
    if (loggedUser == null) {
        out.print("{\"unreadCount\":0,\"notifications\":[]}");
        return;
    }

    int userId = loggedUser.getUserId();
    String action = request.getParameter("action");

    try (Connection conn = new DBContext().getConnection()) {
        if ("markAllRead".equals(action)) {
            String sqlUpdate = "UPDATE Notification SET is_read = 1 WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
            out.print("{\"success\":true,\"unreadCount\":0}");
            return;
        }

        if ("markRead".equals(action)) {
            int notifId = 0;
            try {
                notifId = Integer.parseInt(request.getParameter("id"));
            } catch (Exception ignored) {}
            if (notifId > 0) {
                String sqlUpdate = "UPDATE Notification SET is_read = 1 WHERE notification_id = ? AND user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                    ps.setInt(1, notifId);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }
            }
            out.print("{\"success\":true}");
            return;
        }

        // Đếm số thông báo chưa đọc
        int unreadCount = 0;
        String sqlCount = "SELECT COUNT(*) FROM Notification WHERE user_id = ? AND is_read = 0";
        try (PreparedStatement ps = conn.prepareStatement(sqlCount)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    unreadCount = rs.getInt(1);
                }
            }
        }

        // Lấy danh sách thông báo của user (15 thông báo mới nhất)
        String sqlList = "SELECT notification_id, topic_id, message, is_read "
                       + "FROM Notification WHERE user_id = ? "
                       + "ORDER BY notification_id DESC";
        StringBuilder json = new StringBuilder();
        json.append("{\"unreadCount\":").append(unreadCount).append(",\"notifications\":[");
        boolean first = true;
        try (PreparedStatement ps = conn.prepareStatement(sqlList)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next() && count < 15) {
                    if (!first) json.append(",");
                    first = false;
                    int nId = rs.getInt("notification_id");
                    int tId = rs.getInt("topic_id");
                    boolean wasNullTopic = rs.wasNull();
                    String msg = rs.getString("message");
                    if (msg == null) msg = "";
                    msg = msg.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
                    boolean isRead = rs.getBoolean("is_read");

                    json.append("{")
                        .append("\"id\":").append(nId).append(",")
                        .append("\"topicId\":").append(wasNullTopic ? "null" : tId).append(",")
                        .append("\"message\":\"").append(msg).append("\",")
                        .append("\"isRead\":").append(isRead)
                        .append("}");
                    count++;
                }
            }
        }
        json.append("]}");
        out.print(json.toString());
    } catch (Exception e) {
        out.print("{\"unreadCount\":0,\"notifications\":[],\"error\":\"" + e.getMessage() + "\"}");
    }
%>