<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phiếu Đề Xuất Đề Tài Mới — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
    <link rel="stylesheet" href="${base}/css/reporter.css">
</head>
<body>
<div class="app">
    <jsp:include page="/WEB-INF/views/sidebar.jsp">
        <jsp:param name="active" value="reporter-compose"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="/WEB-INF/views/topbar.jsp">
            <jsp:param name="title" value="Phiếu Đề Xuất Đề Tài"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">PHÂN HỆ PHÓNG VIÊN / WF-01</span>
                    <h1>${formTopic.topicId > 0 ? 'Chỉnh sửa đề xuất đề tài' : 'Soạn thảo đề xuất đề tài mới'}</h1>
                    <p>Điền ý tưởng, góc tiếp cận độc quyền, dự toán kinh phí và đăng ký nhân sự tác nghiệp để lưu nháp hoặc gửi thẩm định.</p>
                </div>
                <div class="page-actions">
                    <a class="btn" href="${base}/reporter/duplicate-search">
                        <i>🔍</i> <span>Tra cứu trùng lặp</span>
                    </a>
                    <a class="btn" href="${base}/reporter/topics">
                        &larr; <span>Danh sách đề tài</span>
                    </a>
                </div>
            </div>

            <c:if test="${not empty error}">
                <div style="background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce; border-radius: 10px; padding: 12px 16px; margin-bottom: 20px; font-weight: 600;">
                    <strong>✕ Lỗi:</strong> <c:out value="${error}"/>
                </div>
            </c:if>

            <c:if test="${not empty formTopic.rejectionReason}">
                <div class="alert" style="background:#fff4df;color:var(--warning);border:1px solid #f4ddb2;padding:14px 16px;border-radius:10px;margin-bottom:20px">
                    <strong>Yêu cầu chỉnh sửa từ Biên tập viên:</strong>
                    <p style="margin:6px 0 0;font-weight:600"><c:out value="${formTopic.rejectionReason}"/></p>
                </div>
            </c:if>

            <div class="detail-grid">
                <section class="card">
                    <div class="form-title-heading">
                        Phiếu Đề Xuất Đề Tài Báo Chí (NTMCS)
                    </div>

                    <form method="post" action="${base}/reporter/compose" id="pitch-form">
                        <input type="hidden" name="topicId" value="${formTopic.topicId}">

                        <div class="form-grid">
                            <div class="field full">
                                <div style="display:flex;justify-content:space-between;align-items:center">
                                    <label for="title">Tiêu đề đề tài <span style="color:var(--danger)">*</span></label>
                                    <button type="button" class="btn sm" id="btn-check-duplicate" style="font-size:11.5px;padding:4px 8px">
                                        🔍 Kiểm tra trùng lặp nhanh
                                    </button>
                                </div>
                                <input id="title" name="title" maxlength="200" required value="<c:out value='${formTopic.title}'/>"
                                       placeholder="Nhập tiêu đề đề tài rõ ràng, mang tính thời sự...">
                                <small class="hint">Tiêu đề cần ngắn gọn, rõ sự kiện hoặc hiện tượng xã hội cần phản ánh.</small>
                            </div>

                            <div class="field full">
                                <label for="angle">Góc tiếp cận / Trọng tâm <span style="color:var(--danger)">*</span></label>
                                <textarea id="angle" name="angle" maxlength="1500" rows="3" required
                                          placeholder="Mô tả góc nhìn độc quyền, điểm nhấn nội dung..."><c:out value="${formTopic.angle}"/></textarea>
                                <small class="hint">Góc tiếp cận là điểm cốt lõi phân biệt đề tài này với các tin tức sự kiện thông thường.</small>
                            </div>

                            <div class="field full">
                                <label for="summary">Tóm tắt ý tưởng / Hiện tượng sự kiện</label>
                                <textarea id="summary" name="summary" maxlength="2000" rows="2"
                                          placeholder="Sự việc diễn ra ở đâu, đối tượng nào liên quan, tại sao cần thực hiện bài viết này?"><c:out value="${formTopic.summary}"/></textarea>
                            </div>

                            <div class="field">
                                <label for="categoryId">
                                    Chuyên mục phân loại <span style="color:var(--danger)">*</span>
                                </label>
                                <select id="categoryId" name="categoryId" required>
                                    <option value="">-- Chọn chuyên mục --</option>
                                    <c:forEach var="c" items="${categories}">
                                        <option value="${c.categoryId}" ${formTopic.categoryId == c.categoryId ? 'selected' : ''}>
                                            <c:out value="${c.categoryName}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="field">
                                <label for="deadline">Thời hạn hoàn thành dự kiến <span style="color:var(--danger)">*</span></label>
                                <input id="deadline" name="deadline" type="date" value="${formTopic.deadline}" min="${today}" required>
                            </div>

                            <div class="field">
                                <label for="genre">Thể loại báo chí <span style="color:var(--danger)">*</span></label>
                                <select id="genre" name="genre" required>
                                    <c:forEach var="g" items="${genres}">
                                        <option value="${g}" ${formTopic.genre eq g ? 'selected' : ''}><c:out value="${g}"/></option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="field">
                                <label for="priority">Mức độ ưu tiên</label>
                                <select id="priority" name="priority">
                                    <c:forEach var="p" items="${priorities}">
                                        <option value="${p}" ${formTopic.priority eq p ? 'selected' : ''}>
                                            <c:choose>
                                                <c:when test="${p eq 'NORMAL'}">Thường (NORMAL)</c:when>
                                                <c:when test="${p eq 'HOT'}">Tin nóng (HOT)</c:when>
                                                <c:when test="${p eq 'URGENT'}">Khẩn cấp (URGENT)</c:when>
                                                <c:otherwise><c:out value="${p}"/></c:otherwise>
                                            </c:choose>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>

                        <!-- PHẦN 1: DỰ TOÁN KINH PHÍ TÁC NGHIỆP (Topic_Estimate) -->
                        <div class="form-section-box">
                            <div class="form-section-title">
                                <span>Dự toán kinh phí tác nghiệp (Topic_Estimate)</span>
                            </div>

                            <table class="nested-form-table" id="estimate-table">
                                <thead>
                                    <tr>
                                        <th style="width: 58%;">KHOẢN MỤC CHI PHÍ</th>
                                        <th style="width: 28%;">SỐ TIỀN (VNĐ)</th>
                                        <th style="width: 14%; text-align: center;">THAO TÁC</th>
                                    </tr>
                                </thead>
                                <tbody id="estimate-table-body">
                                    <c:choose>
                                        <c:when test="${not empty formEstimates}">
                                            <c:forEach var="est" items="${formEstimates}">
                                                <tr>
                                                    <td>
                                                        <input type="text" name="costItem" value="<c:out value='${est.costItem}'/>" placeholder="Nhập tên khoản mục chi phí..." required>
                                                    </td>
                                                    <td>
                                                        <input type="number" name="amount" class="estimate-amount-input" value="${est.amount}" min="0" step="1000" placeholder="0" required>
                                                    </td>
                                                    <td style="text-align: center;">
                                                        <button type="button" class="btn-danger-sm btn-del-estimate">Xóa</button>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td>
                                                    <input type="text" name="costItem" value="" placeholder="Nhập tên khoản mục chi phí...">
                                                </td>
                                                <td>
                                                    <input type="number" name="amount" class="estimate-amount-input" value="0" min="0" step="1000" placeholder="0">
                                                </td>
                                                <td style="text-align: center;">
                                                    <button type="button" class="btn-danger-sm btn-del-estimate">Xóa</button>
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>

                            <div class="estimate-summary-bar">
                                <button type="button" class="btn sm" id="btn-add-estimate">
                                    + Thêm khoản chi
                                </button>
                                <div class="estimate-total-label">
                                    Tổng cộng: <strong id="estimate-total-display">0 VNĐ</strong>
                                </div>
                            </div>
                        </div>

                        <!-- PHẦN 2: THÀNH VIÊN TỔ TÁC NGHIỆP (Topic_Member) -->
                        <div class="form-section-box">
                            <div class="form-section-title">
                                <span>Thành viên tổ tác nghiệp (Topic_Member)</span>
                            </div>

                            <table class="nested-form-table" id="member-table">
                                <thead>
                                    <tr>
                                        <th style="width: 58%;">NHÂN SỰ</th>
                                        <th style="width: 28%;">VAI TRÒ TRONG NHÓM</th>
                                        <th style="width: 14%; text-align: center;">THAO TÁC</th>
                                    </tr>
                                </thead>
                                <tbody id="member-table-body">
                                    <c:choose>
                                        <c:when test="${not empty formMembers}">
                                            <c:forEach var="m" items="${formMembers}">
                                                <tr class="member-row">
                                                    <td>
                                                        <select name="memberUserId" class="user-select-box" required>
                                                            <option value="">-- Chọn phóng viên tác nghiệp --</option>
                                                            <c:forEach var="u" items="${activeUsers}">
                                                                <option value="${u.userId}" ${m.userId == u.userId ? 'selected' : ''}>
                                                                    ${u.fullName} (Phóng viên${not empty u.deskName ? ' - ' += u.deskName : ''})
                                                                </option>
                                                            </c:forEach>
                                                        </select>
                                                        <div class="schedule-feedback-box"></div>
                                                    </td>
                                                    <td>
                                                        <select name="memberRole" required>
                                                            <option value="LEAD_REPORTER" ${m.roleInTeam eq 'LEAD_REPORTER' ? 'selected' : ''}>Phóng viên chính</option>
                                                            <option value="PHOTO_REPORTER" ${m.roleInTeam eq 'PHOTO_REPORTER' ? 'selected' : ''}>Phóng viên ảnh</option>
                                                            <option value="CAMERAMAN" ${m.roleInTeam eq 'CAMERAMAN' ? 'selected' : ''}>Quay phim</option>
                                                            <option value="ASSISTANT" ${m.roleInTeam eq 'ASSISTANT' ? 'selected' : ''}>Trợ lý</option>
                                                        </select>
                                                    </td>
                                                    <td style="text-align: center;">
                                                        <button type="button" class="btn-danger-sm btn-del-member">Xóa</button>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr class="member-row">
                                                <td>
                                                    <select name="memberUserId" class="user-select-box">
                                                        <option value="">-- Chọn phóng viên tác nghiệp --</option>
                                                        <c:forEach var="u" items="${activeUsers}">
                                                            <option value="${u.userId}">${u.fullName} (Phóng viên${not empty u.deskName ? ' - ' += u.deskName : ''})</option>
                                                        </c:forEach>
                                                    </select>
                                                    <div class="schedule-feedback-box"></div>
                                                </td>
                                                <td>
                                                    <select name="memberRole">
                                                        <option value="LEAD_REPORTER">Phóng viên chính</option>
                                                        <option value="PHOTO_REPORTER">Phóng viên ảnh</option>
                                                        <option value="CAMERAMAN">Quay phim</option>
                                                        <option value="ASSISTANT">Trợ lý</option>
                                                    </select>
                                                </td>
                                                <td style="text-align: center;">
                                                    <button type="button" class="btn-danger-sm btn-del-member">Xóa</button>
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>

                            <div>
                                <button type="button" class="btn sm" id="btn-add-member">
                                    + Thêm thành viên
                                </button>
                            </div>
                        </div>

                        <div class="action-row" style="margin-top:24px;justify-content:flex-start;gap:12px">
                            <a class="btn" href="${base}/reporter/topics">
                                Hủy bỏ
                            </a>
                            <button class="btn action-btn-draft" type="submit" name="action" value="saveDraft">
                                💾 Lưu bản thảo (DRAFT)
                            </button>
                            <button class="btn primary" type="submit" name="action" value="submit">
                                🚀 Gửi duyệt đề xuất (SUBMITTED)
                            </button>
                        </div>
                    </form>
                </section>

                <aside>
                    <section class="card">
                        <h2>Quy tắc thẩm định (Criteria)</h2>
                        <p style="color:var(--muted);font-size:13px;line-height:1.6;margin:0 0 14px">
                            Đề xuất sẽ được Trưởng ban / Thư ký biên tập đánh giá dựa trên các tiêu chí chuẩn:
                        </p>
                        <div style="display:grid;gap:10px">
                            <div style="display:flex;justify-content:space-between;padding:8px 10px;background:var(--surface2);border-radius:8px">
                                <span>1. Tính thời sự &amp; sự kiện</span>
                                <strong>30%</strong>
                            </div>
                            <div style="display:flex;justify-content:space-between;padding:8px 10px;background:var(--surface2);border-radius:8px">
                                <span>2. Giá trị thông tin công chúng</span>
                                <strong>30%</strong>
                            </div>
                            <div style="display:flex;justify-content:space-between;padding:8px 10px;background:var(--surface2);border-radius:8px">
                                <span>3. Tính khả thi &amp; thực địa</span>
                                <strong>20%</strong>
                            </div>
                            <div style="display:flex;justify-content:space-between;padding:8px 10px;background:var(--surface2);border-radius:8px">
                                <span>4. Góc nhìn mới (Độc đáo)</span>
                                <strong>20%</strong>
                            </div>
                        </div>
                    </section>

                    <section class="card" style="margin-top:20px">
                        <h2>Thông tin tác giả</h2>
                        <div style="display:flex;align-items:center;gap:12px">
                            <div class="avatar" style="width:40px;height:40px;border-radius:10px;background:#eef2ff;color:var(--primary);display:grid;place-items:center;font-weight:700">
                                <c:out value="${not empty currentUserName ? currentUserName.substring(0, 1).toUpperCase() : 'P'}"/>
                            </div>
                            <div>
                                <strong><c:out value="${not empty currentUserName ? currentUserName : sessionScope.loggedUser.fullName}"/></strong>
                                <small style="display:block;color:var(--muted)"><c:out value="${not empty currentUserEmail ? currentUserEmail : sessionScope.loggedUser.email}"/> &middot; <c:out value="${not empty currentUserDesk ? currentUserDesk : 'Tòa soạn'}"/></small>
                            </div>
                        </div>
                        <p style="color:var(--muted);font-size:12px;margin:12px 0 0;line-height:1.5">
                            Mọi đề xuất được gán tự động cho tài khoản phóng viên đang đăng nhập.
                        </p>
                    </section>
                </aside>
            </div>
        </main>
    </div>
