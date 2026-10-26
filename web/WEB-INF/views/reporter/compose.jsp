<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Soạn đề xuất đề tài — NTMCS" />
</jsp:include>
<c:set var="activeNav" value="compose" scope="request" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/topbar.jsp" />

<div class="page-head">
    <div>
        <div class="eyebrow">SCR-1.4 &middot; WORKFLOW 1</div>
        <h1>${formTopic.topicId > 0 ? 'Chỉnh sửa đề xuất đề tài' : 'Soạn thảo đề xuất đề tài mới'}</h1>
        <p>Điền ý tưởng, góc tiếp cận độc đáo, chuyên mục và thời hạn để lưu nháp hoặc gửi thẩm định.</p>
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
        <form method="post" action="${pageContext.request.contextPath}/reporter/compose" id="pitch-form">
            <input type="hidden" name="topicId" value="${formTopic.topicId}">

            <div class="form-grid">
                <div class="field full">
                    <div style="display:flex;justify-content:space-between;align-items:center">
                        <label for="title">Tiêu đề dự kiến <span style="color:var(--danger)">*</span></label>
                        <button type="button" class="link" id="btn-check-duplicate" style="font-size:12px">
                            🔍 Kiểm tra trùng lặp nhanh
                        </button>
                    </div>
                    <input id="title" name="title" maxlength="200" required value="<c:out value='${formTopic.title}'/>"
                           placeholder="Ví dụ: Mô hình giao thông xanh tại các quận nội đô Hà Nội">
                    <small class="hint">Tiêu đề cần ngắn gọn, rõ sự kiện hoặc hiện tượng xã hội cần phản ánh.</small>
                </div>

                <div class="field full">
                    <label for="summary">Tóm tắt ý tưởng / Hiện tượng sự kiện <span style="color:var(--danger)">*</span></label>
                    <textarea id="summary" name="summary" maxlength="2000" rows="3"
                              placeholder="Sự việc diễn ra ở đâu, đối tượng nào liên quan, tại sao cần thực hiện bài viết này?"><c:out value="${formTopic.summary}"/></textarea>
                    <small class="hint">Nội dung tóm tắt để người duyệt nắm nhanh tính thời sự và giá trị công chúng.</small>
                </div>

                <div class="field full">
                    <label for="angle">Góc tiếp cận (Angle) &amp; Điểm mới <span style="color:var(--danger)">*</span></label>
                    <textarea id="angle" name="angle" maxlength="1500" rows="3"
                              placeholder="Góc nhìn riêng của bạn, giải pháp đề xuất, câu hỏi cốt lõi mà bài viết sẽ trả lời?"><c:out value="${formTopic.angle}"/></textarea>
                    <small class="hint">Angle là điểm cốt lõi phân biệt đề tài này với các tin tức sự kiện thông thường.</small>
                </div>




                <div class="field">
                    <label for="category">
                        Chuyên mục <span style="color:var(--danger)">*</span>
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

                <div class="field">
                    <label for="deadline">Deadline dự kiến <span style="color:var(--danger)">*</span></label>
                    <input id="deadline" name="deadline" type="date" value="${formTopic.deadline}" min="${today}" required>
                    <small class="hint">Deadline tối thiểu từ hôm nay trở đi.</small>
                </div>
            </div>

            <div class="action-row" style="margin-top:24px;justify-content:flex-start">
                <button class="btn" type="submit" name="action" value="saveDraft">
                    💾 Lưu bản nháp
                </button>
                <button class="btn primary" type="submit" name="action" value="submit">
                    🚀 Nộp đề xuất thẩm định
                </button>
                <a class="btn" href="${pageContext.request.contextPath}/reporter/topics">
                    Hủy bỏ
                </a>
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

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
