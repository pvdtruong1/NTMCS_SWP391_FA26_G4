<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${quota.quotaId > 0}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${isEdit ? 'Chỉnh Sửa Chỉ Tiêu' : 'Giao Chỉ Tiêu Mới'} — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
</head>
<body>
<div class="app">
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="quota"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="topbar.jsp">
            <jsp:param name="title" value="Quản Lý Chỉ Tiêu Sản Xuất"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">PRODUCTION PLANNING / WF-05</span>
                    <h1>${isEdit ? 'Cập Nhật Chỉ Tiêu Sản Xuất' : 'Thiết Lập Chỉ Tiêu Sản Xuất Mới'}</h1>
                    <p>Phân bổ định mức bài viết theo kỳ hoạt động cho từng ban chuyên môn tòa soạn.</p>
                </div>
                <div class="page-actions">
                    <a href="${base}/quota" class="btn" style="text-decoration: none;">
                        &larr; Quay lại danh sách
                    </a>
                </div>
            </div>

            <c:if test="${not empty error}">
                <div style="background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce; border-radius: 10px; padding: 12px 16px; margin-bottom: 20px; font-weight: 600;">
                    ✕ <c:out value="${error}" />
                </div>
            </c:if>

            <div class="card" style="max-width: 650px;">
                <form method="post" action="${base}/quota">
                    <input type="hidden" name="action" value="${isEdit ? 'update' : 'create'}">
                    <input type="hidden" name="id" value="${quota.quotaId}">

                    <div class="form-grid">
                        <div class="field full">
                            <span>PHÒNG BAN CHUYÊN MÔN (DESK) <b style="color: var(--danger);">*</b></span>
                            <c:choose>
                                <c:when test="${isEdit}">
                                    <input type="text" value="<c:out value='${quota.deskName}' />" readonly style="background: var(--surface2);">
                                </c:when>
                                <c:otherwise>
                                    <select name="deskId" required>
                                        <option value="">-- Chọn ban chuyên môn --</option>
                                        <c:forEach var="d" items="${deskList}">
                                            <option value="${d.deskId}" ${d.deskId == quota.deskId ? 'selected' : ''}>🏛️ <c:out value="${d.deskName}" /></option>
                                        </c:forEach>
                                    </select>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="field">
                            <span>KỲ SẢN XUẤT (PERIOD) <b style="color: var(--danger);">*</b></span>
                            <input type="text" name="period" value="<c:out value='${quota.period}' />"
                                   placeholder="Ví dụ: 2026-10 hoặc 2026-Q4" ${isEdit ? 'readonly style="background: var(--surface2);"' : ''} required>
                            <small style="color: var(--muted); font-size: 11px;">Định dạng tháng: <code>YYYY-MM</code> hoặc quý: <code>YYYY-QX</code></small>
                        </div>

                        <div class="field">
                            <span>ĐỊNH MỨC ĐỀ TÀI (QUOTA) <b style="color: var(--danger);">*</b></span>
                            <input type="number" name="quota" value="<c:out value='${countText}' />" min="0" required placeholder="Số lượng bài">
                        </div>
                    </div>

                    <div style="display: flex; gap: 10px; margin-top: 24px; padding-top: 20px; border-top: 1px solid var(--line);">
                        <button type="submit" class="btn primary">
                            💾 Lưu Chỉ Tiêu
                        </button>
                        <a href="${base}/quota" class="btn" style="text-decoration: none;">
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
