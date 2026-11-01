<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản Lý Người Dùng — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
</head>
<body>
<div class="app">
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="users"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="topbar.jsp">
            <jsp:param name="title" value="Quản Lý Tài Khoản &amp; Định Danh"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">GOVERNANCE &amp; ADMINISTRATION / WF-05</span>
                    <h1>Danh Sách Tài Khoản Người Dùng</h1>
                    <p>Quản lý tài khoản, phân quyền vai trò và phòng ban chuyên môn cho toàn bộ nhân sự tòa soạn.</p>
                </div>
                <div class="page-actions">
                    <a href="${base}/users?action=new" class="btn primary">
                        + Thêm Tài Khoản Mới
                    </a>
                </div>
            </div>

            <!-- Feedback Notifications -->
            <c:if test="${param.msg == 'created'}">
                <div style="background: #e8f7ef; color: #15734f; border: 1px solid #ccebdd; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">
                    ✓ Tạo mới tài khoản người dùng thành công!
                </div>
            </c:if>
            <c:if test="${param.msg == 'updated'}">
                <div style="background: #e8f7ef; color: #15734f; border: 1px solid #ccebdd; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">
                    ✓ Đã cập nhật thông tin tài khoản thành công!
                </div>
            </c:if>
            <c:if test="${param.msg == 'locked'}">
                <div style="background: #fff4dd; color: #9a6200; border: 1px solid #f4ddb2; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">
                    🔒 Tài khoản đã được khóa an toàn.
                </div>
            </c:if>
            <c:if test="${param.msg == 'unlocked'}">
                <div style="background: #e8f7ef; color: #15734f; border: 1px solid #ccebdd; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">
                    🔓 Tài khoản đã được mở khóa hoạt động trở lại.
                </div>
            </c:if>
            <c:if test="${param.msg == 'selflock'}">
                <div style="background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">
                    ✕ Bạn không thể tự khóa tài khoản của chính mình.
                </div>
            </c:if>

            <div class="card">
                <div class="table-scroll">
                    <table>
                        <thead>
                            <tr>
                                <th style="width: 60px;">MÃ</th>
                                <th>HỌ VÀ TÊN</th>
                                <th>EMAIL</th>
                                <th>VAI TRÒ (ROLE)</th>
                                <th>PHÒNG BAN (DESK)</th>
                                <th>TRẠNG THÁI</th>
                                <th style="text-align: right;">THAO TÁC</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="u" items="${userList}">
                                <tr>
                                    <td><strong>#${u.userId}</strong></td>
                                    <td>
                                        <strong><c:out value="${u.fullName}" /></strong>
                                    </td>
                                    <td><span style="color: var(--muted);"><c:out value="${u.email}" /></span></td>
                                    <td>
                                        <span class="badge" style="background: #eef2ff; color: var(--primary); font-weight: 700;">
                                            ${u.roleName}
                                        </span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty u.deskName}">
                                                🏛️ <c:out value="${u.deskName}" />
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color: var(--muted); font-style: italic;">Toàn tòa soạn</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${u.activeStatus}">
                                                <span class="badge b-approved">Hoạt động</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge b-rejected">Bị khóa</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align: right;">
                                        <div style="display: inline-flex; gap: 6px;">
                                            <a href="${base}/users?action=edit&id=${u.userId}" class="btn sm" style="text-decoration: none;">
                                                ✏️ Sửa
                                            </a>
                                            <form method="post" action="${base}/users" style="display:inline; margin: 0;">
                                                <input type="hidden" name="id" value="${u.userId}">
                                                <c:choose>
                                                    <c:when test="${u.activeStatus}">
                                                        <input type="hidden" name="action" value="lock">
                                                        <button type="submit" class="btn sm danger" onclick="return confirm('Bạn có chắc chắn muốn khóa tài khoản này?');">
                                                            🔒 Khóa
                                                        </button>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <input type="hidden" name="action" value="unlock">
                                                        <button type="submit" class="btn sm success">
                                                            🔓 Mở
                                                        </button>
                                                    </c:otherwise>
                                                </c:choose>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>
</div>
</body>
</html>
