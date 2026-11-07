<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đề Xuất Xin Hỗ Trợ Đề Tài — NTMCS</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${base}/css/style.css">
    <link rel="stylesheet" href="${base}/css/reporter.css">
</head>
<body class="bg-light">
<div class="app">
    <jsp:include page="/WEB-INF/views/sidebar.jsp">
        <jsp:param name="active" value="reporter-requests"/>
    </jsp:include>

    <div class="main p-4">
        <!-- Topbar/Breadcrumb -->
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb mb-1">
                        <li class="breadcrumb-item"><a href="${base}/home">Trang chủ</a></li>
                        <li class="breadcrumb-item"><a href="${base}/reporter/dashboard">Phân hệ phóng viên</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Đề xuất xin hỗ trợ</li>
                    </ol>
                </nav>
                <h3 class="fw-bold mb-0 text-primary">
                    <i class="bi bi-hand-thumbs-up-fill me-2"></i>Đề Xuất & Xin Hỗ Trợ Đề Tài
                </h3>
            </div>
            <div>
                <a href="${base}/reporter/topics" class="btn btn-outline-secondary">
                    <i class="bi bi-journal-text me-1"></i> Quản lý đề tài của tôi
                </a>
            </div>
        </div>

        <!-- Banner thông tin nghiệp vụ -->
        <div class="alert alert-info border-0 shadow-sm d-flex align-items-center mb-4 p-3" role="alert">
            <i class="bi bi-shield-check fs-2 me-3 text-info"></i>
            <div>
                <strong class="d-block mb-1">Quy định thẩm quyền đề xuất:</strong>
                <span>Chỉ những đề tài đã chính thức được <strong>Phê duyệt (APPROVED)</strong> bởi Ban Biên tập mới đủ điều kiện xin cấp phát tài sản thiết bị, gia hạn hạn nộp bài hoặc xin điều chỉnh nhân sự nhóm.</span>
            </div>
        </div>

        <!-- Danh sách Đề tài đã APPROVED -->
        <div class="card shadow-sm border-0">
            <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
                <h5 class="fw-bold mb-0 text-dark">
                    <i class="bi bi-list-stars me-2 text-primary"></i>Danh sách đề tài đủ điều kiện đề xuất (${approvedTopics != null ? approvedTopics.size() : 0})
                </h5>
                <span class="badge bg-success-subtle text-success border border-success-subtle px-3 py-2">
                    <i class="bi bi-check2-circle me-1"></i> Trạng thái: APPROVED
                </span>
            </div>

            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th style="width: 140px;">Mã đề tài</th>
                                <th>Tiêu đề đề tài & Góc tiếp cận</th>
                                <th style="width: 160px;">Chuyên mục</th>
                                <th style="width: 140px;">Hạn nộp bài</th>
                                <th style="width: 130px;">Trạng thái</th>
                                <th style="width: 180px;" class="text-center">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="t" items="${approvedTopics}">
                                <tr>
                                    <td>
                                        <span class="badge bg-secondary text-uppercase fw-semibold">
                                            <c:out value="${t.topicCode != null ? t.topicCode : ('#' += t.topicId)}"/>
                                        </span>
                                    </td>
                                    <td>
                                        <a href="${base}/reporter/requests?topicId=${t.topicId}" class="fw-bold text-dark text-decoration-none d-block mb-1">
                                            <c:out value="${t.title}"/>
                                        </a>
                                        <small class="text-muted d-block text-truncate" style="max-width: 450px;">
                                            <c:out value="${t.angle != null ? t.angle : t.summary}"/>
                                        </small>
                                    </td>
                                    <td>
                                        <span class="badge bg-light text-dark border">
                                            <c:out value="${t.categoryName != null ? t.categoryName : 'Chung'}"/>
                                        </span>
                                    </td>
                                    <td>
                                        <strong class="text-danger small">
                                            <i class="bi bi-calendar-event me-1"></i><c:out value="${t.deadline}"/>
                                        </strong>
                                    </td>
                                    <td>
                                        <span class="badge bg-success">
                                            <i class="bi bi-check-circle me-1"></i>${t.status}
                                        </span>
                                    </td>
                                    <td class="text-center">
                                        <a href="${base}/reporter/requests?topicId=${t.topicId}" class="btn btn-sm btn-primary px-3 shadow-sm">
                                            <i class="bi bi-send-plus-fill me-1"></i> Tạo đề xuất
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>

                            <c:if test="${empty approvedTopics}">
                                <tr>
                                    <td colspan="6" class="text-center py-5">
                                        <div class="text-muted">
                                            <i class="bi bi-inbox fs-1 d-block mb-2 text-secondary"></i>
                                            <p class="mb-1 fw-semibold">Hiện chưa có đề tài nào của bạn ở trạng thái ĐÃ DUYỆT (APPROVED).</p>
                                            <small>Các đề tài đang là bản nháp (DRAFT) hoặc đang chờ thẩm định (SUBMITTED) cần được duyệt trước khi xin tài sản hay nhân sự.</small>
                                            <div class="mt-3">
                                                <a href="${base}/reporter/topics" class="btn btn-outline-primary btn-sm">
                                                    <i class="bi bi-arrow-left me-1"></i> Xem danh sách đề tài của tôi
                                                </a>
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Bootstrap 5 Bundle JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
