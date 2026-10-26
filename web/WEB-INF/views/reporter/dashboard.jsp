<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/views/common/header.jsp">
  <jsp:param name="pageTitle" value="Reporter Dashboard — NTMCS" />
</jsp:include>
<c:set var="activeNav" value="dashboard" scope="request" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/topbar.jsp" />

<div class="page-head">
  <div>
    <div class="eyebrow">WORKFLOW 1 &middot; ITERATION 1</div>
    <h1>Reporter Dashboard</h1>
    <p>Tổng quan tình trạng các đề xuất đề tài của phóng viên và tiến độ thẩm định.</p>
  </div>
  <div class="page-actions">
    <a class="btn" href="${pageContext.request.contextPath}/reporter/duplicate-search">
      <i>🔍</i> <span>Tra cứu trùng lặp</span>
    </a>
    <a class="btn primary" href="${pageContext.request.contextPath}/reporter/compose">
      <i>＋</i> <span>Soạn đề tài mới</span>
    </a>
  </div>
</div>

<section class="stats">
  <div class="stat">
    <div class="stat-icon">◆</div>
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
      <a class="link" href="${pageContext.request.contextPath}/reporter/topics">Tất cả đề tài &rarr;</a>
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
      <a class="link" href="${pageContext.request.contextPath}/reporter/topics">Xem danh sách &rarr;</a>
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
                <a class="row-link" href="${pageContext.request.contextPath}/reporter/detail?id=${t.topicId}">
                  <c:out value="${t.title}"/>
                </a>
                <small><c:out value="${t.category}"/> &middot; Hạn: <c:out value="${t.deadline}"/></small>
              </td>
              <td>
                <span class="priority-tag ${t.priority}"><c:out value="${t.priority}"/></span>
              </td>
              <td>
                <span class="badge b-${t.status.toLowerCase()}"><c:out value="${t.status}"/></span>
              </td>
              <td>
                <a class="btn sm" href="${pageContext.request.contextPath}/reporter/detail?id=${t.topicId}">Xem</a>
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
    <h2>Quyền phóng viên (WF1)</h2>
    <div class="permission-box">
      <span>✓ Soạn đề xuất đề tài mới</span>
      <span>✓ Lưu bản nháp (DRAFT)</span>
      <span>✓ Nộp đề xuất (SUBMITTED)</span>
      <span>✓ Tra cứu trùng lặp 30 ngày</span>
      <span>✓ Sửa đề tài bị yêu cầu bổ sung</span>
      <span>✓ Theo dõi quy trình 7 bước</span>
    </div>
  </section>

  <section class="card">
    <h2>Phạm vi bàn giao Iteration 1</h2>
    <p style="color:var(--muted);font-size:13px;line-height:1.6;margin:0 0 12px">
      Iteration 1 tập trung hoàn thiện toàn bộ luồng tác nghiệp của Phóng viên (Workflow 1):
    </p>
    <div style="font-size:12.5px;display:grid;gap:6px">
      <div><strong>SCR-1.1:</strong> Reporter Dashboard</div>
      <div><strong>SCR-1.2:</strong> Personal Topic List</div>
      <div><strong>SCR-1.3:</strong> Duplicate Search Tool</div>
      <div><strong>SCR-1.4:</strong> Topic Pitch Composition Form</div>
      <div><strong>SCR-1.6:</strong> Topic Detail &amp; Progress</div>
    </div>
  </section>

  <section class="card">
    <h2>Thao tác nhanh</h2>
    <p style="color:var(--muted);font-size:13px;margin:0 0 14px">
      Đang đăng nhập bằng tài khoản <strong>Mai Khánh Hùng</strong> (hung@ntmcs.local).
    </p>
    <div style="display:grid;gap:8px">
      <a class="btn primary" href="${pageContext.request.contextPath}/reporter/compose">+ Soạn đề xuất đề tài</a>
      <a class="btn" href="${pageContext.request.contextPath}/reporter/duplicate-search">Tra cứu đề tài trùng lặp</a>
      <a class="btn danger" href="${pageContext.request.contextPath}/reporter/reset" onclick="return confirm('Đặt lại toàn bộ dữ liệu mẫu?')">Reset về Seed DB</a>
    </div>
  </section>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
