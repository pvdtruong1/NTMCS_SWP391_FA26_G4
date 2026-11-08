<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đề Xuất Xin Hỗ Trợ Đề Tài — NTMCS</title>
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
            <jsp:param name="title" value="Đề xuất xin hỗ trợ"/>
        </jsp:include>

        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">PHÂN HỆ PHÓNG VIÊN / WF-01</span>
                    <h1>Đề Xuất &amp; Xin Hỗ Trợ Đề Tài</h1>
                    <p>Khởi tạo đề xuất xin cấp phát trang thiết bị tác nghiệp, gia hạn thời hạn nộp bài hoặc điều chỉnh nhân sự nhóm.</p>
                </div>
                <div class="page-actions">
                    <a class="btn" href="${base}/reporter/topics">
                        <i>📋</i> <span>Đề tài của tôi</span>
                    </a>
                </div>
            </div>

            <!-- Banner thông tin nghiệp vụ chuẩn NTMCS -->
            <div style="background: #e8f4ff; color: #1e40af; border: 1px solid #bfdbfe; border-radius: 12px; padding: 14px 18px; margin-bottom: 22px; display: flex; align-items: flex-start; gap: 14px; box-shadow: 0 2px 6px rgba(37,99,235,0.06);">
                <div style="font-size: 22px; line-height: 1;">ℹ️</div>
                <div style="font-size: 13.5px; line-height: 1.5;">
                    <strong style="display: block; margin-bottom: 2px; color: #1e3a8a;">Quy định thẩm quyền đề xuất:</strong>
                    <span>Chỉ những đề tài đã chính thức được <strong>Phê duyệt (APPROVED)</strong> hoặc đang trong giai đoạn <strong>Triển khai tác nghiệp (IN_PROGRESS)</strong> mới đủ điều kiện xin cấp phát tài sản thiết bị, gia hạn hạn nộp bài hoặc xin điều chỉnh nhân sự nhóm.</span>
                </div>
            </div>

            <!-- Danh sách Đề tài đã APPROVED -->
            <div class="card">
                <div class="card-title" style="margin-bottom: 16px;">
                    <div>
                        <h2 style="margin: 0; font-size: 17px; font-weight: 800;">
                            Danh sách đề tài đủ điều kiện đề xuất (${not empty approvedTopics ? fn:length(approvedTopics) : 0})
                        </h2>
                        <small style="color: var(--muted); display: block; margin-top: 3px;">
                            Các đề tài đang trong giai đoạn tác nghiệp thực địa được phép gửi yêu cầu hỗ trợ.
                        </small>
                    </div>
                    <div>
                        <span class="badge b-approved">
                            ✓ APPROVED &amp; IN_PROGRESS
                        </span>
                    </div>
                </div>

                <div class="table-scroll">
                    <table>
                        <thead>
                            <tr>
                                <th style="width: 140px;">Mã đề tài</th>
                                <th>Tiêu đề đề tài &amp; Góc tiếp cận</th>
                                <th style="width: 160px;">Chuyên mục</th>
                                <th style="width: 150px;">Hạn nộp bài</th>
                                <th style="width: 120px;">Trạng thái</th>
                                <th style="width: 150px; text-align: right;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="t" items="${approvedTopics}">
                                <tr>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty t.topicCode}">
                                                <strong style="color: var(--primary);"><c:out value="${t.topicCode}"/></strong>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color: var(--muted); font-weight: 600;">#${t.topicId}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <a href="${base}/reporter/requests?topicId=${t.topicId}" class="row-link" style="font-weight: 700;">
                                            <c:out value="${t.title}"/>
                                        </a>
                                        <small style="color: var(--muted);">
                                            <c:out value="${t.angle != null ? t.angle : t.summary}"/>
                                        </small>
                                    </td>
                                    <td>
                                        <span class="tag">
                                            <c:out value="${t.categoryName != null ? t.categoryName : 'Chung'}"/>
                                        </span>
                                    </td>
                                    <td>
                                        <strong style="color: var(--danger); font-size: 13px;">
                                            📅 <c:out value="${t.deadline != null ? t.deadline : 'Chưa đặt'}"/>
                                        </strong>
                                    </td>
                                    <td>
                                        <span class="badge b-${t.status.toLowerCase()}">${t.status}</span>
                                    </td>
                                    <td style="text-align: right;">
                                        <a href="${base}/reporter/requests?topicId=${t.topicId}" class="btn primary sm" style="display: inline-flex; align-items: center; gap: 5px;">
                                            <i>🤝</i> <span>Tạo đề xuất</span>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>

                            <c:if test="${empty approvedTopics}">
                                <tr>
                                    <td colspan="6" style="padding: 40px 20px; text-align: center;">
                                        <div style="color: var(--muted);">
                                            <div style="font-size: 32px; margin-bottom: 8px;">📭</div>
                                            <p style="font-size: 14px; font-weight: 700; margin: 0 0 6px; color: var(--text);">
                                                Hiện chưa có đề tài nào của bạn ở trạng thái ĐÃ DUYỆT (APPROVED) hoặc ĐANG TÁC NGHIỆP (IN_PROGRESS).
                                            </p>
                                            <small style="color: var(--muted); display: block; max-width: 480px; margin: 0 auto 16px;">
                                                Các đề tài đang là bản nháp (DRAFT) hoặc đang chờ thẩm định (SUBMITTED) cần được Ban Biên tập duyệt trước khi xin cấp tài sản hay nhân sự.
                                            </small>
                                            <a href="${base}/reporter/topics" class="btn sm">
                                                ← Xem danh sách đề tài của tôi
                                            </a>
                                        </div>
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
