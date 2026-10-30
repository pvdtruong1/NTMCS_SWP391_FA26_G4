<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="SCR-1.4: Phiếu Đề Xuất Đề Tài Mới — NTMCS" />
</jsp:include>
<c:set var="activeNav" value="compose" scope="request" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/topbar.jsp" />

<style>
/* Scoped form styles for SCR-1.4 */
.form-title-heading {
    font-size: 19px;
    font-weight: 700;
    color: var(--primary, #2563eb);
    margin: 0 0 18px 0;
    padding-bottom: 12px;
    border-bottom: 2px solid var(--line, #e2e8f0);
}
.form-section-box {
    border: 1px solid var(--line, #dbe2ea);
    border-radius: 10px;
    padding: 16px;
    margin-top: 20px;
    background: #fafbfd;
}
body.dark .form-section-box {
    background: var(--surface2, #182234);
    border-color: var(--line, #263247);
}
.form-section-title {
    font-size: 13.5px;
    font-weight: 700;
    color: var(--text, #1e293b);
    margin: 0 0 12px 0;
    display: flex;
    align-items: center;
    justify-content: space-between;
}
/* Override global table min-width to prevent overflow */
table.nested-form-table {
    width: 100% !important;
    min-width: 0 !important;
    max-width: 100% !important;
    table-layout: fixed !important;
    border-collapse: separate !important;
    border-spacing: 0 !important;
    border: 1px solid var(--line, #e2e8f0) !important;
    border-radius: 8px !important;
    background: var(--surface, #ffffff) !important;
    margin-bottom: 12px !important;
    overflow: hidden !important;
}
table.nested-form-table th {
    background: #f1f5f9 !important;
    padding: 9px 12px !important;
    font-size: 11px !important;
    font-weight: 700 !important;
    color: #475569 !important;
    border-bottom: 1px solid var(--line, #e2e8f0) !important;
    border-right: 1px solid var(--line, #e2e8f0) !important;
    text-transform: uppercase !important;
    letter-spacing: 0.5px !important;
}
table.nested-form-table th:last-child {
    border-right: none !important;
}
table.nested-form-table td {
    padding: 8px 10px !important;
    border-bottom: 1px solid var(--line, #e2e8f0) !important;
    border-right: 1px solid var(--line, #e2e8f0) !important;
    vertical-align: top !important;
    background: var(--surface, #ffffff) !important;
}
table.nested-form-table td:last-child {
    border-right: none !important;
}
table.nested-form-table tr:last-child td {
    border-bottom: none !important;
}
body.dark table.nested-form-table th {
    background: var(--surface2, #182234) !important;
    color: var(--muted, #9aa8bd) !important;
    border-color: var(--line, #263247) !important;
}
body.dark table.nested-form-table td {
    background: var(--surface, #121b2b) !important;
    border-color: var(--line, #263247) !important;
}
table.nested-form-table input,
table.nested-form-table select {
    width: 100% !important;
    box-sizing: border-box !important;
    padding: 8px 10px !important;
    font-size: 13.5px !important;
    border-radius: 6px !important;
    border: 1px solid var(--line, #cbd5e1) !important;
    background: var(--surface, #ffffff) !important;
    color: var(--text, #0f172a) !important;
    outline: none !important;
}
table.nested-form-table input:focus,
table.nested-form-table select:focus {
    border-color: var(--primary, #2563eb) !important;
    box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.15) !important;
}
.btn-danger-sm {
    background: #dc2626 !important;
    color: #ffffff !important;
    border: none !important;
    border-radius: 6px !important;
    padding: 7px 14px !important;
    font-size: 12.5px !important;
    font-weight: 600 !important;
    cursor: pointer !important;
    transition: background 0.15s ease !important;
    width: 100% !important;
    text-align: center !important;
}
.btn-danger-sm:hover {
    background: #b91c1c !important;
}
.schedule-status {
    font-size: 12px;
    padding: 7px 10px;
    border-radius: 6px;
    margin-top: 6px;
    line-height: 1.45;
    display: block;
    word-break: break-word;
}
.schedule-status.status-free {
    background: #f0fdf4;
    color: #166534;
    border: 1px solid #bbf7d0;
}
body.dark .schedule-status.status-free {
    background: rgba(34, 197, 94, 0.15);
    color: #86efac;
    border-color: rgba(34, 197, 94, 0.3);
}
.schedule-status.status-busy {
    background: #fffbeb;
    color: #92400e;
    border: 1px solid #fef3c7;
}
body.dark .schedule-status.status-busy {
    background: rgba(234, 179, 8, 0.15);
    color: #fde047;
    border-color: rgba(234, 179, 8, 0.3);
}
.estimate-summary-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 10px;
    flex-wrap: wrap;
    gap: 10px;
}
.estimate-total-label {
    font-size: 14px;
    font-weight: 600;
    color: var(--text, #1e293b);
}
.estimate-total-label strong {
    color: var(--primary, #2563eb);
    font-size: 15px;
}
.action-btn-draft {
    background: #1e293b !important;
    color: #ffffff !important;
    border: 1px solid #0f172a !important;
}
.action-btn-draft:hover {
    background: #0f172a !important;
}
</style>

<div class="page-head">
    <div>
        <div class="eyebrow">SCR-1.4 &middot; WORKFLOW 1</div>
        <h1>${formTopic.topicId > 0 ? 'Chỉnh sửa đề xuất đề tài' : 'Soạn thảo đề xuất đề tài mới'}</h1>
        <p>Điền ý tưởng, góc tiếp cận độc đáo, dự toán kinh phí và đăng ký nhân sự tác nghiệp để lưu nháp hoặc gửi thẩm định.</p>
    </div>
    <div class="page-actions">
        <a class="btn" href="${pageContext.request.contextPath}/reporter/duplicate-search">
            <i>🔍</i> <span>Tra cứu trùng lặp</span>
        </a>
        <a class="btn" href="${pageContext.request.contextPath}/reporter/topics">
            &larr; <span>Danh sách đề tài</span>
        </a>
    </div>
</div>

<c:if test="${not empty error}">
    <div class="alert danger">
        <strong>Lỗi:</strong> <c:out value="${error}"/>
    </div>
</c:if>

<c:if test="${not empty formTopic.rejectionReason}">
    <div class="alert">
        <strong>Yêu cầu chỉnh sửa từ Biên tập viên:</strong>
        <p style="margin:4px 0 0"><c:out value="${formTopic.rejectionReason}"/></p>
    </div>
</c:if>

<div class="detail-grid">
    <section class="card">
        <div class="form-title-heading">
            SCR-1.4: Phiếu Đề Xuất Đề Tài Mới (NTMCS)
        </div>

        <form method="post" action="${pageContext.request.contextPath}/reporter/compose" id="pitch-form">
            <input type="hidden" name="topicId" value="${formTopic.topicId}">

            <div class="form-grid">
                <div class="field full">
                    <div style="display:flex;justify-content:space-between;align-items:center">
                        <label for="title">Tiêu đề đề tài <span style="color:var(--danger)">*</span></label>
                        <button type="button" class="link" id="btn-check-duplicate" style="font-size:12px">
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
                    <label for="category">
                        Chuyên mục phân loại <span style="color:var(--danger)">*</span>
                    </label>
                    <select id="category" name="category" required>
                        <option value="">-- Chọn chuyên mục --</option>
                        <c:forEach var="c" items="${categories}">
                            <option value="<c:out value='${c}'/>"
                                    ${formTopic.category eq c ? 'selected' : ''}>
                                <c:out value="${c}"/>
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
                        <option value="NORMAL" ${formTopic.priority eq 'NORMAL' ? 'selected' : ''}>Thường (NORMAL)</option>
                        <option value="HOT" ${formTopic.priority eq 'HOT' ? 'selected' : ''}>Tin nóng (HOT)</option>
                        <option value="URGENT" ${formTopic.priority eq 'URGENT' ? 'selected' : ''}>Khẩn cấp (URGENT)</option>
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
                            <c:when test="${formTopic.topicId == 0}">
                                <!-- Dữ liệu mẫu ban đầu theo giao diện SCR-1.4 -->
                                <tr>
                                    <td>
                                        <input type="text" name="costItem" value="Chi phí di chuyển &amp; nhiên liệu xe hiện trường" placeholder="Nhập tên khoản mục chi phí..." required>
                                    </td>
                                    <td>
                                        <input type="number" name="amount" class="estimate-amount-input" value="1500000" min="0" step="1000" placeholder="0" required>
                                    </td>
                                    <td style="text-align: center;">
                                        <button type="button" class="btn-danger-sm btn-del-estimate">Xóa</button>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <input type="text" name="costItem" value="Bồi dưỡng cộng tác viên &amp; trinh sát địa bàn" placeholder="Nhập tên khoản mục chi phí..." required>
                                    </td>
                                    <td>
                                        <input type="number" name="amount" class="estimate-amount-input" value="2000000" min="0" step="1000" placeholder="0" required>
                                    </td>
                                    <td style="text-align: center;">
                                        <button type="button" class="btn-danger-sm btn-del-estimate">Xóa</button>
                                    </td>
                                </tr>
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
                                                <option value="">-- Chọn nhân sự --</option>
                                                <c:forEach var="u" items="${activeUsers}">
                                                    <option value="${u.userId}" ${m.userId == u.userId ? 'selected' : ''}>
                                                        ${u.fullName} (${u.roleName})
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
                            <c:when test="${formTopic.topicId == 0}">
                                <!-- Dữ liệu mẫu ban đầu theo đúng giao diện SCR-1.4 trong ảnh -->
                                <tr class="member-row">
                                    <td>
                                        <select name="memberUserId" class="user-select-box" required>
                                            <option value="">-- Chọn nhân sự --</option>
                                            <c:forEach var="u" items="${activeUsers}">
                                                <option value="${u.userId}" ${u.userId == 1 ? 'selected' : ''}>
                                                    ${u.fullName} (${u.roleName})
                                                </option>
                                            </c:forEach>
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
                                </tr>
                                <tr class="member-row">
                                    <td>
                                        <select name="memberUserId" class="user-select-box" required>
                                            <option value="">-- Chọn nhân sự --</option>
                                            <c:forEach var="u" items="${activeUsers}">
                                                <option value="${u.userId}" ${u.userId == 2 ? 'selected' : ''}>
                                                    ${u.fullName} (${u.roleName})
                                                </option>
                                            </c:forEach>
                                        </select>
                                        <div class="schedule-feedback-box"></div>
                                    </td>
                                    <td>
                                        <select name="memberRole" required>
                                            <option value="LEAD_REPORTER" selected>Phóng viên chính</option>
                                            <option value="PHOTO_REPORTER">Phóng viên ảnh</option>
                                            <option value="CAMERAMAN">Quay phim</option>
                                            <option value="ASSISTANT">Trợ lý</option>
                                        </select>
                                    </td>
                                    <td style="text-align: center;">
                                        <button type="button" class="btn-danger-sm btn-del-member">Xóa</button>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <tr class="member-row">
                                    <td>
                                        <select name="memberUserId" class="user-select-box">
                                            <option value="">-- Chọn nhân sự --</option>
                                            <c:forEach var="u" items="${activeUsers}">
                                                <option value="${u.userId}">${u.fullName} (${u.roleName})</option>
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
                <a class="btn" href="${pageContext.request.contextPath}/reporter/topics">
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

        <section class="card">
            <h2>Thông tin tác giả</h2>
            <div style="display:flex;align-items:center;gap:12px">
                <div class="avatar">PV</div>
                <div>
                    <strong>Mai Khánh Hùng</strong>
                    <small style="display:block;color:var(--muted)">hung@ntmcs.local &middot; Ban Thời sự</small>
                </div>
            </div>
            <p style="color:var(--muted);font-size:12px;margin:12px 0 0;line-height:1.5">
                Mọi đề xuất được gán tự động cho tài khoản phóng viên đang đăng nhập.
            </p>
        </section>
    </aside>
</div>

<!-- Template options for JavaScript dynamic rows -->
<div id="user-options-template" style="display:none;">
    <option value="">-- Chọn nhân sự --</option>
    <c:forEach var="u" items="${activeUsers}">
        <option value="${u.userId}">${u.fullName} (${u.roleName})</option>
    </c:forEach>
</div>

<script>
(function() {
    // 1. Data mapping of user schedules from Server (passed via controller)
    const BUSY_SCHEDULES = ${not empty userBusySchedulesJson ? userBusySchedulesJson : '{}'};

    // 2. Budget / Estimate Total Calculation
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

    // Bind amount input change
    document.getElementById('estimate-table-body')?.addEventListener('input', function(e) {
        if (e.target.classList.contains('estimate-amount-input')) {
            calculateEstimateTotal();
        }
    });

    // Add new estimate row
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

    // Delete estimate row
    document.getElementById('estimate-table-body')?.addEventListener('click', function(e) {
        if (e.target.classList.contains('btn-del-estimate')) {
            const row = e.target.closest('tr');
            if (row) {
                row.remove();
                calculateEstimateTotal();
            }
        }
    });

    // 3. Personnel / Member Schedule Checking
    function updateScheduleFeedback(selectEl) {
        const userId = selectEl.value;
        const parentCell = selectEl.closest('td');
        const feedbackBox = parentCell.querySelector('.schedule-feedback-box');
        if (!feedbackBox) return;

        if (!userId) {
            feedbackBox.innerHTML = '';
            return;
        }

        // Check if duplicate user selected in another row
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

    // Refresh all feedbacks
    function refreshAllFeedbacks() {
        const selects = document.querySelectorAll('.user-select-box');
        selects.forEach(function(sel) {
            updateScheduleFeedback(sel);
        });
    }

    // Bind change on user select
    document.getElementById('member-table-body')?.addEventListener('change', function(e) {
        if (e.target.classList.contains('user-select-box')) {
            refreshAllFeedbacks();
        }
    });

    // Add member row
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

    // Delete member row
    document.getElementById('member-table-body')?.addEventListener('click', function(e) {
        if (e.target.classList.contains('btn-del-member')) {
            const row = e.target.closest('tr');
            if (row) {
                row.remove();
                refreshAllFeedbacks();
            }
        }
    });

    // Validate form on submit
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

    // Initial calculations on load
    document.addEventListener('DOMContentLoaded', function() {
        calculateEstimateTotal();
        refreshAllFeedbacks();
    });

    // Run immediately as well in case script loads after DOM
    calculateEstimateTotal();
    refreshAllFeedbacks();
})();
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