</div>

<!-- Template options for JavaScript dynamic rows -->
<div id="user-options-template" style="display:none;">
    <option value="">-- Chọn phóng viên tác nghiệp --</option>
    <c:forEach var="u" items="${activeUsers}">
        <option value="${u.userId}">${u.fullName} (Phóng viên${not empty u.deskName ? ' - ' += u.deskName : ''})</option>
    </c:forEach>
</div>

<script>
(function() {
    const BUSY_SCHEDULES = ${not empty userBusySchedulesJson ? userBusySchedulesJson : '{}'};

    function calculateEstimateTotal() {
        let total = 0;
        const inputs = document.querySelectorAll('.estimate-amount-input');
        inputs.forEach(function(input) {
            const val = parseFloat(input.value) || 0;
            total += val;
        });
        const display = document.getElementById('estimate-total-display');
        if (display) {
            display.textContent = total.toLocaleString('vi-VN') + ' VNĐ';
        }
    }

    document.getElementById('estimate-table-body')?.addEventListener('input', function(e) {
        if (e.target.classList.contains('estimate-amount-input')) {
            calculateEstimateTotal();
        }
    });

    document.getElementById('btn-add-estimate')?.addEventListener('click', function() {
        const tbody = document.getElementById('estimate-table-body');
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
                <input type="text" name="costItem" placeholder="Nhập tên khoản mục chi phí..." required>
            </td>
            <td>
                <input type="number" name="amount" class="estimate-amount-input" value="0" min="0" step="1000" placeholder="0" required>
            </td>
            <td style="text-align: center;">
                <button type="button" class="btn-danger-sm btn-del-estimate">Xóa</button>
            </td>
        `;
        tbody.appendChild(tr);
        calculateEstimateTotal();
    });

    document.getElementById('estimate-table-body')?.addEventListener('click', function(e) {
        if (e.target.classList.contains('btn-del-estimate')) {
            const row = e.target.closest('tr');
            if (row) {
                row.remove();
                calculateEstimateTotal();
            }
        }
    });

    function updateScheduleFeedback(selectEl) {
        const userId = selectEl.value;
        const parentCell = selectEl.closest('td');
        const feedbackBox = parentCell.querySelector('.schedule-feedback-box');
        if (!feedbackBox) return;

        if (!userId) {
            feedbackBox.innerHTML = '';
            return;
        }

        const allSelects = document.querySelectorAll('.user-select-box');
        let duplicateCount = 0;
        allSelects.forEach(function(s) {
            if (s.value === userId) duplicateCount++;
        });

        if (duplicateCount > 1) {
            feedbackBox.innerHTML = `
                <div class="schedule-status status-busy">
                    <span style="font-weight:bold">⚠ Cảnh báo:</span> Nhân sự này đã được thêm trong nhóm tác nghiệp!
                </div>
            `;
            return;
        }

        const busyTopics = BUSY_SCHEDULES[userId];
        if (busyTopics && busyTopics.length > 0) {
            feedbackBox.innerHTML = `
                <div class="schedule-status status-busy">
                    <span style="font-weight:bold">⚠ Bận lịch (Đề tài):</span> ` + busyTopics.join('; ') + `
                </div>
            `;
        } else {
            feedbackBox.innerHTML = `
                <div class="schedule-status status-free">
                    <span style="font-weight:bold">✓ Lịch tác nghiệp trống:</span> Sẵn sàng nhận nhiệm vụ
                </div>
            `;
        }
    }

    function refreshAllFeedbacks() {
        const selects = document.querySelectorAll('.user-select-box');
        selects.forEach(function(sel) {
            updateScheduleFeedback(sel);
        });
    }

    document.getElementById('member-table-body')?.addEventListener('change', function(e) {
        if (e.target.classList.contains('user-select-box')) {
            refreshAllFeedbacks();
        }
    });

    document.getElementById('btn-add-member')?.addEventListener('click', function() {
        const tbody = document.getElementById('member-table-body');
        const userOptionsHtml = document.getElementById('user-options-template')?.innerHTML || '';
        const tr = document.createElement('tr');
        tr.className = 'member-row';
        tr.innerHTML = `
            <td>
                <select name="memberUserId" class="user-select-box" required>
                    ` + userOptionsHtml + `
                </select>
                <div class="schedule-feedback-box"></div>
            </td>
            <td>
                <select name="memberRole" required>
                    <option value="LEAD_REPORTER">Phóng viên chính</option>
                    <option value="PHOTO_REPORTER">Phóng viên ảnh</option>
                    <option value="CAMERAMAN">Quay phim</option>
                    <option value="ASSISTANT" selected>Trợ lý</option>
                </select>
            </td>
            <td style="text-align: center;">
                <button type="button" class="btn-danger-sm btn-del-member">Xóa</button>
            </td>
        `;
        tbody.appendChild(tr);
    });

    document.getElementById('member-table-body')?.addEventListener('click', function(e) {
        if (e.target.classList.contains('btn-del-member')) {
            const row = e.target.closest('tr');
            if (row) {
                row.remove();
                refreshAllFeedbacks();
            }
        }
    });

    document.getElementById('btn-check-duplicate')?.addEventListener('click', function() {
        const titleVal = encodeURIComponent(document.getElementById('title')?.value || '');
        window.open('${base}/reporter/duplicate-search?keyword=' + titleVal, '_blank');
    });

    document.getElementById('pitch-form')?.addEventListener('submit', function(e) {
        const allSelects = document.querySelectorAll('.user-select-box');
        const userIds = new Set();
        let hasDuplicate = false;

        allSelects.forEach(function(s) {
            if (s.value) {
                if (userIds.has(s.value)) {
                    hasDuplicate = true;
                }
                userIds.add(s.value);
            }
        });

        if (hasDuplicate) {
            e.preventDefault();
            alert('Có nhân sự bị thêm trùng lặp nhiều lần trong danh sách tổ tác nghiệp. Vui lòng kiểm tra lại!');
            return false;
        }
    });

    calculateEstimateTotal();
    refreshAllFeedbacks();
})();
</script>
</body>
</html>
