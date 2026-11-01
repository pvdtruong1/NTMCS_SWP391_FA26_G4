<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${editUser.userId > 0}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${isEdit ? 'Chỉnh Sửa Tài Khoản' : 'Thêm Tài Khoản Mới'} — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
</head>
<body>
<div class="app">
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="users"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="topbar.jsp">
            <jsp:param name="title" value="Quản Lý Tài Khoản"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">GOVERNANCE &amp; ADMINISTRATION / WF-05</span>
                    <h1>${isEdit ? 'Chỉnh Sửa Thông Tin Tài Khoản' : 'Tạo Tài Khoản Người Dùng Mới'}</h1>
                    <p>Thiết lập thông tin định danh, chức vụ, vai trò và ban chuyên môn hoạt động.</p>
                </div>
                <div class="page-actions">
                    <a href="${base}/users" class="btn" style="text-decoration: none;">
                        &larr; Quay lại danh sách
                    </a>
                </div>
            </div>

            <c:if test="${not empty error}">
                <div style="background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce; border-radius: 10px; padding: 12px 16px; margin-bottom: 20px; font-weight: 600;">
                    ✕ <c:out value="${error}" />
                </div>
            </c:if>

            <div class="card" style="max-width: 800px;">
                <form method="post" action="${base}/users">
                    <input type="hidden" name="action" value="${isEdit ? 'update' : 'create'}">
                    <input type="hidden" name="id" value="${editUser.userId}">

                    <div class="form-grid">
                        <div class="field">
                            <span>HỌ VÀ TÊN <b style="color: var(--danger);">*</b></span>
                            <input type="text" name="fullName" value="<c:out value='${editUser.fullName}' />" required placeholder="Ví dụ: Nguyễn Văn A">
                        </div>

                        <div class="field">
                            <span>EMAIL ĐĂNG NHẬP <b style="color: var(--danger);">*</b></span>
                            <input type="email" name="email" value="<c:out value='${editUser.email}' />" ${isEdit ? 'readonly style="background: var(--surface2);"' : ''} required placeholder="name@newsroom.vn">
                        </div>

                        <div class="field">
                            <span>${isEdit ? 'MẬT KHẨU MỚI (BỎ TRỐNG NẾU GIỮ NGUYÊN)' : 'MẬT KHẨU (TỐI THIỂU 6 KÝ TỰ)'} ${isEdit ? '' : '<b style="color: var(--danger);">*</b>'}</span>
                            <input type="password" name="password" ${isEdit ? '' : 'required'} placeholder="••••••">
                        </div>

                        <div class="field">
                            <span>VAI TRÒ HỆ THỐNG (ROLE) <b style="color: var(--danger);">*</b></span>
                            <select name="roleId" required>
                                <c:forEach var="r" items="${roleList}">
                                    <option value="${r.roleId}" ${r.roleId == editUser.roleId ? 'selected' : ''}>${r.roleName}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="field full">
                            <span>PHÒNG BAN CHUYÊN MÔN (DESK)</span>
                            <select name="deskId">
                                <option value="">-- Toàn Tòa Soạn / Không trực thuộc ban --</option>
                                <c:forEach var="d" items="${deskList}">
                                    <option value="${d.deskId}" ${d.deskId == editUser.deskId ? 'selected' : ''}>🏛️ <c:out value="${d.deskName}" /></option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <div style="display: flex; gap: 10px; margin-top: 24px; padding-top: 20px; border-top: 1px solid var(--line);">
                        <button type="submit" class="btn primary">
                            💾 Lưu Thông Tin
                        </button>
                        <a href="${base}/users" class="btn" style="text-decoration: none;">
                            Hủy bỏ
                        </a>
                    </div>
                </form>
            </div>
        </main>
    </div>
</div>
</body>
</html>
