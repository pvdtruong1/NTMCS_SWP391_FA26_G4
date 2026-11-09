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
        <c:if test="${not empty sessionScope.permissions and sessionScope.permissions.contains('TOPIC_PITCH')}">
            <div class="nav-group">
                <div class="nav-label">PHÂN HỆ PHÓNG VIÊN (WF-01)</div>
                <a href="${base}/reporter/dashboard" class="nav-item ${param.active == 'reporter-dashboard' ? 'active' : ''}">
                    <i>📊</i> Tổng quan phóng viên
                </a>
                <a href="${base}/reporter/compose" class="nav-item ${param.active == 'reporter-compose' ? 'active' : ''}">
                    <i>✍️</i> Đề xuất đề tài mới
                </a>
                <a href="${base}/reporter/topics" class="nav-item ${param.active == 'reporter-topics' ? 'active' : ''}">
                    <i>📑</i> Quản lý đề tài của tôi
                </a>
                <a href="${base}/reporter/duplicate-search" class="nav-item ${param.active == 'reporter-search' ? 'active' : ''}">
                    <i>🔍</i> Tra cứu trùng lặp
                </a>
                <a href="${base}/reporter/requests" class="nav-item ${param.active == 'reporter-requests' ? 'active' : ''}">
                    <i>🤝</i> Đề xuất xin hỗ trợ
                </a>
            </div>
        </c:if>

        <c:if test="${not empty sessionScope.permissions and sessionScope.permissions.contains('TOPIC_EVALUATE')}">
            <div class="nav-group">
                <div class="nav-label">PHÂN HỆ BTV CHUYÊN MÔN (WF-02)</div>
                <a href="${base}/editorial/dashboard" class="nav-item ${param.active == 'editorial-dashboard' ? 'active' : ''}">
                    <i>📊</i> Dashboard Điều phối & Nghiệm thu
                </a>
                <a href="${base}/editorial/pending-queue" class="nav-item ${param.active == 'pending-queue' or param.active == 'criteria-scoring' ? 'active' : ''}">
                    <i>📋</i> Hàng đợi chờ thẩm định
                </a>
                <a href="${base}/editorial/directive-creation" class="nav-item ${param.active == 'directive-creation' or param.active == 'directive-topic' or param.active == 'reporter-dispatch' ? 'active' : ''}">
                    <i>⚡</i> Đặt hàng đề tài chỉ đạo
                </a>
                <a href="${base}/editorial/idea-archive" class="nav-item ${param.active == 'idea-archive' ? 'active' : ''}">
                    <i>🗄️</i> Kho lưu trữ ý tưởng
                </a>
            </div>
        </c:if>

        <c:if test="${sessionScope.loggedUser.roleId == 4 or sessionScope.user.roleId == 4 or sessionScope.loggedUser.roleName == 'LOGISTICS_COORDINATOR' or sessionScope.user.roleName == 'LOGISTICS_COORDINATOR' or sessionScope.loggedUser.roleId == 7 or sessionScope.user.roleId == 7 or sessionScope.loggedUser.roleName == 'SYSTEM_ADMINISTRATOR' or sessionScope.user.roleName == 'SYSTEM_ADMINISTRATOR' or (not empty sessionScope.permissions and sessionScope.permissions.contains('LOGISTICS_MANAGE'))}">
            <div class="nav-group">
                <div class="nav-label">HẬU CẦN &amp; THIẾT BỊ (WF-03)</div>
                <a href="${base}/assets" class="nav-item ${param.active == 'assets' ? 'active' : ''}">
                    <i>📦</i> Danh sách trang thiết bị
                </a>
                <a href="${base}/asset-requests" class="nav-item ${param.active == 'asset-requests' ? 'active' : ''}">
                    <i>📋</i> Quản lý mượn &amp; Điều phối
                </a>
            </div>
        </c:if>

        <c:if test="${not empty sessionScope.permissions and (sessionScope.permissions.contains('FIELDWORK_TRACK') or sessionScope.permissions.contains('TOPIC_PITCH') or sessionScope.permissions.contains('TOPIC_EVALUATE'))}">
            <div class="nav-group">
                <div class="nav-label">HIỆN TRƯỜNG &amp; ĐIỀU PHỐI (WF-04)</div>
                <c:if test="${sessionScope.permissions.contains('FIELDWORK_TRACK') or sessionScope.permissions.contains('TOPIC_EVALUATE')}">
                    <a href="${base}/fieldwork/kanban" class="nav-item ${param.active == 'fieldwork-kanban' ? 'active' : ''}">
                        <i>📌</i> Bảng Kanban tác nghiệp
                    </a>
                </c:if>
                <c:if test="${sessionScope.permissions.contains('TOPIC_PITCH') or sessionScope.permissions.contains('FIELDWORK_TRACK')}">
                    <a href="${base}/fieldwork/todo" class="nav-item ${param.active == 'fieldwork-todo' ? 'active' : ''}">
                        <i>✅</i> Việc hiện trường của tôi
                    </a>
                </c:if>
                <c:if test="${sessionScope.permissions.contains('FIELDWORK_TRACK') or sessionScope.permissions.contains('TOPIC_EVALUATE')}">
                    <a href="${base}/fieldwork/nudge" class="nav-item ${param.active == 'fieldwork-nudge' ? 'active' : ''}">
                        <i>🔔</i> Đôn đốc tiến độ (Nudge)
                    </a>
                    <a href="${base}/fieldwork/audit" class="nav-item ${param.active == 'fieldwork-audit' ? 'active' : ''}">
                        <i>📡</i> Nhật ký phát thanh &amp; Ping
                    </a>
                    <a href="${base}/fieldwork/sla" class="nav-item ${param.active == 'fieldwork-sla' ? 'active' : ''}">
                        <i>⏱️</i> Cấu hình hạn mức SLA
                    </a>
                </c:if>
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
                <div class="nav-label">ĐIỀU HÀNH &amp; CHỈ TIÊU (WF-05)</div>
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