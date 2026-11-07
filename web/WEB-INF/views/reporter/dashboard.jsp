<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Reporter Dashboard — NTMCS</title>
        <link rel="stylesheet" href="${base}/css/style.css">
        <link rel="stylesheet" href="${base}/css/reporter.css">
    </head>
    <body>
        <div class="app">
            <jsp:include page="/WEB-INF/views/sidebar.jsp">
                <jsp:param name="active" value="reporter-dashboard"/>
            </jsp:include>
            <div class="main">
                <jsp:include page="/WEB-INF/views/topbar.jsp">
                    <jsp:param name="title" value="Bàn làm việc Phóng viên"/>
                </jsp:include>
                <main>
                    <div class="page-head">
                        <div>
                            <span class="eyebrow">PHÂN HỆ PHÓNG VIÊN / WF-01</span>
                            <h1>Tổng Quan Bàn Làm Việc Phóng Viên</h1>
                            <p>Theo dõi tiến độ, trạng thái các đề xuất đề tài cá nhân và phản hồi thẩm định từ Ban Biên tập.</p>
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

                    <section class="stats" style="grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));">
                        <div class="stat">
                            <div class="stat-icon" style="background:#eef2ff;color:var(--primary)">◆</div>
                            <div>
                                <span>Chờ thẩm định</span>
                                <strong>${submittedCount}</strong>
                                <small>Gửi Ban biên tập duyệt</small>
                            </div>
                        </div>

                        <div class="stat">
                            <div class="stat-icon" style="background:#e9f8f1;color:var(--success)">◎</div>
                            <div>
                                <span>Đang tác nghiệp</span>
                                <strong>${inProgressCount}</strong>
                                <small>Đề tài đã triển khai</small>
                            </div>
                        </div>

                        <div class="stat">
                            <div class="stat-icon" style="background:#fff4df;color:var(--warning)">✍️</div>
                            <div>
                                <span>Bản nháp / Cần sửa</span>
                                <strong>${draftCount + revisionCount}</strong>
                                <small>${draftCount} nháp &middot; ${revisionCount} yêu cầu sửa</small>
                            </div>
                        </div>

                        <div class="stat">
                            <div class="stat-icon" style="background:#e8f4ff;color:#2563eb">✓</div>
                            <div>
                                <span>Đã duyệt &amp; Sẵn sàng</span>
                                <strong>${approvedCount + readyCmsCount}</strong>
                                <small>Approved &middot; CMS Ready</small>
                            </div>
                        </div>
                    </section>

                    <div class="grid2">
                        <section class="card">
                            <div class="card-title">
                                <h2>Vòng đời đề tài (Topic Lifecycle)</h2>
                                <a class="link" href="${base}/reporter/topics">Tất cả đề tài &rarr;</a>
                            </div>
                            <div class="flow">
                                <div>
                                    <span>1. BẢN NHÁP</span>
                                    <b>${draftCount}</b>
                                </div>
                                <div>
                                    <span>2. ĐÃ GỬI DUYỆT</span>
                                    <b>${submittedCount}</b>
                                </div>
                                <div>
                                    <span>3. YÊU CẦU SỬA</span>
                                    <b>${revisionCount}</b>
                                </div>
                                <div>
                                    <span>4. ĐÃ PHÊ DUYỆT</span>
                                    <b>${approvedCount}</b>
                                </div>
                                <div>
                                    <span>5. TÁC NGHIỆP</span>
                                    <b>${inProgressCount}</b>
                                </div>
                                <div>
                                    <span>6. ĐÃ BÀN GIAO</span>
                                    <b>0</b>
                                </div>
                                <div>
                                    <span>7. SẴN SÀNG CMS</span>
                                    <b>${readyCmsCount}</b>
                                </div>
                                <div>
                                    <span>TỔNG SỐ ĐỀ TÀI</span>
                                    <b>${totalCount}</b>
                                </div>
                            </div>
                        </section>

                        <section class="card">
                            <div class="card-title">
                                <h2>Đề tài gần đây của bạn</h2>
                                <a class="link" href="${base}/reporter/topics">Xem danh sách &rarr;</a>
                            </div>
                            <div class="table-scroll" style="box-shadow:none">
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Mã</th>
                                            <th>Tiêu đề đề tài</th>
                                            <th>Ưu tiên</th>
                                            <th>Trạng thái</th>
                                            <th></th>
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
                                                            <span style="color:var(--muted)">#${t.topicId}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <a class="row-link" href="${base}/reporter/detail?id=${t.topicId}">
                                                        <strong><c:out value="${t.title}"/></strong>
                                                    </a>
                                                    <small style="display:block;color:var(--muted)"><c:out value="${t.categoryName != null ? t.categoryName : t.category}"/> &middot; Hạn: <c:out value="${t.deadline}"/></small>
                                                </td>
                                                <td>
                                                    <span class="priority-tag ${t.priority}"><c:out value="${t.priority}"/></span>
                                                </td>
                                                <td>
                                                    <span class="badge b-${t.status.toLowerCase()}"><c:out value="${t.status}"/></span>
                                                </td>
                                                <td>
                                                    <a class="btn sm" href="${base}/reporter/detail?id=${t.topicId}">Xem</a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty topics}">
                                            <tr>
                                                <td colspan="5" class="empty">Chưa có đề tài nào trong danh sách.</td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </section>
                    </div>

                    <div class="grid3">
                        <section class="card">
                            <h2>Quyền tác nghiệp phóng viên (WF-01)</h2>
                            <div class="permission-box">
                                <span>✓ Soạn đề xuất đề tài mới</span>
                                <span>✓ Lưu bản nháp (DRAFT)</span>
                                <span>✓ Nộp đề xuất thẩm định (SUBMITTED)</span>
                                <span>✓ Tra cứu trùng lặp 30 ngày</span>
                                <span>✓ Tiếp thu &amp; Sửa đề tài theo ý kiến BTV</span>
                                <span>✓ Giám sát quy trình sản xuất tin bài</span>
                            </div>
                        </section>

                        <section class="card">
                            <h2>Quy trình xử lý đề xuất</h2>
                            <p style="color:var(--muted);font-size:13px;line-height:1.6;margin:0 0 12px">
                                Mỗi đề tài được khởi tạo sẽ trải qua các bước trong chu trình tòa soạn hội tụ:
                            </p>
                            <div style="font-size:12.5px;display:grid;gap:6px">
                                <div><strong>Bước 1:</strong> Phóng viên soạn đề tài, lập dự toán &amp; nhân sự</div>
                                <div><strong>Bước 2:</strong> Nộp duyệt sang Ban Biên tập (Desk Editor)</div>
                                <div><strong>Bước 3:</strong> Thẩm định Rubric (Đạt / Yêu cầu sửa / Từ chối)</div>
                                <div><strong>Bước 4:</strong> Triển khai tác nghiệp và xuất bản CMS</div>
                            </div>
                        </section>

                        <section class="card">
                            <h2>Thông tin tài khoản</h2>
                            <p style="color:var(--muted);font-size:13px;margin:0 0 14px">
                                Đang tác nghiệp với tài khoản: <strong><c:out value="${not empty currentUserName ? currentUserName : sessionScope.loggedUser.fullName}"/></strong> (<c:out value="${not empty currentUserEmail ? currentUserEmail : sessionScope.loggedUser.email}"/>).
                            </p>
                            <div style="display:grid;gap:8px">
                                <a class="btn primary" href="${base}/reporter/compose">✍️ Soạn đề xuất đề tài</a>
                                <a class="btn" href="${base}/reporter/duplicate-search">🔍 Tra cứu đề tài trùng lặp</a>
                                <a class="btn" href="${base}/reporter/topics">📑 Quản lý danh sách đề tài</a>
                            </div>
                        </section>
                    </div>
                </main>
            </div>
        </div>
    </body>
</html>
