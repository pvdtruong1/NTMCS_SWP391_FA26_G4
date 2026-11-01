<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cấu Hình Tiêu Chí Rubric — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
</head>
<body>
<div class="app">
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="criteria"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="topbar.jsp">
            <jsp:param name="title" value="Cấu Hình Tiêu Chí Thẩm Định (Rubric)"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">QUALITY GOVERNANCE / WF-05</span>
                    <h1>Bộ Tiêu Chí Thẩm Định Đề Tài (Rubric Matrix)</h1>
                    <p>Cấu hình danh mục tiêu chí và trọng số (%) phục vụ BTV chuyên môn chấm điểm đề xuất đề tài (Tổng trọng số bắt buộc = 100%).</p>
                </div>
            </div>

            <c:if test="${param.msg == 'saved'}">
                <div style="background: #e8f7ef; color: #15734f; border: 1px solid #ccebdd; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">
                    ✓ Đã lưu cấu hình bộ tiêu chí Rubric thành công!
                </div>
            </c:if>
            <c:if test="${param.msg == 'nochange'}">
                <div style="background: #fff4dd; color: #9a6200; border: 1px solid #f4ddb2; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">
                    ℹ Không có thay đổi nào trong bảng tiêu chí.
                </div>
            </c:if>
            <c:if test="${not empty error}">
                <div style="background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">
                    ✕ <c:out value="${error}" />
                </div>
            </c:if>

            <div class="card">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding-bottom: 14px; border-bottom: 1px solid var(--line);">
                    <div>
                        <h2 style="margin: 0; font-size: 16px;">Bảng Trọng Số Tiêu Chí</h2>
                        <small style="color: var(--muted);">Điền thông tin vào dòng trống cuối bảng để thêm tiêu chí mới. Tiêu chí đã sử dụng chỉ có thể đặt trọng số về 0%.</small>
                    </div>
                    <c:if test="${total != null}">
                        <div style="padding: 6px 14px; border-radius: 8px; font-weight: 800; font-size: 14px; background: ${total == '100' or total == '100.0' or total == '100.00' ? '#e8f7ef' : '#ffedf0'}; color: ${total == '100' or total == '100.0' or total == '100.00' ? '#15734f' : '#bd3344'};">
                            Tổng Trọng Số: ${total}%
                        </div>
                    </c:if>
                </div>

                <form method="post" action="${base}/criteria">
                    <div class="table-scroll">
                        <table>
                            <thead>
                                <tr>
                                    <th style="width: 70px;">MÃ</th>
                                    <th>TÊN TIÊU CHÍ ĐÁNH GIÁ (CRITERION NAME)</th>
                                    <th style="width: 140px; text-align: center;">TRỌNG SỐ (%)</th>
                                    <th style="width: 90px; text-align: center;">XÓA</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="r" items="${rows}">
                                    <tr>
                                        <td>
                                            <c:choose>
                                                <c:when test="${r[0] == '0'}">
                                                    <span class="badge" style="background: #eef2ff; color: var(--primary);">Mới</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <strong>#${r[0]}</strong>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <input type="hidden" name="criteriaId" value="${r[0]}">
                                            <input type="text" name="criteriaName" value="<c:out value='${r[1]}' />"
                                                   style="width: 100%; padding: 8px 12px; border: 1px solid var(--line); border-radius: 8px;"
                                                   placeholder="${r[0] == '0' ? 'Nhập tên tiêu chí mới cần thêm...' : ''}">
                                        </td>
                                        <td style="text-align: center;">
                                            <input type="number" step="0.01" name="weight" value="<c:out value='${r[2]}' />"
                                                   style="width: 90px; padding: 8px 10px; text-align: center; border: 1px solid var(--line); border-radius: 8px; font-weight: 700;">
                                        </td>
                                        <td style="text-align: center;">
                                            <c:if test="${r[0] != '0'}">
                                                <input type="checkbox" name="remove" value="${r[0]}" ${r[3] == '1' ? 'checked' : ''}
                                                       style="width: 18px; height: 18px; cursor: pointer;">
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
                        <button type="submit" class="btn primary">
                            💾 Lưu Ma Trận Tiêu Chí
                        </button>
                    </div>
                </form>
            </div>
        </main>
    </div>
</div>
</body>
</html>
