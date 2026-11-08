<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đề Xuất &amp; Xin Hỗ Trợ Đề Tài — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
    <link rel="stylesheet" href="${base}/css/reporter.css">
</head>
<body>
<div class="app">
    <jsp:include page="/WEB-INF/views/sidebar.jsp">
        <jsp:param name="active" value="reporter-requests"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="/WEB-INF/views/topbar.jsp">
            <jsp:param name="title" value="Đề Xuất &amp; Xin Hỗ Trợ Tác Nghiệp"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">PHÂN HỆ PHÓNG VIÊN / WF-01 &amp; WF-03</span>
                    <h1>Đề Xuất &amp; Xin Hỗ Trợ Tác Nghiệp</h1>
                    <p>Lập phiếu xin mượn trang thiết bị (xe, máy quay, flycam), xin gia hạn thời hạn nộp bài (Deadline), hoặc đề xuất bổ sung nhân sự nhóm.</p>
                </div>
                <div class="page-actions">
                    <a class="btn" href="${base}/reporter/topics">
                        <i>📑</i> <span>Đề tài của tôi</span>
                    </a>
                    <a class="btn" href="${base}/reporter/dashboard">
                        <i>📊</i> <span>Tổng quan phóng viên</span>
                    </a>
                </div>
            </div>

            <!-- Banner thông tin nghiệp vụ / Thẩm quyền đề xuất -->
            <div style="background: #eef2ff; color: #1e3a8a; border: 1px solid #c7d2fe; border-radius: 12px; padding: 14px 18px; margin-bottom: 22px; display: flex; align-items: center; gap: 14px;">
                <span style="font-size: 26px;">🛡️</span>
                <div>
                    <strong style="display: block; font-size: 14px; margin-bottom: 2px;">Quy định thẩm quyền đề xuất tác nghiệp:</strong>
                    <span style="font-size: 13px; color: #374151;">
                        Chỉ những đề tài đã chính thức được <strong>Phê duyệt (APPROVED)</strong> hoặc <strong>Đang tác nghiệp (IN_PROGRESS)</strong> bởi Ban Biên tập mới đủ điều kiện xin cấp phát tài sản thiết bị, gia hạn Deadline hoặc đề xuất điều chỉnh nhân sự nhóm.
                    </span>
                </div>
            </div>

            <!-- Bảng Danh sách Đề tài đủ điều kiện đề xuất (APPROVED) -->
            <div class="card">
                <div class="card-head" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;">
                    <div>
                        <h2 style="font-size: 16px; margin: 0; display: flex; align-items: center; gap: 8px;">
                            <span>📋</span> Danh Sách Đề Tài Đủ Điều Kiện Đề Xuất
                        </h2>
                        <small style="color: var(--muted);">Chỉ hiển thị các đề tài đang trong giai đoạn tác nghiệp thực địa (${approvedTopics != null ? approvedTopics.size() : 0} đề tài)</small>
                    </div>
                    <span class="badge b-approved">
                        Trạng thái: APPROVED &amp; IN_PROGRESS
                    </span>
                </div>

                <div class="table-scroll">
                    <table>
                        <thead>
                            <tr>
                                <th style="width: 130px;">Mã đề tài</th>
                                <th>Tiêu đề đề tài &amp; Góc tiếp cận</th>
                                <th style="width: 170px;">Chuyên mục</th>
                                <th style="width: 130px;">Hạn nộp bài</th>
                                <th style="width: 130px;">Trạng thái</th>
                                <th style="width: 160px; text-align: right;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="t" items="${approvedTopics}">
                                <tr>
                                    <td>
                                        <b style="color: var(--primary); font-family: monospace;">
                                            <c:out value="${t.topicCode != null ? t.topicCode : ('#' += t.topicId)}"/>
                                        </b>
                                    </td>
                                    <td>
                                        <a class="row-link" href="${base}/reporter/requests?topicId=${t.topicId}">
                                            <strong><c:out value="${t.title}"/></strong>
                                        </a>
                                        <small style="color: var(--muted); margin-top: 3px; display: block;">
                                            <c:out value="${t.angle != null ? t.angle : t.summary}"/>
                                        </small>
                                    </td>
                                    <td>
                                        <span class="badge" style="background: var(--surface2); border: 1px solid var(--line);">
                                            <c:out value="${t.categoryName != null ? t.categoryName : 'Chung'}"/>
                                        </span>
                                    </td>
                                    <td>
                                        <span style="font-size: 12px; font-weight: 600; color: var(--danger);">
                                            📅 <c:out value="${t.deadline}"/>
                                        </span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${t.status eq 'APPROVED'}">
                                                <span class="badge b-approved">✓ APPROVED</span>
                                            </c:when>
                                            <c:when test="${t.status eq 'IN_PROGRESS'}">
                                                <span class="badge" style="background: #e0f2fe; color: #0369a1; border: 1px solid #bae6fd;">⚡ IN_PROGRESS</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge"><c:out value="${t.status}"/></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align: right;">
                                        <a href="${base}/reporter/requests?topicId=${t.topicId}" class="btn primary sm" style="text-decoration: none; display: inline-flex; align-items: center; gap: 4px;">
                                            <span>📋</span> <span>Tạo đề xuất &rarr;</span>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>

                            <c:if test="${empty approvedTopics}">
                                <tr>
                                    <td colspan="6" style="text-align: center; padding: 48px 20px; color: var(--muted);">
                                        <div style="font-size: 32px; margin-bottom: 8px;">📭</div>
                                        <div style="font-weight: 600; font-size: 15px; color: var(--text); margin-bottom: 4px;">
                                            Hiện chưa có đề tài nào của bạn ở trạng thái ĐÃ DUYỆT (APPROVED) hoặc ĐANG TÁC NGHIỆP (IN_PROGRESS)
                                        </div>
                                        <p style="margin: 0 0 16px 0; font-size: 13px;">
                                            Các đề tài đang là bản nháp (DRAFT) hoặc chờ thẩm định (SUBMITTED) cần được Ban Biên tập phê duyệt trước khi lập phiếu xin tài sản hay nhân sự.
                                        </p>
                                        <a href="${base}/reporter/topics" class="btn sm" style="text-decoration: none;">
                                            &larr; Xem danh sách đề tài của tôi
                                        </a>
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>
</div>
</body>
</html>
