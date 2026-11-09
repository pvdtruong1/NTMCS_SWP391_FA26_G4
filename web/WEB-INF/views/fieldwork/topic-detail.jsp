<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hồ Sơ Đề Tài [${topic.topicCode != null ? topic.topicCode : ('#' += topic.topicId)}] — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
    <style>
        .topic-badge {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 700;
            text-transform: uppercase;
        }
        .b-approved { background: #e0f2fe; color: #0369a1; }
        .b-in_progress { background: #fef9c3; color: #a16207; }
        .b-delivered { background: #dcfce7; color: #15803d; }
        .b-aborted { background: #fee2e2; color: #b91c1c; }
        .b-rejected { background: #fee2e2; color: #b91c1c; }

        .dossier-grid {
            display: grid;
            grid-template-columns: 1fr 340px;
            gap: 20px;
            align-items: start;
        }
        @media (max-width: 1024px) {
            .dossier-grid { grid-template-columns: 1fr; }
        }

        .dossier-card {
            background: var(--surface);
            border: 1px solid var(--line);
            border-radius: 12px;
            padding: 20px;
            margin-bottom: 20px;
        }
        .dossier-card h2 {
            font-size: 15px;
            font-weight: 700;
            margin: 0 0 16px 0;
            display: flex;
            align-items: center;
            justify-content: space-between;
            color: var(--text);
        }

        .meta-list {
            display: flex;
            flex-direction: column;
            gap: 12px;
        }
        .meta-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 13px;
            padding-bottom: 8px;
            border-bottom: 1px dashed var(--line);
        }
        .meta-item:last-child {
            border-bottom: none;
            padding-bottom: 0;
        }
        .meta-label {
            color: var(--muted);
        }
        .meta-val {
            font-weight: 600;
            color: var(--text);
            text-align: right;
        }

        .task-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px 14px;
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            margin-bottom: 10px;
        }
        .task-row:last-child { margin-bottom: 0; }
        .badge-status {
            padding: 3px 8px;
            border-radius: 6px;
            font-size: 11px;
            font-weight: 700;
        }
        .status-TODO { background: #f1f5f9; color: #475569; }
        .status-IN_PROGRESS { background: #fef9c3; color: #a16207; }
        .status-DONE { background: #dcfce7; color: #15803d; }
    </style>
</head>
<body>
<div class="app">
    <jsp:include page="/WEB-INF/views/sidebar.jsp">
        <jsp:param name="active" value="fieldwork-kanban"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="/WEB-INF/views/topbar.jsp">
            <jsp:param name="title" value="Hồ Sơ Đề Tài Hiện Trường (${topic.topicCode != null ? topic.topicCode : ('#' += topic.topicId)})"/>
        </jsp:include>
        <main>
            <!-- Page Header -->
            <div class="page-head" style="margin-bottom: 24px;">
                <div>
                    <span class="eyebrow">GIÁM SÁT TÁC NGHIỆP HIỆN TRƯỜNG / WF-04</span>
                    <h1 style="display:flex; align-items:center; gap:12px; margin-top:4px;">
                        <span><c:out value="${topic.title}" /></span>
                        <span class="topic-badge b-${topic.status != null ? topic.status.toLowerCase() : 'approved'}">
                            <c:out value="${topic.status}" default="APPROVED" />
                        </span>
                    </h1>
                    <p style="color:var(--muted); margin-top:4px;">
                        Mã định danh: <strong style="color:var(--primary); font-family:monospace;">${topic.topicCode != null ? topic.topicCode : ('#' += topic.topicId)}</strong>
                        &middot; Khởi tạo ngày: <c:out value="${topic.createdDate}" default="—" />
                    </p>
                </div>
                <div class="page-actions" style="display:flex; gap:10px;">
                    <c:choose>
                        <c:when test="${sessionScope.loggedUser.roleId == 4 or sessionScope.user.roleId == 4 or (not empty sessionScope.permissions and sessionScope.permissions.contains('LOGISTICS_MANAGE'))}">
                            <button type="button" onclick="history.back()" class="btn" style="background:var(--surface); border:1px solid var(--line); color:var(--text); text-decoration:none; cursor:pointer;">
                                ← Quay lại
                            </button>
                            <a href="${base}/asset-requests?topicId=${topic.topicId}" class="btn primary" style="text-decoration:none;">
                                📋 Yêu cầu tài sản của đề tài
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="${base}/fieldwork/kanban" class="btn" style="background:var(--surface); border:1px solid var(--line); color:var(--text); text-decoration:none;">
                                ← Quay lại Kanban
                            </a>
                            <a href="${base}/fieldwork/nudge" class="btn" style="background:#fffbeb; color:#b45309; border:1px solid #fde68a; text-decoration:none;">
                                ⚡ Đôn đốc tiến độ
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- Incidents Alert (nếu có sự cố khẩn cấp) -->
            <c:if test="${not empty incidents}">
                <div style="background: #fff1f2; border: 1px solid #fecdd3; border-radius: 12px; padding: 16px 20px; margin-bottom: 24px;">
                    <h3 style="color: #be123c; margin: 0 0 10px 0; font-size: 15px; display: flex; align-items: center; gap: 8px;">
                        <span>🚨</span> CẢNH BÁO SỰ CỐ KHẨN CẤP HIỆN TRƯỜNG (${incidents.size()} sự cố đã ghi nhận)
                    </h3>
                    <c:forEach var="inc" items="${incidents}">
                        <div style="background: #fff; border: 1px solid #fda4af; border-radius: 8px; padding: 10px 14px; margin-bottom: 8px;">
                            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:4px; font-size:12px;">
                                <strong style="color:#9f1239;">Người báo: <c:out value="${inc[2]}" /></strong>
                                <span style="color:#881337; font-family:monospace;">${inc[4]}</span>
                            </div>
                            <div style="color: #4c0519; font-size: 13px; line-height: 1.4;">
                                <c:out value="${inc[3]}" />
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:if>

            <!-- Dossier Grid -->
            <div class="dossier-grid">
                <!-- Left Column -->
                <div>
                    <!-- Hero Content Card -->
                    <div class="dossier-card">
                        <h2>
                            <span>📖 Góc Tiếp Cận &amp; Tóm Tắt Ý Tưởng</span>
                        </h2>
                        <div style="margin-bottom: 16px;">
                            <label style="font-size: 11px; font-weight: 700; color: var(--muted); text-transform: uppercase;">Góc tiếp cận (Angle):</label>
                            <div style="font-size: 14px; font-weight: 600; color: var(--text); background: #f8fafc; padding: 10px 14px; border-radius: 8px; margin-top: 4px; border: 1px solid #e2e8f0;">
                                <c:out value="${not empty topic.angle ? topic.angle : 'Chưa có thông tin góc tiếp cận riêng.'}" />
                            </div>
                        </div>
                        <div>
                            <label style="font-size: 11px; font-weight: 700; color: var(--muted); text-transform: uppercase;">Tóm tắt ý tưởng &amp; Hiện tượng sự kiện:</label>
                            <div style="font-size: 13px; line-height: 1.6; color: #334155; background: #f8fafc; padding: 12px 14px; border-radius: 8px; margin-top: 4px; border: 1px solid #e2e8f0; white-space: pre-line;">
                                <c:out value="${not empty topic.summary ? topic.summary : 'Chưa có nội dung tóm tắt.'}" />
                            </div>
                        </div>
                    </div>

                    <!-- Ban Biên Tập Phản Hồi / Đánh Giá -->
                    <c:if test="${not empty topic.rejectionReason}">
                        <div class="dossier-card" style="border-left: 4px solid var(--primary);">
                            <h2><span>📝 Đánh Giá &amp; Chỉ Đạo Từ Ban Biên Tập</span></h2>
                            <div style="font-size: 13px; color: #1e293b; background: #eff6ff; padding: 12px 14px; border-radius: 8px; border: 1px solid #bfdbfe;">
                                <c:out value="${topic.rejectionReason}" />
                            </div>
                        </div>
                    </c:if>

                    <!-- Danh sách Đầu Việc Tác Nghiệp Hiện Trường (Fieldwork_Task) -->
                    <div class="dossier-card">
                        <h2>
                            <span>📋 Đầu Việc Hiện Trường (Tasks)</span>
                            <span class="badge" style="background:#eef2ff; color:var(--primary); font-size:12px;">${tasks != null ? tasks.size() : 0} đầu việc</span>
                        </h2>
                        <c:choose>
                            <c:when test="${not empty tasks}">
                                <c:forEach var="task" items="${tasks}">
                                    <div class="task-row">
                                        <div style="flex:1; padding-right:16px;">
                                            <div style="display:flex; align-items:center; gap:8px; margin-bottom:4px;">
                                                <small style="color:var(--muted); font-family:monospace;">#${task.taskId}</small>
                                                <strong style="font-size:13px; color:#1e293b;"><c:out value="${task.description}" /></strong>
                                            </div>
                                            <small style="color:#64748b;">
                                                👤 Phụ trách: <b><c:out value="${task.assigneeName}" default="Chưa phân công" /></b>
                                                (${task.assigneeEmail})
                                            </small>
                                        </div>
                                        <div>
                                            <span class="badge-status status-${task.taskStatus}">
                                                <c:choose>
                                                    <c:when test="${task.taskStatus == 'TODO'}">⏳ Chờ tiếp nhận</c:when>
                                                    <c:when test="${task.taskStatus == 'IN_PROGRESS'}">⚡ Đang tác nghiệp</c:when>
                                                    <c:when test="${task.taskStatus == 'DONE'}">✓ Đã nộp SP</c:when>
                                                    <c:otherwise>${task.taskStatus}</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <p style="color:var(--muted); font-size:13px; text-align:center; padding:16px 0; margin:0;">
                                    Chưa có đầu việc hiện trường nào được tạo cho đề tài này.
                                </p>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Sản Phẩm Tác Nghiệp Đã Nộp (Deliverables) -->
                    <div class="dossier-card">
                        <h2>
                            <span>📦 Sản Phẩm &amp; Tài Liệu Tác Nghiệp Đã Nộp</span>
                            <span class="badge" style="background:#dcfce7; color:#15803d; font-size:12px;">${deliverables != null ? deliverables.size() : 0} sản phẩm</span>
                        </h2>
                        <c:choose>
                            <c:when test="${not empty deliverables}">
                                <div style="display:flex; flex-direction:column; gap:10px;">
                                    <c:forEach var="del" items="${deliverables}">
                                        <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:12px 14px;">
                                            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
                                                <strong style="color:var(--primary); font-size:13px;">
                                                    📁 <c:out value="${not empty del.originalFileName ? del.originalFileName : (not empty del.externalUrl ? del.externalUrl : 'Tài liệu nộp')}" />
                                                </strong>
                                                <small style="color:var(--muted); font-family:monospace;">${del.submittedAt}</small>
                                            </div>
                                            <c:if test="${not empty del.deliverableNote}">
                                                <div style="font-size:12px; color:#475569; margin-bottom:6px;">
                                                    Ghi chú: <c:out value="${del.deliverableNote}" />
                                                </div>
                                            </c:if>
                                            <div style="font-size:11px; color:#64748b; display:flex; justify-content:space-between;">
                                                <span>Người nộp: <b><c:out value="${del.submitterName}" /></b></span>
                                                <c:if test="${not empty del.storedFilePath}">
                                                    <a href="${base}${del.storedFilePath}" target="_blank" style="color:var(--primary); text-decoration:none; font-weight:600;">
                                                        ⬇️ Tải tài liệu
                                                    </a>
                                                </c:if>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <p style="color:var(--muted); font-size:13px; text-align:center; padding:16px 0; margin:0;">
                                    Chưa có sản phẩm hoặc bằng chứng tác nghiệp nào được nộp.
                                </p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- Right Column: Meta & Team -->
                <div>
                    <!-- Meta Summary Card -->
                    <div class="dossier-card">
                        <h2><span>📋 Thông Tin Tổng Quan</span></h2>
                        <div class="meta-list">
                            <div class="meta-item">
                                <span class="meta-label">Tác giả đề xuất:</span>
                                <span class="meta-val"><c:out value="${topic.reporterName}" default="—" /></span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Email tác giả:</span>
                                <span class="meta-val" style="font-family:monospace; font-size:12px;"><c:out value="${topic.reporterEmail}" default="—" /></span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Ban chuyên môn:</span>
                                <span class="meta-val"><c:out value="${topic.deskName}" default="Ban Thời sự - Chính trị" /></span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Chuyên mục:</span>
                                <span class="meta-val"><c:out value="${topic.categoryName != null ? topic.categoryName : topic.category}" default="—" /></span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Thể loại báo chí:</span>
                                <span class="meta-val"><c:out value="${topic.genre}" default="Tin bài" /></span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Mức độ ưu tiên:</span>
                                <span class="meta-val" style="color: #b45309; font-weight:700;"><c:out value="${topic.priority}" default="NORMAL" /></span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Hạn chót tác nghiệp:</span>
                                <span class="meta-val" style="color:#b91c1c; font-weight:700;"><c:out value="${topic.deadline}" default="—" /></span>
                            </div>
                        </div>
                    </div>

                    <!-- Topic Members / Ekip Card -->
                    <div class="dossier-card">
                        <h2>
                            <span>👥 Kíp Tác Nghiệp</span>
                            <span class="badge" style="background:#e0f2fe; color:#0369a1; font-size:12px;">${members != null ? members.size() : 0} thành viên</span>
                        </h2>
                        <c:choose>
                            <c:when test="${not empty members}">
                                <div style="display:flex; flex-direction:column; gap:8px;">
                                    <c:forEach var="m" items="${members}">
                                        <div style="display:flex; justify-content:space-between; align-items:center; padding:8px 12px; background:#f8fafc; border-radius:8px; border:1px solid #e2e8f0;">
                                            <div>
                                                <strong style="font-size:13px; color:#1e293b;"><c:out value="${m.userName}" /></strong>
                                                <small style="display:block; color:#64748b; font-size:11px;"><c:out value="${m.userRole}" /></small>
                                            </div>
                                            <span style="font-size:11px; font-weight:600; background:#f1f5f9; padding:2px 8px; border-radius:4px; color:#475569;">
                                                <c:out value="${m.roleInTeamDisplay}" />
                                            </span>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <p style="color:var(--muted); font-size:13px; text-align:center; padding:12px 0; margin:0;">
                                    Chưa chỉ định kíp tác nghiệp chính thức.
                                </p>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Estimates / Dự toán kinh phí -->
                    <c:if test="${not empty estimates}">
                        <div class="dossier-card">
                            <h2>
                                <span>💰 Dự Toán Tác Nghiệp</span>
                                <span class="badge" style="background:#fef3c7; color:#b45309; font-size:12px;">${estimates.size()} mục</span>
                            </h2>
                            <div style="display:flex; flex-direction:column; gap:8px;">
                                <c:forEach var="est" items="${estimates}">
                                    <div style="display:flex; justify-content:space-between; align-items:center; font-size:12px; padding:6px 0; border-bottom:1px dashed #e2e8f0;">
                                        <span style="color:#475569;"><c:out value="${est.costItem}" /></span>
                                        <strong style="color:#15803d;"><fmt:formatNumber value="${est.amount}" type="number" /> đ</strong>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>
        </main>
    </div>
</div>
</body>
</html>
