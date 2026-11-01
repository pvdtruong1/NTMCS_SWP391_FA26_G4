<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đề tài của tôi — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
    <link rel="stylesheet" href="${base}/css/reporter.css">
</head>
<body>
<div class="app">
    <jsp:include page="/WEB-INF/views/sidebar.jsp">
        <jsp:param name="active" value="reporter-topics"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="/WEB-INF/views/topbar.jsp">
            <jsp:param name="title" value="Quản Lý Đề Tài Của Tôi"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">PHÂN HỆ PHÓNG VIÊN / WF-01</span>
                    <h1>Danh Sách Đề Tài Của Tôi</h1>
                    <p>Quản lý toàn bộ danh sách các đề xuất đề tài do bạn khởi tạo, theo dõi tình trạng thẩm định và phản hồi.</p>
                </div>
                <div class="page-actions">
                    <a class="btn" href="${base}/reporter/duplicate-search">
                        <i>🔍</i> <span>Tra cứu trùng lặp</span>
                    </a>
                    <a class="btn primary" href="${base}/reporter/compose">
                        <i>＋</i> <span>Soạn đề tài mới</span>
                    </a>
                </div>
            </div>

            <c:if test="${param.message == 'submitted'}">
                <div style="background: #e8f7ef; color: var(--success); border: 1px solid #ccebdd; border-radius: 10px; padding: 12px 16px; margin-bottom: 20px; font-weight: 600;">
                    ✓ Đề tài đã được nộp thành công lên Ban Biên tập để thẩm định.
                </div>
            </c:if>
            <c:if test="${param.message == 'draftSaved'}">
                <div style="background: #e8f4ff; color: #2563eb; border: 1px solid #bfdbfe; border-radius: 10px; padding: 12px 16px; margin-bottom: 20px; font-weight: 600;">
                    ✓ Bản nháp đề tài đã được lưu thành công.
                </div>
            </c:if>
            <c:if test="${param.message == 'deleted'}">
                <div style="background: #e8f7ef; color: var(--success); border: 1px solid #ccebdd; border-radius: 10px; padding: 12px 16px; margin-bottom: 20px; font-weight: 600;">
                    ✓ Đã xóa bản nháp đề tài thành công.
                </div>
            </c:if>
            <c:if test="${param.message == 'submitFailed'}">
                <div style="background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce; border-radius: 10px; padding: 12px 16px; margin-bottom: 20px; font-weight: 600;">
                    ✕ Không thể nộp đề tài. Vui lòng kiểm tra lại trạng thái hoặc thông tin đề tài.
                </div>
            </c:if>
            <c:if test="${param.message == 'deleteFailed'}">
                <div style="background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce; border-radius: 10px; padding: 12px 16px; margin-bottom: 20px; font-weight: 600;">
                    ✕ Không thể xóa đề tài (chỉ được phép xóa đề tài ở trạng thái Bản nháp DRAFT).
                </div>
            </c:if>

            <form class="filterbar" method="get" action="${base}/reporter/topics">
                <input name="keyword" value="<c:out value='${keyword}'/>" placeholder="⌕ Tìm theo mã, tiêu đề, góc tiếp cận..." style="flex:1;max-width:380px">

                <select name="status">
                    <option value="">Tất cả trạng thái</option>
                    <option value="DRAFT" ${status eq 'DRAFT' ? 'selected' : ''}>Bản nháp (DRAFT)</option>
                    <option value="SUBMITTED" ${status eq 'SUBMITTED' ? 'selected' : ''}>Đã nộp duyệt (SUBMITTED)</option>
                    <option value="REVISION_REQUESTED" ${status eq 'REVISION_REQUESTED' ? 'selected' : ''}>Yêu cầu chỉnh sửa (REVISION_REQUESTED)</option>
                    <option value="APPROVED" ${status eq 'APPROVED' ? 'selected' : ''}>Đã phê duyệt (APPROVED)</option>
                    <option value="IN_PROGRESS" ${status eq 'IN_PROGRESS' ? 'selected' : ''}>Đang tác nghiệp (IN_PROGRESS)</option>
                    <option value="ACCEPTED_READY_FOR_CMS" ${status eq 'ACCEPTED_READY_FOR_CMS' ? 'selected' : ''}>Sẵn sàng CMS (ACCEPTED)</option>
                    <option value="REJECTED" ${status eq 'REJECTED' ? 'selected' : ''}>Bị từ chối (REJECTED)</option>
                </select>

                <button class="btn" type="submit">Lọc</button>
                <a class="btn" href="${base}/reporter/topics">Xóa lọc</a>

                <div style="margin-left:auto;color:var(--muted);font-size:12.5px;align-self:center">
                    Hiển thị <strong>${topics.size()}</strong> đề tài
                </div>
            </form>

            <div class="table-scroll">
                <table>
                    <thead>
                        <tr>
                            <th>Mã</th>
                            <th>Tiêu đề đề tài &amp; Góc tiếp cận</th>
                            <th>Chuyên mục</th>
                            <th>Thể loại</th>
                            <th>Ưu tiên</th>
                            <th>Deadline</th>
                            <th>Trạng thái</th>
                            <th style="text-align:right">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="t" items="${topics}">
                            <tr>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty t.topicCode}">
                                            <strong><c:out value="${t.topicCode}"/></strong>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color:var(--muted);font-weight:600">#${t.topicId}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td style="max-width:340px">
                                    <a class="row-link" href="${base}/reporter/detail?id=${t.topicId}">
                                        <strong><c:out value="${t.title}"/></strong>
                                    </a>
                                    <c:if test="${not empty t.angle}">
                                        <small style="color:var(--muted);margin-top:2px;display:block">
                                            <span style="font-weight:600">Angle:</span> <c:out value="${t.angle}"/>
                                        </small>
                                    </c:if>
                                    <c:if test="${not empty t.rejectionReason}">
                                        <small style="color:var(--danger);font-weight:600;margin-top:4px;display:block">
                                            &ldquo;<c:out value="${t.rejectionReason}"/>&rdquo;
                                        </small>
                                    </c:if>
                                </td>

                                <td>
                                    <strong><c:out value="${t.categoryName != null ? t.categoryName : t.category}"/></strong>
                                    <small style="display:block;color:var(--muted)"><c:out value="${t.deskName != null ? t.deskName : (t.desk != null ? t.desk : 'Thời sự')}"/></small>
                                </td>

                                <td>
                                    <c:out value="${t.genre != null ? t.genre : 'Tin nhanh'}"/>
                                </td>

                                <td>
                                    <span class="priority-tag ${t.priority}"><c:out value="${t.priority}"/></span>
                                </td>

                                <td>
                                    <c:out value="${t.deadline}"/>
                                </td>

                                <td>
                                    <span class="badge b-${t.status.toLowerCase()}"><c:out value="${t.status}"/></span>
                                </td>

                                <td style="text-align:right">
                                    <div style="display:inline-flex;gap:6px;justify-content:flex-end">
                                        <a class="btn sm" href="${base}/reporter/detail?id=${t.topicId}">Mở</a>

                                        <c:if test="${t.status eq 'DRAFT' or t.status eq 'REVISION_REQUESTED' or t.status eq 'REVISE'}">
                                            <a class="btn sm primary" href="${base}/reporter/compose?id=${t.topicId}">Sửa</a>
                                        </c:if>

                                        <c:if test="${t.status eq 'DRAFT'}">
                                            <a class="btn sm success" href="${base}/reporter/quick-submit?id=${t.topicId}" onclick="return confirm('Nộp đề xuất đề tài này lên Ban Biên tập để thẩm định?')">Nộp</a>
                                            <a class="btn sm danger" href="${base}/reporter/delete?id=${t.topicId}" onclick="return confirm('Bạn có chắc chắn muốn xóa bản nháp đề tài này? Thao tác không thể hoàn tác.')">Xóa</a>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>

                        <c:if test="${empty topics}">
                            <tr>
                                <td colspan="8" class="empty">
                                    <div style="padding:20px">
                                        <p style="margin:0 0 10px;font-size:15px">Không tìm thấy đề tài nào phù hợp với điều kiện tìm kiếm.</p>
                                        <a class="btn primary" href="${base}/reporter/compose">Soạn đề tài mới ngay</a>
                                    </div>
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </main>
    </div>
</div>
</body>
</html>
