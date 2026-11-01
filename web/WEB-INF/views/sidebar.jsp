<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<aside id="sidebar">
    <div class="brand">
        <div class="brand-logo">N</div>
        <div>
            <b>NTMCS</b>
            <small>Newsroom Operating System</small>
        </div>
    </div>
    <nav>
        <div class="nav-group">
            <div class="nav-label">TỔNG QUAN</div>
            <a href="${base}/home" class="nav-item ${param.active == 'home' ? 'active' : ''}">
                <i>🏠</i> Trang chủ điều hành
            </a>
        </div>

        <c:if test="${not empty sessionScope.permissions and sessionScope.permissions.contains('TOPIC_EVALUATE')}">
            <div class="nav-group">
                <div class="nav-label">PHÂN HỆ BTV CHUYÊN MÔN (WF-02)</div>
                <a href="${base}/editorial/pending-queue" class="nav-item ${param.active == 'pending-queue' ? 'active' : ''}">
                    <i>📋</i> Hàng đợi chờ thẩm định
                </a>
                <a href="${base}/editorial/directive-creation" class="nav-item ${param.active == 'directive-creation' ? 'active' : ''}">
                    <i>⚡</i> Đặt hàng đề tài chỉ đạo
                </a>
                <a href="${base}/editorial/idea-archive" class="nav-item ${param.active == 'idea-archive' ? 'active' : ''}">
                    <i>🗄️</i> Kho lưu trữ ý tưởng
                </a>
            </div>
        </c:if>

        <c:if test="${not empty sessionScope.permissions and (sessionScope.permissions.contains('USER_MANAGE') or sessionScope.permissions.contains('RBAC_MANAGE') or sessionScope.permissions.contains('AUDIT_VIEW'))}">
            <div class="nav-group">
                <div class="nav-label">QUẢN TRỊ HỆ THỐNG (WF-05)</div>
                <c:if test="${sessionScope.permissions.contains('USER_MANAGE')}">
                    <a href="${base}/users" class="nav-item ${param.active == 'users' ? 'active' : ''}">
                        <i>👥</i> Tài khoản người dùng
                    </a>
                </c:if>
                <c:if test="${sessionScope.permissions.contains('RBAC_MANAGE')}">
                    <a href="${base}/rbac" class="nav-item ${param.active == 'rbac' ? 'active' : ''}">
                        <i>🛡️</i> Ma trận quyền (RBAC)
                    </a>
                </c:if>
                <c:if test="${sessionScope.permissions.contains('AUDIT_VIEW')}">
                    <a href="${base}/audit-log" class="nav-item ${param.active == 'audit' ? 'active' : ''}">
                        <i>📜</i> Nhật ký hệ thống (Audit)
                    </a>
                </c:if>
            </div>
        </c:if>

        <c:if test="${not empty sessionScope.permissions and sessionScope.permissions.contains('QUOTA_MANAGE')}">
            <div class="nav-group">
                <div class="nav-label">ĐIỀU HÀNH & CHỈ TIÊU (WF-05)</div>
                <a href="${base}/dashboard" class="nav-item ${param.active == 'dashboard' ? 'active' : ''}">
                    <i>📊</i> Dashboard điều hành
                </a>
                <a href="${base}/report" class="nav-item ${param.active == 'report' ? 'active' : ''}">
                    <i>📈</i> Báo cáo hiệu suất
                </a>
                <a href="${base}/quota" class="nav-item ${param.active == 'quota' ? 'active' : ''}">
                    <i>🎯</i> Chỉ tiêu phòng ban (Quota)
                </a>
                <a href="${base}/criteria" class="nav-item ${param.active == 'criteria' ? 'active' : ''}">
                    <i>⚖️</i> Bộ tiêu chí Rubric
                </a>
                <a href="${base}/categories" class="nav-item ${param.active == 'categories' ? 'active' : ''}">
                    <i>🗂️</i> Chuyên mục & Phòng ban
                </a>
            </div>
        </c:if>

        <div class="nav-group">
            <div class="nav-label">HỆ THỐNG</div>
            <a href="${base}/logout" class="nav-item" style="color: #e53e3e;">
                <i>🚪</i> Đăng xuất
            </a>
        </div>
    </nav>
    <div class="sidebar-bottom">
        <span class="demo">SWP391 — NHÓM 4</span>
        <small>Tòa Soạn Hội Tụ (NTMCS)</small>
    </div>
</aside>
