<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/views/common/header.jsp">
  <jsp:param name="pageTitle" value="${topic != null ? topic.title : 'Chi tiết đề tài'} — NTMCS" />
</jsp:include>
<c:set var="activeNav" value="topics" scope="request" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/topbar.jsp" />

<c:choose>
  <c:when test="${empty topic}">
    <div class="page-head">
      <div>
        <div class="eyebrow">SCR-1.6 &middot; ERROR</div>
        <h1>Không tìm thấy đề tài</h1>
        <p>Đề tài bạn yêu cầu không tồn tại hoặc bạn không có quyền truy cập.</p>
      </div>
      <div class="page-actions">
        <a class="btn" href="${pageContext.request.contextPath}/reporter/topics">&larr; Quay lại danh sách</a>
      </div>
    </div>
  </c:when>

  <c:otherwise>
    <div class="page-head">
      <div>
        <div class="eyebrow">SCR-1.6 &middot; CHI TIẾT ĐỀ TÀI</div>
        <h1><c:out value="${topic.title}"/></h1>
        <p>
          Mã định danh: <strong>${topic.topicCode != null ? topic.topicCode : ('#' += topic.topicId)}</strong>
          &middot; Khởi tạo ngày <c:out value="${topic.createdDate}"/>
        </p>
      </div>
      <div class="page-actions">
        <a class="btn" href="${pageContext.request.contextPath}/reporter/topics">&larr; Danh sách</a>
        <c:if test="${topic.status eq 'DRAFT' or topic.status eq 'REVISION_REQUESTED'}">
          <a class="btn primary" href="${pageContext.request.contextPath}/reporter/compose?id=${topic.topicId}">
            ✍️ Chỉnh sửa đề tài
          </a>
        </c:if>
        <c:if test="${topic.status eq 'DRAFT'}">
          <a class="btn success" href="${pageContext.request.contextPath}/reporter/quick-submit?id=${topic.topicId}" onclick="return confirm('Nộp đề xuất này lên ban biên tập?')">
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
              <b><c:out value="${topic.reporterName != null ? topic.reporterName : 'Mai Khánh Hùng'}"/></b>
            </div>
            <div>
              <span>Chuyên mục</span>
              <b><c:out value="${topic.category}"/></b>
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
            <div class="alert danger" style="margin-top:16px">
              <strong>Phản hồi từ Ban Biên tập:</strong>
              <p style="margin:4px 0 0"><c:out value="${topic.rejectionReason}"/></p>
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
            <a class="btn" href="${pageContext.request.contextPath}/reporter/topics">&larr; Về danh sách đề tài</a>
            <c:if test="${topic.status eq 'DRAFT' or topic.status eq 'REVISION_REQUESTED'}">
              <a class="btn primary" href="${pageContext.request.contextPath}/reporter/compose?id=${topic.topicId}">
                Chỉnh sửa nội dung đề xuất
              </a>
            </c:if>
            <c:if test="${topic.status eq 'DRAFT'}">
              <a class="btn success" href="${pageContext.request.contextPath}/reporter/quick-submit?id=${topic.topicId}">
                Nộp thẩm định
              </a>
              <a class="btn danger" href="${pageContext.request.contextPath}/reporter/delete?id=${topic.topicId}" data-confirm-delete>
                Xóa bản nháp
              </a>
            </c:if>
          </div>
        </section>

        <!-- Preview placeholders for Iteration 2 modules -->
        <div class="grid2">
          <section class="card">
            <div class="card-title">
              <h2>Ekip đề tài (Topic_Member)</h2>
              <span class="demo">Iter 2</span>
            </div>
            <p style="color:var(--muted);font-size:13px;margin:0">
              Phân công nhân sự (quay phim, ảnh, trợ lý) sẽ được kích hoạt sau khi đề tài được phê duyệt tại Iteration 2.
            </p>
          </section>

          <section class="card">
            <div class="card-title">
              <h2>Dự toán kinh phí (Estimate)</h2>
              <span class="demo">Iter 2</span>
            </div>
            <p style="color:var(--muted);font-size:13px;margin:0">
              Bảng lập dự toán chi phí tác nghiệp thực địa sẽ hoàn thiện ở Iteration 2.
            </p>
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

        <section class="card">
          <h2>Hồ sơ quản lý</h2>
          <div style="font-size:13px;display:grid;gap:8px">
            <div><strong>Phòng ban:</strong> <c:out value="${topic.desk != null ? topic.desk : 'Ban Thời sự'}"/></div>
            <div><strong>Email liên hệ:</strong> <c:out value="${topic.reporterEmail != null ? topic.reporterEmail : 'hung@ntmcs.local'}"/></div>
            <div><strong>Ngày tạo:</strong> <c:out value="${topic.createdDate}"/></div>
            <div><strong>Trạng thái:</strong> <span class="badge b-${topic.status.toLowerCase()}"><c:out value="${topic.status}"/></span></div>
          </div>
        </section>
      </aside>
    </div>
  </c:otherwise>
</c:choose>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
