<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/views/common/header.jsp">
  <jsp:param name="pageTitle" value="Tra cứu trùng lặp đề tài — NTMCS" />
</jsp:include>
<c:set var="activeNav" value="duplicate-search" scope="request" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/topbar.jsp" />

<div class="page-head">
  <div>
    <div class="eyebrow">SCR-1.3 &middot; WORKFLOW 1</div>
    <h1>Tra cứu trùng lặp đề tài</h1>
    <p>Kiểm tra tiêu đề, tóm tắt và góc tiếp cận với các đề tài trong 30 ngày gần đây để tránh trùng lặp nội dung.</p>
  </div>
  <div class="page-actions">
    <a class="btn primary" href="${pageContext.request.contextPath}/reporter/compose">
      <i>✍️</i> <span>Tiến hành soạn đề tài</span>
    </a>
  </div>
</div>

<div class="card">
  <form method="get" action="${pageContext.request.contextPath}/reporter/duplicate-search">
    <div class="field">
      <label for="keyword" style="font-size:13px;font-weight:700">Từ khóa sự kiện, vấn đề hoặc tiêu đề dự kiến</label>
      <div style="display:flex;gap:10px;margin-top:6px;flex-wrap:wrap">
        <input id="keyword" name="keyword" value="<c:out value='${keyword}'/>"
               placeholder="Ví dụ: giao thông xanh, ngập úng, trường nghề, trí tuệ nhân tạo, sách vùng cao..."
               style="flex:1;min-width:300px" required>
        <button class="btn primary" type="submit">
          🔍 Kiểm tra trùng lặp
        </button>
        <c:if test="${not empty keyword}">
          <a class="btn" href="${pageContext.request.contextPath}/reporter/duplicate-search">Xóa tìm kiếm</a>
        </c:if>
      </div>
      <small class="hint" style="margin-top:6px">
        Hệ thống rà soát tiêu đề, tóm tắt và góc tiếp cận của mọi đề tài trong tòa soạn khởi tạo trong 30 ngày gần nhất.
      </small>
    </div>

    <div style="margin-top:14px;display:flex;align-items:center;gap:8px;flex-wrap:wrap">
      <span style="font-size:12px;color:var(--muted)">Từ khóa gợi ý:</span>
      <a class="tag" href="${pageContext.request.contextPath}/reporter/duplicate-search?keyword=giao+thông+xanh">giao thông xanh</a>
      <a class="tag" href="${pageContext.request.contextPath}/reporter/duplicate-search?keyword=ngập+úng">ngập úng</a>
      <a class="tag" href="${pageContext.request.contextPath}/reporter/duplicate-search?keyword=trường+nghề">trường nghề</a>
      <a class="tag" href="${pageContext.request.contextPath}/reporter/duplicate-search?keyword=AI">trí tuệ nhân tạo (AI)</a>
      <a class="tag" href="${pageContext.request.contextPath}/reporter/duplicate-search?keyword=Hà+Giang">sách Hà Giang</a>
    </div>
  </form>
</div>

<c:if test="${not empty keyword}">
  <c:choose>
    <c:when test="${not empty matches}">
      <div class="alert" style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:10px">
        <div>
          <strong>Cảnh báo khả năng trùng lặp:</strong>
          Tìm thấy <strong>${matches.size()}</strong> đề tài có liên quan đến từ khóa “<c:out value="${keyword}"/>”.
          Hãy đối chiếu góc tiếp cận (Angle) để tạo sự khác biệt.
        </div>
        <a class="btn primary sm" href="${pageContext.request.contextPath}/reporter/compose">Vẫn tiếp tục tạo đề tài với Angle mới</a>
      </div>

      <div class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>Mã</th>
              <th>Tiêu đề đề tài &amp; Góc tiếp cận</th>
              <th>Chuyên mục</th>
              <th>Ngày tạo</th>
              <th>Deadline</th>
              <th>Trạng thái</th>
              <th>So sánh</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="t" items="${matches}">
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
                <td style="max-width:360px">
                  <a class="row-link" href="${pageContext.request.contextPath}/reporter/detail?id=${t.topicId}">
                    <strong><c:out value="${t.title}"/></strong>
                  </a>
                  <c:if test="${not empty t.angle}">
                    <small><span style="font-weight:600">Angle:</span> <c:out value="${t.angle}"/></small>
                  </c:if>
                </td>
                <td><c:out value="${t.category}"/></td>
                <td><c:out value="${t.createdDate}"/></td>
                <td><c:out value="${t.deadline}"/></td>
                <td>
                  <span class="badge b-${t.status.toLowerCase()}"><c:out value="${t.status}"/></span>
                </td>
                <td>
                  <a class="btn sm" href="${pageContext.request.contextPath}/reporter/detail?id=${t.topicId}">Xem chi tiết</a>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </c:when>

    <c:otherwise>
      <div class="alert success" style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:10px">
        <div>
          <strong>An toàn:</strong> Không phát hiện đề tài nào trùng lặp với từ khóa “<c:out value="${keyword}"/>” trong 30 ngày qua!
        </div>
        <a class="btn primary sm" href="${pageContext.request.contextPath}/reporter/compose?title=<c:out value='${keyword}'/>">
          Soạn đề xuất với tiêu đề này ngay &rarr;
        </a>
      </div>
    </c:otherwise>
  </c:choose>
</c:if>

<div class="grid2" style="margin-top:20px">
  <section class="card">
    <h2>Lời khuyên khi phát hiện đề tài tương tự</h2>
    <ol style="margin:0;padding-left:20px;color:var(--text);font-size:13.5px;line-height:1.7">
      <li><strong>Đổi góc tiếp cận (Angle):</strong> Tìm lát cắt mới (ví dụ: chuyển từ phản ánh thực trạng sang giải pháp hoặc câu chuyện cá nhân).</li>
      <li><strong>Khai thác nhân vật khác:</strong> Phỏng vấn nhóm người chịu tác động trực tiếp thay vì chỉ dùng dữ liệu chung.</li>
      <li><strong>Thay đổi thể loại:</strong> Có thể chuyển từ tin nhanh sự kiện sang bài phỏng vấn chuyên sâu hoặc phóng sự đa phương tiện.</li>
    </ol>
  </section>

  <section class="card">
    <h2>Nguyên tắc chống trùng lặp tại tòa soạn</h2>
    <p style="color:var(--muted);font-size:13px;line-height:1.6;margin:0 0 10px">
      Hệ thống áp dụng cửa sổ rà soát 30 ngày cho các đề tài cùng chuyên mục. Biên tập viên phòng ban sẽ căn cứ kết quả này để phê duyệt hoặc yêu cầu chỉnh sửa đề xuất của phóng viên.
    </p>
    <a class="btn primary full" href="${pageContext.request.contextPath}/reporter/compose">+ Soạn thảo đề xuất mới</a>
  </section>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
