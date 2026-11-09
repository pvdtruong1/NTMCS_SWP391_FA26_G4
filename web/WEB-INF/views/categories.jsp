<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chuyên Mục & Phòng Ban — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
</head>
<body>
<div class="app">
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="categories"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="topbar.jsp">
            <jsp:param name="title" value="Cấu Hình Chuyên Mục & Phòng Ban"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">ORGANIZATION SETUP / WF-05</span>
                    <h1>Cấu Trúc Phòng Ban & Cây Chuyên Mục</h1>
                    <p>Quản lý sơ đồ cơ cấu tổ chức các Ban Biên Tập và cây phân cấp chuyên mục tin tức báo chí.</p>
                </div>
                <div class="page-actions">
                    <a href="${base}/categories?action=newDesk" class="btn" style="text-decoration: none;">
                        🏛️ + Thêm Phòng Ban
                    </a>
                    <a href="${base}/categories?action=newCategory" class="btn primary" style="text-decoration: none;">
                        🗂️ + Thêm Chuyên Mục
                    </a>
                </div>
            </div>

            <!-- Notifications -->
            <c:if test="${param.msg == 'deskCreated'}"><div style="background: #e8f7ef; color: #15734f; border: 1px solid #ccebdd; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">✓ Đã thêm ban chuyên môn mới thành công!</div></c:if>
            <c:if test="${param.msg == 'deskUpdated'}"><div style="background: #e8f7ef; color: #15734f; border: 1px solid #ccebdd; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">✓ Đã cập nhật ban chuyên môn thành công!</div></c:if>
            <c:if test="${param.msg == 'deskDeleted'}"><div style="background: #e8f7ef; color: #15734f; border: 1px solid #ccebdd; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">✓ Đã xóa ban chuyên môn thành công!</div></c:if>
            <c:if test="${param.msg == 'categoryCreated'}"><div style="background: #e8f7ef; color: #15734f; border: 1px solid #ccebdd; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">✓ Đã tạo chuyên mục mới thành công!</div></c:if>
            <c:if test="${param.msg == 'categoryUpdated'}"><div style="background: #e8f7ef; color: #15734f; border: 1px solid #ccebdd; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">✓ Đã cập nhật chuyên mục thành công!</div></c:if>
            <c:if test="${param.msg == 'categoryDeleted'}"><div style="background: #e8f7ef; color: #15734f; border: 1px solid #ccebdd; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">✓ Đã xóa chuyên mục thành công!</div></c:if>
            <c:if test="${param.msg == 'nochange'}"><div style="background: #fff4dd; color: #9a6200; border: 1px solid #f4ddb2; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">ℹ Không có thay đổi nào.</div></c:if>
            <c:if test="${not empty error}"><div style="background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">✕ <c:out value="${error}" /></div></c:if>

            <div class="grid2">
                <!-- Desks List Card -->
                <div class="card" style="display: flex; flex-direction: column;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;">
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <span style="font-size: 16px;">🏛️</span>
                            <h2 style="margin: 0; font-size: 15px; font-weight: 700;">Danh Sách Ban Chuyên Môn (Desks)</h2>
                        </div>
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <span class="badge" style="background: #eef2ff; color: var(--primary); font-weight: 700;">${deskList.size()} ban</span>
                            <a href="${base}/categories?action=newDesk" class="btn sm" style="text-decoration: none; padding: 4px 10px; font-size: 11.5px;">+ Thêm Ban</a>
                        </div>
                    </div>

                    <div class="table-scroll">
                        <table class="table-compact" style="width: 100%; min-width: 100%;">
                            <thead>
                                <tr>
                                    <th style="width: 15%; text-align: center;">MÃ</th>
                                    <th style="width: 55%;">TÊN BAN CHUYÊN MÔN</th>
                                    <th style="width: 30%; text-align: right;">THAO TÁC</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty deskList}">
                                        <tr>
                                            <td colspan="3" style="text-align: center; padding: 32px 16px; color: var(--muted);">
                                                Chưa có ban chuyên môn nào được khởi tạo.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="d" items="${deskList}">
                                            <tr>
                                                <td style="text-align: center;">
                                                    <span class="badge" style="background: #f1f5f9; color: #475569; font-weight: 700; font-family: monospace; font-size: 11px;">
                                                        #${d.deskId}
                                                    </span>
                                                </td>
                                                <td>
                                                    <strong style="color: var(--text); font-size: 13.5px;">🏛️ <c:out value="${d.deskName}" /></strong>
                                                </td>
                                                <td style="text-align: right;">
                                                    <div style="display: inline-flex; gap: 6px; justify-content: flex-end;">
                                                        <a href="${base}/categories?action=editDesk&id=${d.deskId}" class="btn sm" style="padding: 4px 8px; font-size: 11.5px; text-decoration: none;" title="Chỉnh sửa ban">
                                                            ✏️ Sửa
                                                        </a>
                                                        <form method="post" action="${base}/categories" style="display:inline; margin: 0;"
                                                              onsubmit="return confirm('Bạn có chắc chắn muốn xóa ban chuyên môn này?');">
                                                            <input type="hidden" name="action" value="deleteDesk">
                                                            <input type="hidden" name="id" value="${d.deskId}">
                                                            <button type="submit" class="btn sm danger" style="padding: 4px 8px; font-size: 11.5px;" title="Xóa ban">
                                                                🗑️ Xóa
                                                            </button>
                                                        </form>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Category Tree Card -->
                <div class="card" style="display: flex; flex-direction: column;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;">
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <span style="font-size: 16px;">🗂️</span>
                            <h2 style="margin: 0; font-size: 15px; font-weight: 700;">Cây Phân Cấp Chuyên Mục (Category Tree)</h2>
                        </div>
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <span class="badge" style="background: #eef2ff; color: var(--primary); font-weight: 700;">${categoryTree.size()} mục</span>
                            <a href="${base}/categories?action=newCategory" class="btn sm primary" style="text-decoration: none; padding: 4px 10px; font-size: 11.5px;">+ Thêm Chuyên Mục</a>
                        </div>
                    </div>

                    <div class="table-scroll">
                        <table class="table-compact" style="width: 100%; min-width: 100%;">
                            <thead>
                                <tr>
                                    <th style="width: 48%;">TÊN CHUYÊN MỤC</th>
                                    <th style="width: 28%;">TRỰC THUỘC BAN</th>
                                    <th style="width: 24%; text-align: right;">THAO TÁC</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty categoryTree}">
                                        <tr>
                                            <td colspan="3" style="text-align: center; padding: 32px 16px; color: var(--muted);">
                                                Chưa có chuyên mục nào được khởi tạo.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="c" items="${categoryTree}">
                                            <tr>
                                                <td>
                                                    <div style="display: flex; align-items: center;">
                                                        <c:choose>
                                                            <c:when test="${empty c.parentCategoryId}">
                                                                <span style="font-weight: 700; color: var(--text); font-size: 13.5px; display: inline-flex; align-items: center; gap: 6px;">
                                                                    <span style="font-size: 14px;">📁</span> <c:out value="${c.categoryName}" />
                                                                </span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span style="display: inline-flex; align-items: center; gap: 5px; font-size: 13px; color: var(--text); padding-left: 18px;">
                                                                    <span style="color: var(--muted); font-size: 13px;">└</span>
                                                                    <span style="font-size: 13px;">📄</span>
                                                                    <strong><c:out value="${c.categoryName}" /></strong>
                                                                </span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                </td>
                                                <td>
                                                    <span class="badge" style="background: var(--surface2); color: var(--text); border: 1px solid var(--line); font-weight: 600;">
                                                        🏛️ <c:out value="${deskNames[c.deskId]}" default="Chưa gắn ban" />
                                                    </span>
                                                </td>
                                                <td style="text-align: right;">
                                                    <div style="display: inline-flex; gap: 6px; justify-content: flex-end;">
                                                        <a href="${base}/categories?action=editCategory&id=${c.categoryId}" class="btn sm" style="padding: 4px 8px; font-size: 11.5px; text-decoration: none;" title="Chỉnh sửa chuyên mục">
                                                            ✏️ Sửa
                                                        </a>
                                                        <form method="post" action="${base}/categories" style="display:inline; margin: 0;"
                                                              onsubmit="return confirm('Bạn có chắc chắn muốn xóa chuyên mục này?');">
                                                            <input type="hidden" name="action" value="deleteCategory">
                                                            <input type="hidden" name="id" value="${c.categoryId}">
                                                            <button type="submit" class="btn sm danger" style="padding: 4px 8px; font-size: 11.5px;" title="Xóa chuyên mục">
                                                                🗑️ Xóa
                                                            </button>
                                                        </form>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>
</body>
</html>
