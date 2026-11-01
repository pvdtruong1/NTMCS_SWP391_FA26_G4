<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${desk.deskId > 0}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${isEdit ? 'Chỉnh Sửa Ban Chuyên Môn' : 'Thêm Ban Chuyên Môn Mới'} — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
</head>
<body>
<div class="app">
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="categories"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="topbar.jsp">
            <jsp:param name="title" value="Cấu Hình Ban Chuyên Môn"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">ORGANIZATION SETUP / WF-05</span>
                    <h1>${isEdit ? 'Chỉnh Sửa Ban Chuyên Môn' : 'Tạo Ban Chuyên Môn Mới'}</h1>
                    <p>Thiết lập thông tin định danh ban biên tập chuyên trách trong tòa soạn.</p>
                </div>
                <div class="page-actions">
                    <a href="${base}/categories" class="btn" style="text-decoration: none;">
                        &larr; Quay lại danh sách
                    </a>
                </div>
            </div>

            <c:if test="${not empty error}">
                <div style="background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce; border-radius: 10px; padding: 12px 16px; margin-bottom: 20px; font-weight: 600;">
                    ✕ <c:out value="${error}" />
                </div>
            </c:if>

            <div class="card" style="max-width: 600px;">
                <form method="post" action="${base}/categories">
                    <input type="hidden" name="action" value="${isEdit ? 'updateDesk' : 'createDesk'}">
                    <input type="hidden" name="id" value="${desk.deskId}">

                    <div class="field full">
                        <span>TÊN BAN CHUYÊN MÔN (DESK NAME) <b style="color: var(--danger);">*</b></span>
                        <input type="text" name="deskName" value="<c:out value='${desk.deskName}' />" required placeholder="Ví dụ: Ban Thời sự - Chính trị">
                    </div>

                    <div style="display: flex; gap: 10px; margin-top: 24px; padding-top: 20px; border-top: 1px solid var(--line);">
                        <button type="submit" class="btn primary">
                            💾 Lưu Ban Chuyên Môn
                        </button>
                        <a href="${base}/categories" class="btn" style="text-decoration: none;">
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
