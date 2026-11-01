<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Bảng Điều Khiển Tòa Soạn — NTMCS</title>
        <link rel="stylesheet" href="${base}/css/style.css">
    </head>
    <body style="padding: 24px; max-width: 1200px; margin: 0 auto; background: var(--bg);">
        <jsp:include page="menu.jsp" />
        
        <div style="background: var(--surface); border: 1px solid var(--line); border-radius: var(--radius); padding: 28px 32px; box-shadow: var(--shadow); margin-top: 16px;">
            <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--line); padding-bottom: 18px; margin-bottom: 22px;">
                <div>
                    <span style="font-size: 11px; font-weight: 800; letter-spacing: 1.1px; color: var(--primary); text-transform: uppercase;">BẢNG ĐIỀU KHIỂN ĐIỀU HÀNH TÒA SOẠN</span>
                    <h1 style="font-size: 26px; margin: 4px 0 2px; color: var(--text);">
                        Xin chào, <c:out value="${sessionScope.loggedUser.fullName}" />
                    </h1>
                    <p style="margin: 0; color: var(--muted); font-size: 13px;">
                        Vai trò: <b><c:out value="${sessionScope.loggedUser.roleName}" /></b>
                        <c:if test="${not empty sessionScope.loggedUser.deskName}">
                            &bull; Phòng ban: <b><c:out value="${sessionScope.loggedUser.deskName}" /></b>
                        </c:if>
                    </p>
                </div>
                <div style="display: flex; gap: 8px;">
                    <a href="${base}/logout" class="btn danger sm" style="padding: 8px 16px; font-size: 13px; font-weight: 700; text-decoration: none; border-radius: 8px;">Đăng xuất</a>
                </div>
            </div>

            <c:choose>
                <c:when test="${empty screens}">
                    <div style="padding: 30px; text-align: center; color: var(--muted); background: var(--surface2); border-radius: 12px; border: 1px dashed var(--line);">
                        <p style="font-size: 15px; margin: 0 0 6px;">Tài khoản của bạn hiện chưa được phân quyền truy cập chức năng nào.</p>
                        <small>Vui lòng liên hệ Quản trị viên (Cường) để thiết lập phân quyền RBAC phù hợp.</small>
                    </div>
                </c:when>
                <c:otherwise>
                    <h3 style="font-size: 16px; margin: 0 0 14px; color: var(--text);">Các phân hệ được cấp phép truy cập:</h3>
                    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 14px;">
                        <c:forEach var="s" items="${screens}">
                            <a href="${base}${s[1]}" style="display: flex; align-items: center; justify-content: space-between; padding: 18px 20px; background: var(--surface2); border: 1px solid var(--line); border-radius: 12px; color: var(--text); text-decoration: none; transition: all .15s ease; box-shadow: 0 2px 8px rgba(0,0,0,0.02);" onmouseover="this.style.borderColor='var(--primary)'; this.style.transform='translateY(-2px)'" onmouseout="this.style.borderColor='var(--line)'; this.style.transform='none'">
                                <span style="font-weight: 600; font-size: 14px;">${s[0]}</span>
                                <span style="color: var(--primary); font-size: 18px; font-weight: 800;">&rarr;</span>
                            </a>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </body>
</html>
