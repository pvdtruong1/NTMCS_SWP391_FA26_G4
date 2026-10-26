<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/views/common/header.jsp">
  <jsp:param name="pageTitle" value="Đề tài của tôi — NTMCS" />
</jsp:include>
<c:set var="activeNav" value="topics" scope="request" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/topbar.jsp" />

<div class="page-head">
  <div>
    <div class="eyebrow">SCR-1.2 &middot; WORKFLOW 1</div>
    <h1>Đề tài của tôi</h1>
    <p>Quản lý toàn bộ danh sách các đề xuất đề tài do bạn tạo, theo dõi tình trạng phê duyệt.</p>
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

<form class="filterbar" method="get" action="${pageContext.request.contextPath}/reporter/topics">
  <input name="keyword" value="<c:out value='${keyword}'/>" placeholder="⌕ Tìm theo mã, tiêu đề, góc tiếp cận..." style="flex:1;max-width:380px">

  <select name="status">
    <option value="">Tất cả trạng thái</option>
    <option value="DRAFT" ${status eq 'DRAFT' ? 'selected' : ''}>Bản nháp (DRAFT)</option>
    <option value="SUBMITTED" ${status eq 'SUBMITTED' ? 'selected' : ''}>Đã nộp duyệt (SUBMITTED)</option>
    <option value="REVISION_REQUESTED" ${status eq 'REVISION_REQUESTED' ? 'selected' : ''}>Yêu cầu chỉnh sửa (REVISION_REQUESTED)</option>
    <option value="APPROVED" ${status eq 'APPROVED' ? 'selected' : ''}>Đã phê duyệt (APPROVED)</option>
    <option value="IN_PROGRESS" ${status eq 'IN_PROGRESS' ? 'selected' : ''}>Đang tác nghiệp (IN_PROGRESS)</option>
    <option value="ACCEPTED_READY_FOR_CMS" ${status eq 'ACCEPTED_READY_FOR_CMS' ? 'selected' : ''}>Sẵn sàng CMS (ACCEPTED)</option>
    <option value="REJECTED" ${status eq 'REJECTED' ? 'selected' : ''}>Bị từ chối (REJECTED)</option>
  </select>

  <button class="btn" type="submit">Lọc</button>
  <a class="btn" href="${pageContext.request.contextPath}/reporter/topics">Xóa lọc</a>

  <div style="margin-left:auto;color:var(--muted);font-size:12.5px;align-self:center">
    Hiển thị <strong>${topics.size()}</strong> đề tài
  </div>
</form>

<div class="table-scroll">
  <table>
    <thead>
      <tr>
        <th>Mã</th>
        <th>Tiêu đề đề tài &amp; Góc tiếp cận</th>
        <th>Chuyên mục</th>
        <th>Thể loại</th>
        <th>Ưu tiên</th>
        <th>Deadline</th>
        <th>Trạng thái</th>
        <th style="text-align:right">Thao tác</th>
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
                <span style="color:var(--muted);font-weight:600">#${t.topicId}</span>
              </c:otherwise>
            </c:choose>
          </td>

          <td style="max-width:340px">
            <a class="row-link" href="${pageContext.request.contextPath}/reporter/detail?id=${t.topicId}">
              <strong><c:out value="${t.title}"/></strong>
            </a>
            <c:if test="${not empty t.angle}">
              <small style="color:var(--muted);margin-top:2px">
                <span style="font-weight:600">Angle:</span> <c:out value="${t.angle}"/>
              </small>
            </c:if>
            <c:if test="${not empty t.rejectionReason}">
              <small style="color:var(--danger);font-weight:600;margin-top:4px">
                &ldquor;<c:out value="${t.rejectionReason}"/>&rdquo;
              </small>
            </c:if>
          </td>

          <td>
            <strong><c:out value="${t.category}"/></strong>
            <small><c:out value="${t.desk != null ? t.desk : 'Thời sự'}"/></small>
          </td>

          <td>
            <c:out value="${t.genre != null ? t.genre : 'Tin nhanh'}"/>
          </td>

          <td>
            <span class="priority-tag ${t.priority}"><c:out value="${t.priority}"/></span>
          </td>

          <td>
            <c:out value="${t.deadline}"/>
          </td>

          <td>
            <span class="badge b-${t.status.toLowerCase()}"><c:out value="${t.status}"/></span>
          </td>

          <td style="text-align:right">
            <div style="display:inline-flex;gap:6px;justify-content:flex-end">
              <a class="btn sm" href="${pageContext.request.contextPath}/reporter/detail?id=${t.topicId}">Mở</a>

              <c:if test="${t.status eq 'DRAFT' or t.status eq 'REVISION_REQUESTED'}">
                <a class="btn sm primary" href="${pageContext.request.contextPath}/reporter/compose?id=${t.topicId}">Sửa</a>
              </c:if>

              <c:if test="${t.status eq 'DRAFT'}">
                <a class="btn sm success" href="${pageContext.request.contextPath}/reporter/quick-submit?id=${t.topicId}" onclick="return confirm('Nộp đề xuất đề tài này lên ban biên tập?')">Nộp</a>
                <a class="btn sm danger" href="${pageContext.request.contextPath}/reporter/delete?id=${t.topicId}" data-confirm-delete>Xóa</a>
              </c:if>
            </div>
          </td>
        </tr>
      </c:forEach>

      <c:if test="${empty topics}">
        <tr>
          <td colspan="8" class="empty">
            <div style="padding:20px">
              <p style="margin:0 0 10px;font-size:15px">Không tìm thấy đề tài nào phù hợp với điều kiện tìm kiếm.</p>
              <a class="btn primary" href="${pageContext.request.contextPath}/reporter/compose">Soạn đề tài mới ngay</a>
            </div>
          </td>
        </tr>
      </c:if>
    </tbody>
  </table>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
