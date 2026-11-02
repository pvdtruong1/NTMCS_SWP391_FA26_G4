<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bảng Kanban Tác Nghiệp Hiện Trường — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
    <style>
        .kanban-board {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 20px;
            margin-top: 20px;
            align-items: start;
        }
        @media (max-width: 992px) {
            .kanban-board {
                grid-template-columns: 1fr;
            }
        }
        .kanban-col {
            background: #f8fafc;
            border: 1px solid var(--line);
            border-radius: 12px;
            padding: 16px;
            display: flex;
            flex-direction: column;
            min-height: 500px;
        }
        .kanban-col-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 14px;
            padding-bottom: 10px;
            border-bottom: 2px solid var(--line);
        }
        .kanban-col-title {
            font-size: 13px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .kanban-col-count {
            background: var(--surface);
            border: 1px solid var(--line);
            border-radius: 12px;
            padding: 2px 8px;
            font-size: 11px;
            font-weight: 600;
        }
        .kanban-card {
            background: var(--surface);
            border: 1px solid var(--line);
            border-radius: 10px;
            padding: 14px;
            margin-bottom: 12px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.04);
            transition: box-shadow 0.15s ease, transform 0.15s ease;
        }
        .kanban-card:hover {
            box-shadow: 0 4px 10px rgba(0,0,0,0.08);
            transform: translateY(-1px);
        }
        .card-topic-tag {
            font-family: monospace;
            font-weight: 700;
            font-size: 11px;
            color: var(--primary);
            background: #eff6ff;
            padding: 2px 6px;
            border-radius: 4px;
        }
        .modal-overlay {
            display: none;
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(15, 23, 42, 0.6);
            backdrop-filter: blur(2px);
            z-index: 9999;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .modal-box {
            background: var(--surface);
            border-radius: 14px;
            max-width: 580px;
            width: 100%;
            box-shadow: 0 20px 40px rgba(0,0,0,0.2);
            overflow: hidden;
            display: flex;
            flex-direction: column;
            max-height: 90vh;
        }
        .modal-head {
            padding: 18px 24px;
            border-bottom: 1px solid var(--line);
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .modal-body {
            padding: 24px;
            overflow-y: auto;
        }
        .modal-footer {
            padding: 14px 24px;
            border-top: 1px solid var(--line);
            background: #f8fafc;
            display: flex;
            justify-content: flex-end;
            gap: 10px;
        }
    </style>
</head>
<body>
<div class="app">
    <jsp:include page="../sidebar.jsp">
        <jsp:param name="active" value="fieldwork-kanban"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="../topbar.jsp">
            <jsp:param name="title" value="Bảng Điều Phối Tác Nghiệp Hiện Trường (Kanban)"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">HIỆN TRƯỜNG &amp; ĐIỀU PHỐI / WF-04</span>
                    <h1>Bảng Điều Phối Tác Nghiệp Hiện Trường (SCR-4.1)</h1>
                    <p>Giám sát Kanban 3 pha tác nghiệp, nộp bàn giao sản phẩm phóng viên (SCR-4.3) và tiếp nhận báo động sự cố (SCR-4.4).</p>
                </div>
                <div class="page-actions" style="display: flex; gap: 8px;">
                    <button type="button" class="btn primary" onclick="openCreateTaskModal()">
                        + Giao Nhiệm Vụ Mới
                    </button>
                    <button type="button" class="btn" style="background: #fff1f2; color: #e11d48; border: 1px solid #fecdd3;" onclick="openEmergencyModal()">
                        🚨 Báo Cáo Sự Cố
                    </button>
                </div>
            </div>

            <c:if test="${not empty errorMessage}">
                <div style="background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce; border-radius: 10px; padding: 12px 16px; margin-bottom: 20px; font-weight: 600;">
                    ✕ <c:out value="${errorMessage}" />
                </div>
            </c:if>

            <c:set var="cntTodo" value="0"/>
            <c:set var="cntProgress" value="0"/>
            <c:set var="cntDone" value="0"/>
            <c:forEach var="t" items="${tasks}">
                <c:choose>
                    <c:when test="${t.taskStatus == 'TODO'}"><c:set var="cntTodo" value="${cntTodo + 1}"/></c:when>
                    <c:when test="${t.taskStatus == 'IN_PROGRESS'}"><c:set var="cntProgress" value="${cntProgress + 1}"/></c:when>
                    <c:when test="${t.taskStatus == 'DONE'}"><c:set var="cntDone" value="${cntDone + 1}"/></c:when>
                </c:choose>
            </c:forEach>

            <!-- Metrics Bar -->
            <div class="stats" style="grid-template-columns: repeat(4, 1fr); margin-bottom: 24px;">
                <div class="stat">
                    <div class="stat-icon" style="background: #eef2ff; color: var(--primary);">📋</div>
                    <div>
                        <span>TỔNG ĐẦU VIỆC</span>
                        <strong>${tasks.size()}</strong>
                        <small style="color: var(--muted);">Tác nghiệp toàn tòa soạn</small>
                    </div>
                </div>
                <div class="stat">
                    <div class="stat-icon" style="background: #f1f5f9; color: #64748b;">⏳</div>
                    <div>
                        <span>CHỜ THỰC HIỆN</span>
                        <strong>${cntTodo}</strong>
                        <small style="color: var(--muted);">Chờ phóng viên tiếp nhận</small>
                    </div>
                </div>
                <div class="stat">
                    <div class="stat-icon" style="background: #fef3c7; color: #d97706;">⚡</div>
                    <div>
                        <span>ĐANG TÁC NGHIỆP</span>
                        <strong>${cntProgress}</strong>
                        <small style="color: #d97706;">Đang triển khai hiện trường</small>
                    </div>
                </div>
                <div class="stat">
                    <div class="stat-icon" style="background: #dcfce7; color: #16a34a;">✅</div>
                    <div>
                        <span>ĐÃ NỘP SẢN PHẨM</span>
                        <strong>${cntDone}</strong>
                        <small style="color: #16a34a;">Hoàn thành bàn giao</small>
                    </div>
                </div>
            </div>

            <!-- Kanban Board -->
            <div class="kanban-board">
                <!-- Col 1: TODO -->
                <div class="kanban-col">
                    <div class="kanban-col-header">
                        <span class="kanban-col-title" style="color: #475569;">
                            <span style="display:inline-block; width:8px; height:8px; border-radius:50%; background:#94a3b8;"></span>
                            Chờ Tiếp Nhận (TODO)
                        </span>
                        <span class="kanban-col-count">${cntTodo}</span>
                    </div>

                    <div class="kanban-cards-wrapper">
                        <c:forEach var="task" items="${tasks}">
                            <c:if test="${task.taskStatus == 'TODO'}">
                                <div class="kanban-card">
                                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                                        <span class="card-topic-tag">${task.topicCode}</span>
                                        <small style="color:var(--muted); font-size:11px;">#${task.taskId}</small>
                                    </div>
                                    <div style="font-weight:600; font-size:13px; margin-bottom:6px; color:#1e293b;">
                                        <c:out value="${task.topicTitle}" />
                                    </div>
                                    <div style="font-size:12px; color:#475569; margin-bottom:12px; line-height:1.4;">
                                        <c:out value="${task.description}" />
                                    </div>
                                    <div style="display:flex; justify-content:space-between; align-items:center; padding-top:10px; border-top:1px solid #f1f5f9; font-size:11px;">
                                        <span style="color:#64748b; display:flex; align-items:center; gap:4px;">
                                            👤 <b><c:out value="${task.assigneeName}" /></b>
                                        </span>
                                        <button type="button" class="btn" style="padding:4px 8px; font-size:11px;" onclick="updateTaskStatus(${task.taskId}, 'IN_PROGRESS')">
                                            Bắt đầu &rarr;
                                        </button>
                                    </div>
                                </div>
                            </c:if>
                        </c:forEach>
                    </div>
                </div>

                <!-- Col 2: IN_PROGRESS -->
                <div class="kanban-col" style="background:#fefce8; border-color:#fef08a;">
                    <div class="kanban-col-header" style="border-color:#fde047;">
                        <span class="kanban-col-title" style="color: #b45309;">
                            <span style="display:inline-block; width:8px; height:8px; border-radius:50%; background:#eab308;"></span>
                            Đang Tác Nghiệp (IN PROGRESS)
                        </span>
                        <span class="kanban-col-count" style="background:#fef08a; color:#854d0e;">${cntProgress}</span>
                    </div>

                    <div class="kanban-cards-wrapper">
                        <c:forEach var="task" items="${tasks}">
                            <c:if test="${task.taskStatus == 'IN_PROGRESS'}">
                                <div class="kanban-card">
                                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                                        <span class="card-topic-tag" style="background:#fef9c3; color:#a16207;">${task.topicCode}</span>
                                        <small style="color:var(--muted); font-size:11px;">#${task.taskId}</small>
                                    </div>
                                    <div style="font-weight:600; font-size:13px; margin-bottom:6px; color:#1e293b;">
                                        <c:out value="${task.topicTitle}" />
                                    </div>
                                    <div style="font-size:12px; color:#475569; margin-bottom:12px; line-height:1.4;">
                                        <c:out value="${task.description}" />
                                    </div>
                                    <div style="display:flex; justify-content:space-between; align-items:center; padding-top:10px; border-top:1px solid #fef08a; font-size:11px;">
                                        <span style="color:#64748b;">
                                            👤 <b><c:out value="${task.assigneeName}" /></b>
                                        </span>
                                        <div style="display:flex; gap:4px;">
                                            <button type="button" class="btn" style="padding:4px 6px; font-size:11px;" title="Lùi lại TODO" onclick="updateTaskStatus(${task.taskId}, 'TODO')">&larr;</button>
                                            <button type="button" class="btn primary" style="padding:4px 8px; font-size:11px;" onclick="openDeliverableModal(${task.taskId}, '${task.topicCode}', '${task.topicTitle}')">
                                                📦 Nộp bài
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </c:if>
                        </c:forEach>
                    </div>
                </div>

                <!-- Col 3: DONE -->
                <div class="kanban-col" style="background:#f0fdf4; border-color:#bbf7d0;">
                    <div class="kanban-col-header" style="border-color:#86efac;">
                        <span class="kanban-col-title" style="color: #15803d;">
                            <span style="display:inline-block; width:8px; height:8px; border-radius:50%; background:#22c55e;"></span>
                            Đã Nộp Sản Phẩm (DONE)
                        </span>
                        <span class="kanban-col-count" style="background:#dcfce7; color:#166534;">${cntDone}</span>
                    </div>

                    <div class="kanban-cards-wrapper">
                        <c:forEach var="task" items="${tasks}">
                            <c:if test="${task.taskStatus == 'DONE'}">
                                <div class="kanban-card">
                                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                                        <span class="card-topic-tag" style="background:#dcfce7; color:#15803d;">${task.topicCode}</span>
                                        <span style="background:#dcfce7; color:#15803d; border-radius:10px; padding:2px 8px; font-size:10px; font-weight:700;">✓ HOÀN THÀNH</span>
                                    </div>
                                    <div style="font-weight:600; font-size:13px; margin-bottom:6px; color:#1e293b;">
                                        <c:out value="${task.topicTitle}" />
                                    </div>
                                    <div style="font-size:12px; color:#475569; margin-bottom:12px; line-height:1.4;">
                                        <c:out value="${task.description}" />
                                    </div>
                                    <div style="display:flex; justify-content:space-between; align-items:center; padding-top:10px; border-top:1px solid #bbf7d0; font-size:11px;">
                                        <span style="color:#64748b;">
                                            👤 <b><c:out value="${task.assigneeName}" /></b>
                                        </span>
                                        <div style="display:flex; gap:4px;">
                                            <button type="button" class="btn" style="padding:4px 6px; font-size:11px;" title="Lùi lại IN_PROGRESS" onclick="updateTaskStatus(${task.taskId}, 'IN_PROGRESS')">&larr;</button>
                                            <button type="button" class="btn" style="padding:4px 8px; font-size:11px; background:#e0f2fe; color:#0369a1; border-color:#bae6fd;" onclick="openDeliverableModal(${task.taskId}, '${task.topicCode}', '${task.topicTitle}')">
                                                📦 Nộp thêm
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </c:if>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<!-- Modal 1: Giao Nhiệm Vụ Mới -->
<div id="createTaskModal" class="modal-overlay">
    <div class="modal-box">
        <div class="modal-head">
            <h3 style="margin:0; font-size:16px;">Giao Nhiệm Vụ Tác Nghiệp Hiện Trường</h3>
            <button type="button" style="background:none; border:none; font-size:20px; cursor:pointer;" onclick="closeModal('createTaskModal')">&times;</button>
        </div>
        <form id="createTaskForm" onsubmit="submitCreateTask(event)">
            <div class="modal-body">
                <div style="margin-bottom:14px;">
                    <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px;">Chọn Đề Tài Đã Duyệt</label>
                    <select name="topicId" id="createTopicId" required style="width:100%; padding:9px 12px; border:1px solid var(--line); border-radius:8px; font-size:13px;">
                        <c:forEach var="tp" items="${approvedTopics}">
                            <option value="${tp[0]}">[${tp[1]}] ${tp[2]} (${tp[3]})</option>
                        </c:forEach>
                    </select>
                </div>
                <div style="margin-bottom:14px;">
                    <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px;">Phóng Viên Đảm Nhận</label>
                    <select name="assigneeUserId" id="createAssigneeId" required style="width:100%; padding:9px 12px; border:1px solid var(--line); border-radius:8px; font-size:13px;">
                        <c:forEach var="u" items="${users}">
                            <option value="${u.userId}">${u.fullName} (${u.roleName} — ${u.email})</option>
                        </c:forEach>
                    </select>
                </div>
                <div style="margin-bottom:14px;">
                    <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px;">Mô Tả Nhiệm Vụ Hiện Trường</label>
                    <textarea name="description" id="createDescription" rows="4" required placeholder="Nhập yêu cầu tác nghiệp, địa bàn khảo sát, phỏng vấn nhân vật..." style="width:100%; padding:10px 12px; border:1px solid var(--line); border-radius:8px; font-size:13px; box-sizing:border-box;"></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn" onclick="closeModal('createTaskModal')">Hủy</button>
                <button type="submit" class="btn primary" id="btnSaveTask">Tạo &amp; Giao Việc</button>
            </div>
        </form>
    </div>
</div>

<!-- Modal 2: Nộp Sản Phẩm Hiện Trường (SCR-4.3) -->
<div id="deliverableModal" class="modal-overlay">
    <div class="modal-box">
        <div class="modal-head">
            <h3 style="margin:0; font-size:16px;">Nộp Sản Phẩm Bàn Giao Hiện Trường (SCR-4.3)</h3>
            <button type="button" style="background:none; border:none; font-size:20px; cursor:pointer;" onclick="closeModal('deliverableModal')">&times;</button>
        </div>
        <form id="deliverableForm" onsubmit="submitDeliverable(event)">
            <input type="hidden" name="taskId" id="deliverableTaskId" value=""/>
            <div class="modal-body">
                <div id="deliverableTopicInfo" style="background:#f1f5f9; padding:10px 14px; border-radius:8px; margin-bottom:16px; font-size:12px; color:#334155;"></div>

                <div style="margin-bottom:14px;">
                    <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px;">Hình Thức Nộp</label>
                    <div style="display:flex; gap:20px;">
                        <label style="display:flex; align-items:center; gap:6px; font-size:13px; cursor:pointer;">
                            <input type="radio" name="submissionType" value="FILE" checked onchange="toggleSubmissionType('FILE')">
                            Tệp tài liệu (Word/PDF tối đa 15MB)
                        </label>
                        <label style="display:flex; align-items:center; gap:6px; font-size:13px; cursor:pointer;">
                            <input type="radio" name="submissionType" value="LINK" onchange="toggleSubmissionType('LINK')">
                            Đường link (Drive, YouTube, Cloud...)
                        </label>
                    </div>
                </div>

                <div id="fileSection" style="margin-bottom:14px;">
                    <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px;">Chọn File (.docx, .doc, .pdf)</label>
                    <input type="file" name="file" id="deliverableFile" accept=".pdf,.doc,.docx" style="width:100%; padding:8px; border:1px dashed var(--line); border-radius:8px; font-size:13px;"/>
                </div>

                <div id="linkSection" style="margin-bottom:14px; display:none;">
                    <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px;">Liên Kết Trực Tuyến</label>
                    <input type="url" name="externalUrl" id="deliverableUrl" placeholder="https://drive.google.com/..." style="width:100%; padding:9px 12px; border:1px solid var(--line); border-radius:8px; font-size:13px; box-sizing:border-box;"/>
                </div>

                <div style="margin-bottom:14px;">
                    <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px;">Cập Nhật Trạng Thái Đầu Việc</label>
                    <select name="taskStatus" id="deliverableStatus" style="width:100%; padding:9px 12px; border:1px solid var(--line); border-radius:8px; font-size:13px;">
                        <option value="DONE" selected>Hoàn thành bàn giao (DONE) &rarr; Chuyển đề tài sang DELIVERED</option>
                        <option value="IN_PROGRESS">Vẫn đang tác nghiệp (IN_PROGRESS) — Nộp bản nháp/bán phần</option>
                    </select>
                </div>

                <div style="margin-bottom:14px;">
                    <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px;">Ghi Chú Bàn Giao</label>
                    <textarea name="deliverableNote" id="deliverableNote" rows="3" required placeholder="Ghi chú tóm tắt nội dung tài liệu hoặc lưu ý cho Ban Biên Tập..." style="width:100%; padding:10px 12px; border:1px solid var(--line); border-radius:8px; font-size:13px; box-sizing:border-box;"></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn" onclick="closeModal('deliverableModal')">Hủy</button>
                <button type="submit" class="btn primary" id="btnSaveDeliverable">Xác Nhận Nộp Sản Phẩm</button>
            </div>
        </form>
    </div>
</div>

<!-- Modal 3: Báo Cáo Sự Cố Khẩn Cấp (SCR-4.4) -->
<div id="emergencyModal" class="modal-overlay">
    <div class="modal-box">
        <div class="modal-head" style="background:#fff1f2; border-color:#fecdd3;">
            <h3 style="margin:0; font-size:16px; color:#e11d48;">🚨 Báo Cáo Sự Cố Hiện Trường Khẩn Cấp (SCR-4.4)</h3>
            <button type="button" style="background:none; border:none; font-size:20px; cursor:pointer;" onclick="closeModal('emergencyModal')">&times;</button>
        </div>
        <form id="emergencyForm" onsubmit="submitEmergency(event)">
            <div class="modal-body">
                <p style="font-size:12px; color:#e11d48; margin-top:0;">
                    Thông báo khẩn cấp sẽ ngay lập tức được gửi tới <b>Trưởng Ban Biên Tập</b> và <b>Điều phối viên Hiện trường</b> qua chuông cảnh báo hệ thống!
                </p>
                <div style="margin-bottom:14px;">
                    <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px;">Chọn Đề Tài Đang Gặp Sự Cố</label>
                    <select name="topicId" id="emergencyTopicId" required style="width:100%; padding:9px 12px; border:1px solid var(--line); border-radius:8px; font-size:13px;">
                        <c:forEach var="tp" items="${approvedTopics}">
                            <option value="${tp[0]}">[${tp[1]}] ${tp[2]}</option>
                        </c:forEach>
                    </select>
                </div>
                <div style="margin-bottom:14px;">
                    <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px;">Mức Độ Khẩn Nguy</label>
                    <select name="severity" id="emergencySeverity" style="width:100%; padding:9px 12px; border:1px solid var(--line); border-radius:8px; font-size:13px;">
                        <option value="HIGH">Ưu tiên cao (HIGH) — Cản trở tác nghiệp thông thường</option>
                        <option value="CRITICAL">Đặc biệt nghiêm trọng (CRITICAL) — An toàn / Pháp lý / Sự cố khẩn cấp</option>
                    </select>
                </div>
                <div style="margin-bottom:14px;">
                    <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px;">Mô Tả Chi Tiết Sự Cố</label>
                    <textarea name="incidentDescription" id="emergencyDescription" rows="4" required placeholder="Mô tả cụ thể sự việc tại hiện trường, khó khăn gặp phải, sự hỗ trợ khẩn cấp cần thiết..." style="width:100%; padding:10px 12px; border:1px solid var(--line); border-radius:8px; font-size:13px; box-sizing:border-box;"></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn" onclick="closeModal('emergencyModal')">Hủy</button>
                <button type="submit" class="btn" style="background:#e11d48; color:#fff;" id="btnSaveEmergency">Phát Báo Động Khẩn Cấp</button>
            </div>
        </form>
    </div>
</div>

<script>
function openModal(id) {
    document.getElementById(id).style.display = 'flex';
}
function closeModal(id) {
    document.getElementById(id).style.display = 'none';
}

function openCreateTaskModal() {
    openModal('createTaskModal');
}

function openEmergencyModal() {
    openModal('emergencyModal');
}

function openDeliverableModal(taskId, topicCode, topicTitle) {
    document.getElementById('deliverableTaskId').value = taskId;
    document.getElementById('deliverableTopicInfo').innerHTML = 'Đầu việc #' + taskId + ' &bull; Đề tài: <b>[' + topicCode + '] ' + topicTitle + '</b>';
    openModal('deliverableModal');
}

function toggleSubmissionType(type) {
    if (type === 'FILE') {
        document.getElementById('fileSection').style.display = 'block';
        document.getElementById('linkSection').style.display = 'none';
    } else {
        document.getElementById('fileSection').style.display = 'none';
        document.getElementById('linkSection').style.display = 'block';
    }
}

async function updateTaskStatus(taskId, status) {
    try {
        const resp = await fetch('${base}/api/fieldwork-tasks', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ taskId: taskId, taskStatus: status })
        });
        const res = await resp.json();
        if (resp.ok) {
            window.location.reload();
        } else {
            alert(res.error || 'Cập nhật trạng thái thất bại.');
        }
    } catch (e) {
        alert('Lỗi kết nối: ' + e.message);
    }
}

