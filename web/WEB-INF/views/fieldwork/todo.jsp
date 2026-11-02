<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Việc Hiện Trường Của Tôi — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
    <style>
        .filter-tab {
            padding: 8px 16px;
            border-radius: 8px;
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
            border: 1px solid var(--line);
            background: var(--surface);
            color: var(--muted);
            transition: all 0.15s ease;
        }
        .filter-tab.active {
            background: var(--primary);
            color: #fff;
            border-color: var(--primary);
        }
        .task-row-card {
            background: var(--surface);
            border: 1px solid var(--line);
            border-radius: 10px;
            padding: 16px 20px;
            margin-bottom: 12px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
            transition: box-shadow 0.15s ease;
        }
        .task-row-card:hover {
            box-shadow: 0 4px 12px rgba(0,0,0,0.06);
        }
        .badge-status {
            padding: 4px 10px;
            border-radius: 20px;
            font-size: 11px;
            font-weight: 700;
            display: inline-block;
        }
        .badge-todo { background: #f1f5f9; color: #475569; }
        .badge-in_progress { background: #fef3c7; color: #b45309; }
        .badge-done { background: #dcfce7; color: #15803d; }

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
        <jsp:param name="active" value="fieldwork-todo"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="../topbar.jsp">
            <jsp:param name="title" value="Việc Hiện Trường Của Tôi (SCR-4.2)"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">TÁC NGHIỆP CÁ NHÂN / WF-04</span>
                    <h1>Việc Hiện Trường Của Tôi (SCR-4.2)</h1>
                    <p>Theo dõi các đầu việc tác nghiệp hiện trường được phân công, cập nhật tiến độ và nộp sản phẩm hoàn thành (SCR-4.3).</p>
                </div>
                <div class="page-actions">
                    <button type="button" class="btn" style="background: #fff1f2; color: #e11d48; border: 1px solid #fecdd3;" onclick="openEmergencyModal()">
                        🚨 Báo Cáo Sự Cố Khẩn Cấp
                    </button>
                </div>
            </div>

            <!-- Filter Tabs -->
            <div style="display: flex; gap: 10px; margin-bottom: 20px;">
                <button type="button" class="filter-tab active" onclick="filterStatus('ALL', this)">Tất Cả (${tasks.size()})</button>
                <button type="button" class="filter-tab" onclick="filterStatus('TODO', this)">Chờ Tiếp Nhận</button>
                <button type="button" class="filter-tab" onclick="filterStatus('IN_PROGRESS', this)">Đang Thực Hiện</button>
                <button type="button" class="filter-tab" onclick="filterStatus('DONE', this)">Đã Hoàn Thành</button>
            </div>

            <c:if test="${empty tasks}">
                <div style="text-align:center; padding:60px 20px; background:var(--surface); border:1px dashed var(--line); border-radius:12px; color:var(--muted);">
                    <div style="font-size:36px; margin-bottom:12px;">🎉</div>
                    <div style="font-size:16px; font-weight:600; color:#1e293b; margin-bottom:6px;">Bạn không có nhiệm vụ hiện trường nào cần thực hiện!</div>
                    <div style="font-size:13px;">Khi Ban Biên Tập phân công nhiệm vụ mới, thông tin sẽ xuất hiện tại đây.</div>
                </div>
            </c:if>

            <div id="taskList">
                <c:forEach var="task" items="${tasks}">
                    <div class="task-row-card" data-status="${task.taskStatus}">
                        <div style="flex:1;">
                            <div style="display:flex; align-items:center; gap:8px; margin-bottom:6px;">
                                <span style="font-family:monospace; font-weight:700; font-size:11px; background:#eff6ff; color:var(--primary); padding:2px 8px; border-radius:4px;">
                                    ${task.topicCode}
                                </span>
                                <c:choose>
                                    <c:when test="${task.taskStatus == 'TODO'}">
                                        <span class="badge-status badge-todo">Chờ tiếp nhận</span>
                                    </c:when>
                                    <c:when test="${task.taskStatus == 'IN_PROGRESS'}">
                                        <span class="badge-status badge-in_progress">⚡ Đang tác nghiệp</span>
                                    </c:when>
                                    <c:when test="${task.taskStatus == 'DONE'}">
                                        <span class="badge-status badge-done">✓ Đã nộp sản phẩm</span>
                                    </c:when>
                                </c:choose>
                                <small style="color:var(--muted); font-size:11px;">#Nhiệm vụ: ${task.taskId}</small>
                            </div>
                            <div style="font-size:15px; font-weight:700; color:#0f172a; margin-bottom:4px;">
                                <c:out value="${task.topicTitle}" />
                            </div>
                            <div style="font-size:13px; color:#475569; line-height:1.4;">
                                <c:out value="${task.description}" />
                            </div>
                        </div>

                        <div style="display:flex; align-items:center; gap:8px;">
                            <c:if test="${task.taskStatus == 'TODO'}">
                                <button type="button" class="btn primary" onclick="updateTaskStatus(${task.taskId}, 'IN_PROGRESS')">
                                    Tiếp Nhận &amp; Bắt Đầu &rarr;
                                </button>
                            </c:if>
                            <c:if test="${task.taskStatus == 'IN_PROGRESS'}">
                                <button type="button" class="btn primary" style="background:#0284c7;" onclick="openDeliverableModal(${task.taskId}, '${task.topicCode}', '${task.topicTitle}')">
                                    📦 Nộp Sản Phẩm Hoàn Thành
                                </button>
                            </c:if>
                            <c:if test="${task.taskStatus == 'DONE'}">
                                <button type="button" class="btn" style="background:#e0f2fe; color:#0369a1; border-color:#bae6fd;" onclick="openDeliverableModal(${task.taskId}, '${task.topicCode}', '${task.topicTitle}')">
                                    📦 Bổ Sung / Nộp Lại
                                </button>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </main>
    </div>
</div>

<!-- Modal: Nộp Sản Phẩm Hiện Trường (SCR-4.3) -->
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

<!-- Modal: Báo Cáo Sự Cố Khẩn Cấp (SCR-4.4) -->
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
function openModal(id) { document.getElementById(id).style.display = 'flex'; }
function closeModal(id) { document.getElementById(id).style.display = 'none'; }

function openEmergencyModal() { openModal('emergencyModal'); }

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

function filterStatus(status, tabElem) {
    document.querySelectorAll('.filter-tab').forEach(t => t.classList.remove('active'));
    tabElem.classList.add('active');

    const rows = document.querySelectorAll('.task-row-card');
    rows.forEach(r => {
        if (status === 'ALL' || r.getAttribute('data-status') === status) {
            r.style.display = 'flex';
        } else {
            r.style.display = 'none';
        }
    });
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
