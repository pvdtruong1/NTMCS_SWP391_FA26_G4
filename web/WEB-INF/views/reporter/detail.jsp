<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${topic != null ? topic.title : 'Chi tiết đề tài'} — NTMCS</title>
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
            <jsp:param name="title" value="Chi Tiết Đề Xuất Đề Tài"/>
        </jsp:include>
        <main>
            <c:choose>
                <c:when test="${empty topic}">
                    <div class="page-head">
                        <div>
                            <span class="eyebrow">PHÂN HỆ PHÓNG VIÊN / WF-01</span>
                            <h1>Không tìm thấy đề tài</h1>
                            <p>Đề tài bạn yêu cầu không tồn tại hoặc bạn không có quyền truy cập.</p>
                        </div>
                        <div class="page-actions">
                            <a class="btn" href="${base}/reporter/topics">&larr; Quay lại danh sách</a>
                        </div>
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="page-head">
                        <div>
                            <span class="eyebrow">PHÂN HỆ PHÓNG VIÊN / WF-01</span>
                            <h1><c:out value="${topic.title}"/></h1>
                            <p>
                                Mã định danh: <strong>${topic.topicCode != null ? topic.topicCode : ('#' += topic.topicId)}</strong>
                                &middot; Khởi tạo ngày <c:out value="${topic.createdDate}"/>
                            </p>
                        </div>
                        <div class="page-actions">
                            <a class="btn" href="${base}/reporter/topics">&larr; Danh sách</a>
                            <a class="btn" href="${base}/reporter/requests?topicId=${topic.topicId}" style="background-color: #0d6efd; color: white;">📋 Đề xuất & Xin hỗ trợ</a>
                            <c:if test="${topic.status eq 'DRAFT' or topic.status eq 'REVISION_REQUESTED' or topic.status eq 'REVISE'}">
                                <a class="btn primary" href="${base}/reporter/compose?id=${topic.topicId}">
                                    ✍️ Chỉnh sửa đề tài
                                </a>
                            </c:if>
                            <c:if test="${topic.status eq 'DRAFT'}">
                                <a class="btn success" href="${base}/reporter/quick-submit?id=${topic.topicId}" onclick="return confirm('Nộp đề xuất này lên Ban Biên tập để thẩm định?')">
                                    🚀 Nộp đề xuất ngay
                                </a>
                            </c:if>
                        </div>
                    </div>

                    <div class="detail-grid">
                        <div>
                            <section class="card hero-card">
                                <div class="detail-title">
                                    <div>
                                        <div class="eyebrow">${topic.topicCode != null ? topic.topicCode : 'PROPOSAL'}</div>
                                        <h2><c:out value="${topic.title}"/></h2>
                                        <p><c:out value="${topic.angle}"/></p>
                                    </div>
                                    <span class="badge b-${topic.status.toLowerCase()}"><c:out value="${topic.status}"/></span>
                                </div>

                                <div class="meta-grid">
                                    <div>
                                        <span>Tác giả đề xuất</span>
                                        <b><c:out value="${topic.reporterName != null ? topic.reporterName : sessionScope.loggedUser.fullName}"/></b>
                                    </div>
                                    <div>
                                        <span>Chuyên mục</span>
                                        <b><c:out value="${topic.categoryName != null ? topic.categoryName : topic.category}"/></b>
                                    </div>
                                    <div>
                                        <span>Thể loại &amp; Ưu tiên</span>
                                        <b><c:out value="${topic.genre}"/> &middot; <span class="priority-tag ${topic.priority}"><c:out value="${topic.priority}"/></span></b>
                                    </div>
                                    <div>
                                        <span>Deadline dự kiến</span>
                                        <b><c:out value="${topic.deadline}"/></b>
                                    </div>
                                </div>

                                <c:if test="${not empty topic.rejectionReason}">
                                    <div class="alert danger" style="background:#ffedf0;color:#bd3344;border:1px solid #f0c8ce;padding:14px 16px;border-radius:10px;margin-top:16px">
                                        <strong>Phản hồi từ Ban Biên tập:</strong>
                                        <p style="margin:4px 0 0;font-weight:600"><c:out value="${topic.rejectionReason}"/></p>
                                    </div>
                                </c:if>

                                <div class="content-block">
                                    <h3>Tóm tắt ý tưởng &amp; Hiện tượng sự kiện</h3>
                                    <div class="content-box">
                                        <c:out value="${topic.summary != null && !topic.summary.isEmpty() ? topic.summary : 'Chưa có thông tin tóm tắt.'}"/>
                                    </div>
                                </div>

                                <div class="content-block">
                                    <h3>Góc tiếp cận (Angle)</h3>
                                    <div class="content-box">
                                        <c:out value="${topic.angle != null && !topic.angle.isEmpty() ? topic.angle : 'Chưa có góc tiếp cận riêng.'}"/>
                                    </div>
                                </div>

                                <div class="action-row">
                                    <a class="btn" href="${base}/reporter/topics">&larr; Về danh sách đề tài</a>
                                    <c:if test="${topic.status eq 'DRAFT' or topic.status eq 'REVISION_REQUESTED' or topic.status eq 'REVISE'}">
                                        <a class="btn primary" href="${base}/reporter/compose?id=${topic.topicId}">
                                            Chỉnh sửa nội dung đề xuất
                                        </a>
                                    </c:if>
                                    <c:if test="${topic.status eq 'DRAFT'}">
                                        <a class="btn success" href="${base}/reporter/quick-submit?id=${topic.topicId}" onclick="return confirm('Nộp đề xuất đề tài này lên Ban Biên tập?')">
                                            Nộp thẩm định
                                        </a>
                                        <a class="btn danger" href="${base}/reporter/delete?id=${topic.topicId}" onclick="return confirm('Bạn có chắc chắn muốn xóa bản nháp đề tài này? Thao tác không thể hoàn tác.')">
                                            Xóa bản nháp
                                        </a>
                                    </c:if>
                                </div>
                            </section>

                            <div class="grid2" style="margin-top:20px">
                                <section class="card">
                                    <div class="card-title">
                                        <h2>Ekip tác nghiệp (Topic_Member)</h2>
                                        <span class="badge b-approved">${not empty members ? members.size() : 0} nhân sự</span>
                                    </div>
                                    <c:choose>
                                        <c:when test="${not empty members}">
                                            <div style="display:flex;flex-direction:column;gap:8px;margin-top:10px">
                                                <c:forEach var="m" items="${members}">
                                                    <div style="display:flex;justify-content:space-between;align-items:center;padding:8px 12px;background:var(--surface2);border-radius:8px">
                                                        <div>
                                                            <strong><c:out value="${m.userName}"/></strong>
                                                            <small style="display:block;color:var(--muted)"><c:out value="${m.userRole}"/></small>
                                                        </div>
                                                        <span class="badge"><c:out value="${m.roleInTeamDisplay}"/></span>
                                                    </div>
                                                </c:forEach>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <p style="color:var(--muted);font-size:13px;margin:0">
                                                Chưa có nhân sự nào được phân công trong tổ tác nghiệp này.
                                            </p>
                                        </c:otherwise>
                                    </c:choose>
                                </section>

                                <section class="card">
                                    <div class="card-title">
                                        <h2>Dự toán kinh phí (Topic_Estimate)</h2>
                                        <span class="badge b-approved">${not empty estimates ? estimates.size() : 0} khoản chi</span>
                                    </div>
                                    <c:choose>
                                        <c:when test="${not empty estimates}">
                                            <div style="display:flex;flex-direction:column;gap:8px;margin-top:10px">
                                                <c:forEach var="e" items="${estimates}">
                                                    <div style="display:flex;justify-content:space-between;align-items:center;padding:8px 12px;background:var(--surface2);border-radius:8px">
                                                        <span><c:out value="${e.costItem}"/></span>
                                                        <strong style="color:var(--primary)"><c:out value="${e.formattedAmount}"/></strong>
                                                    </div>
                                                </c:forEach>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <p style="color:var(--muted);font-size:13px;margin:0">
                                                Chưa có khoản dự toán kinh phí nào được lập cho đề tài này.
                                            </p>
                                        </c:otherwise>
                                    </c:choose>
                                </section>
                            </div>
                        </div>

                        <aside>
                            <section class="card">
                                <h2>Tiến độ thẩm định &amp; Quy trình</h2>
                                <div class="timeline">
                                    <div class="step ${stepRank >= 1 ? 'done' : ''} ${stepRank == 1 ? 'current' : ''}">
                                        <span>1</span>
                                        <div>
                                            <b>Bản nháp (DRAFT)</b>
                                            <small>Khởi tạo ý tưởng ban đầu</small>
                                        </div>
                                    </div>

                                    <div class="step ${stepRank >= 2 ? 'done' : ''} ${stepRank == 2 ? 'current' : ''}">
                                        <span>2</span>
                                        <div>
                                            <b>Đã nộp duyệt (SUBMITTED)</b>
                                            <small>Thư ký / Trưởng ban thẩm định</small>
                                        </div>
                                    </div>

                                    <div class="step ${stepRank >= 3 ? 'done' : ''} ${stepRank == 3 ? 'current' : ''}">
                                        <span>3</span>
                                        <div>
                                            <b>Chờ TBT duyệt (EXECUTIVE)</b>
                                            <small>Vòng phê duyệt cấp tòa soạn</small>
                                        </div>
                                    </div>

                                    <div class="step ${stepRank >= 4 ? 'done' : ''} ${stepRank == 4 ? 'current' : ''}">
                                        <span>4</span>
                                        <div>
                                            <b>Đã duyệt (APPROVED)</b>
                                            <small>Được cấp mã đề tài chính thức</small>
                                        </div>
                                    </div>

                                    <div class="step ${stepRank >= 5 ? 'done' : ''} ${stepRank == 5 ? 'current' : ''}">
                                        <span>5</span>
                                        <div>
                                            <b>Tác nghiệp (IN_PROGRESS)</b>
                                            <small>Thực hiện bài viết &amp; thực địa</small>
                                        </div>
                                    </div>

                                    <div class="step ${stepRank >= 6 ? 'done' : ''} ${stepRank == 6 ? 'current' : ''}">
                                        <span>6</span>
                                        <div>
                                            <b>Đã bàn giao (DELIVERED)</b>
                                            <small>Nộp bản thảo bài viết</small>
                                        </div>
                                    </div>

                                    <div class="step ${stepRank >= 7 ? 'done' : ''} ${stepRank == 7 ? 'current' : ''}">
                                        <span>7</span>
                                        <div>
                                            <b>Sẵn sàng CMS (ACCEPTED)</b>
                                            <small>Bàn giao hệ thống xuất bản</small>
                                        </div>
                                    </div>
                                </div>
                            </section>

                            <section class="card" style="margin-top:20px">
                                <h2>Hồ sơ quản lý</h2>
                                <div style="font-size:13px;display:grid;gap:8px">
                                    <div><strong>Phòng ban:</strong> <c:out value="${topic.deskName != null ? topic.deskName : (topic.desk != null ? topic.desk : 'Thời sự')}"/></div>
                                    <div><strong>Email tác giả:</strong> <c:out value="${topic.reporterEmail != null ? topic.reporterEmail : sessionScope.loggedUser.email}"/></div>
                                    <div><strong>Ngày tạo:</strong> <c:out value="${topic.createdDate}"/></div>
                                    <div><strong>Trạng thái:</strong> <span class="badge b-${topic.status.toLowerCase()}"><c:out value="${topic.status}"/></span></div>
                                </div>
                            </section>
                        </aside>
                    </div>
                </c:otherwise>
            </c:choose>
        </main>
    </div>
</div>
</body>
</html>