async function submitCreateTask(e) {
    e.preventDefault();
    const btn = document.getElementById('btnSaveTask');
    btn.disabled = true;
    try {
        const topicId = parseInt(document.getElementById('createTopicId').value, 10);
        const assigneeUserId = parseInt(document.getElementById('createAssigneeId').value, 10);
        const description = document.getElementById('createDescription').value.trim();

        const resp = await fetch('${base}/api/fieldwork-tasks', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ topicId, assigneeUserId, description })
        });
        const res = await resp.json();
        if (resp.ok) {
            window.location.reload();
        } else {
            alert(res.error || 'Không thể tạo nhiệm vụ.');
            btn.disabled = false;
        }
    } catch (e) {
        alert('Lỗi: ' + e.message);
        btn.disabled = false;
    }
}

async function submitDeliverable(e) {
    e.preventDefault();
    const btn = document.getElementById('btnSaveDeliverable');
    btn.disabled = true;
    try {
        const form = document.getElementById('deliverableForm');
        const formData = new FormData(form);

        const resp = await fetch('${base}/api/fieldwork-deliverables', {
            method: 'POST',
            body: formData
        });
        const res = await resp.json();
        if (resp.ok) {
            alert(res.message || 'Nộp sản phẩm thành công!');
            window.location.reload();
        } else {
            alert(res.error || 'Nộp sản phẩm thất bại.');
            btn.disabled = false;
        }
    } catch (e) {
        alert('Lỗi kết nối: ' + e.message);
        btn.disabled = false;
    }
}

async function submitEmergency(e) {
    e.preventDefault();
    const btn = document.getElementById('btnSaveEmergency');
    btn.disabled = true;
    try {
        const topicId = parseInt(document.getElementById('emergencyTopicId').value, 10);
        const severity = document.getElementById('emergencySeverity').value;
        const desc = document.getElementById('emergencyDescription').value.trim();
        const fullDesc = '[' + (severity === 'CRITICAL' ? 'NGHIÊM TRỌNG ĐẶC BIỆT' : 'KHẨN CẤP') + '] ' + desc;

        const resp = await fetch('${base}/api/emergency-incidents', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ topicId, incidentDescription: fullDesc })
        });
        const res = await resp.json();
        if (resp.ok) {
            alert(res.message || 'Đã phát báo động khẩn cấp thành công!');
            closeModal('emergencyModal');
            document.getElementById('emergencyDescription').value = '';
            btn.disabled = false;
        } else {
            alert(res.error || 'Gửi báo động thất bại.');
            btn.disabled = false;
        }
    } catch (e) {
        alert('Lỗi kết nối: ' + e.message);
        btn.disabled = false;
    }
}
</script>
</body>
</html>
