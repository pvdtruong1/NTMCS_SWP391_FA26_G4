<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chuyên Mục &amp; Phòng Ban — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
</head>
<body>
<div class="app">
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="categories"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="topbar.jsp">
            <jsp:param name="title" value="Cấu Hình Chuyên Mục &amp; Phòng Ban"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">ORGANIZATION SETUP / WF-05</span>
                    <h1>Cấu Trúc Phòng Ban &amp; Cây Chuyên Mục</h1>
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
                <div class="card">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;">
                        <h2 style="margin: 0; font-size: 16px;">Danh Sách Ban Chuyên Môn (Desks)</h2>
                        <a href="${base}/categories?action=newDesk" class="btn sm" style="text-decoration: none;">+ Thêm Ban</a>
                    </div>

                    <div class="table-scroll">
                        <table>
                            <thead>
                                <tr>
                                    <th style="width: 60px;">MÃ</th>
                                    <th>TÊN BAN CHUYÊN MÔN</th>
                                    <th style="text-align: right;">THAO TÁC</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="d" items="${deskList}">
                                    <tr>
                                        <td><strong>#${d.deskId}</strong></td>
                                        <td><strong>🏛️ <c:out value="${d.deskName}" /></strong></td>
                                        <td style="text-align: right;">
                                            <div style="display: inline-flex; gap: 6px;">
                                                <a href="${base}/categories?action=editDesk&id=${d.deskId}" class="btn sm" style="text-decoration: none;">✏️ Sửa</a>
                                                <form method="post" action="${base}/categories" style="display:inline; margin: 0;"
                                                      onsubmit="return confirm('Bạn có chắc chắn muốn xóa ban chuyên môn này?');">
                                                    <input type="hidden" name="action" value="deleteDesk">
                                                    <input type="hidden" name="id" value="${d.deskId}">
                                                    <button type="submit" class="btn sm danger">🗑️ Xóa</button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Category Tree Card -->
                <div class="card">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;">
                        <h2 style="margin: 0; font-size: 16px;">Cây Phân Cấp Chuyên Mục (Category Tree)</h2>
                        <a href="${base}/categories?action=newCategory" class="btn sm primary" style="text-decoration: none;">+ Thêm Chuyên Mục</a>
                    </div>

                    <div class="table-scroll">
                        <table>
                            <thead>
                                <tr>
                                    <th>TÊN CHUYÊN MỤC</th>
                                    <th>TRỰC THUỘC BAN</th>
                                    <th style="text-align: right;">THAO TÁC</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="c" items="${categoryTree}">
                                    <tr>
                                        <td>
                                            ${indents[c.categoryId]}<strong><c:out value="${c.categoryName}" /></strong>
                                        </td>
                                        <td>
                                            <small style="color: var(--muted);">🏛️ <c:out value="${deskNames[c.deskId]}" /></small>
                                        </td>
                                        <td style="text-align: right;">
                                            <div style="display: inline-flex; gap: 6px;">
                                                <a href="${base}/categories?action=editCategory&id=${c.categoryId}" class="btn sm" style="text-decoration: none;">✏️ Sửa</a>
                                                <form method="post" action="${base}/categories" style="display:inline; margin: 0;"
                                                      onsubmit="return confirm('Bạn có chắc chắn muốn xóa chuyên mục này?');">
                                                    <input type="hidden" name="action" value="deleteCategory">
                                                    <input type="hidden" name="id" value="${c.categoryId}">
                                                    <button type="submit" class="btn sm danger">🗑️ Xóa</button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
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
