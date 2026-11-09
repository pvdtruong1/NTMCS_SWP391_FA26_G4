<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.util.ArrayList,java.net.URLEncoder,java.text.SimpleDateFormat,model.AssetRequest" %>

<%!
private String esc(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
}

private String statusLabel(String s) {
    if ("PENDING".equals(s)) return "Chờ duyệt";
    if ("APPROVED".equals(s)) return "Đã duyệt";
    if ("REJECTED".equals(s)) return "Đã từ chối";
    if ("CHECKED_OUT".equals(s)) return "Đang bàn giao";
    if ("RETURNED".equals(s)) return "Đã nhận lại";
    return s != null ? s : "Không xác định";
}

private String statusCss(String s) {
    if (s == null) return "";
    return s.toUpperCase();
}

private int number(Object value) {
    return value instanceof Number ? ((Number) value).intValue() : 0;
}
%>

<%
List<AssetRequest> rows = (List<AssetRequest>) request.getAttribute("requests");
if (rows == null) {
    rows = new ArrayList<>();
}

String ctx = request.getContextPath();
String keyword = (String) request.getAttribute("keyword");
String status = (String) request.getAttribute("status");
String from = (String) request.getAttribute("fromDate");
String to = (String) request.getAttribute("toDate");
String message = request.getParameter("message");

if (keyword == null) keyword = "";
if (status == null) status = "ALL";
if (from == null) from = "";
if (to == null) to = "";

int total = number(request.getAttribute("totalRecords"));
int currentPageNum = Math.max(1, number(request.getAttribute("currentPage")));
int totalPages = Math.max(1, number(request.getAttribute("totalPages")));

int pending = number(request.getAttribute("pendingCount"));
int approved = number(request.getAttribute("approvedCount"));
int rejected = number(request.getAttribute("rejectedCount"));
int checked = number(request.getAttribute("checkedOutCount"));
int returned = number(request.getAttribute("returnedCount"));

String q = URLEncoder.encode(keyword, "UTF-8");
String s = URLEncoder.encode(status, "UTF-8");
String f = URLEncoder.encode(from, "UTF-8");
String t = URLEncoder.encode(to, "UTF-8");
Integer topicId = (Integer) request.getAttribute("topicId");
String baseQuery = "keyword=" + q + "&status=" + s + "&fromDate=" + f + "&toDate=" + t;
if (topicId != null && topicId > 0) {
    baseQuery += "&topicId=" + topicId;
}

SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
request.setAttribute("wf03Active", "requests");
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý yêu cầu sử dụng tài sản | NTMCS</title>
    <link rel="stylesheet" href="<%=ctx%>/css/style.css">
    <link rel="stylesheet" href="<%=ctx%>/assets/css/asset-management.css">
</head>

<body>
<div class="app">
    <jsp:include page="/WEB-INF/views/sidebar.jsp">
        <jsp:param name="active" value="asset-requests"/>
    </jsp:include>

    <section class="main">
        <jsp:include page="/WEB-INF/views/topbar.jsp">
            <jsp:param name="title" value="📋 Quản Lý Yêu Cầu Thiết Bị (WF-03)"/>
        </jsp:include>

        <main>
            <div class="head">
                <div>
                    <div class="eyebrow">WF-03 • ĐIỀU PHỐI VÀ XỬ LÝ YÊU CẦU</div>
                    <h1>Danh sách yêu cầu sử dụng tài sản</h1>
                    <p>Tiếp nhận, kiểm tra xung đột lịch, phê duyệt, bàn giao và thu hồi phương tiện, thiết bị tác nghiệp.</p>
                </div>
            </div>

            <% if ("approved".equals(message)) { %>
                <div class="notice success">✅ Đã phê duyệt yêu cầu sử dụng tài sản thành công.</div>
            <% } else if ("rejected".equals(message)) { %>
                <div class="notice success">✅ Đã từ chối yêu cầu sử dụng tài sản.</div>
            <% } else if ("checkedout".equals(message)) { %>
                <div class="notice success">✅ Xác nhận bàn giao tài sản thành công. Trạng thái tài sản chuyển sang 'Đang bàn giao'.</div>
            <% } else if ("returned".equals(message)) { %>
                <div class="notice success">✅ Xác nhận nhận lại tài sản thành công. Tài sản đã được đưa về trạng thái sẵn sàng hoặc bảo trì.</div>
            <% } else if ("conflict".equals(message)) { %>
                <div class="notice error">❌ Không thể phê duyệt: Khoảng thời gian yêu cầu bị xung đột trùng lịch với một yêu cầu đã được duyệt hoặc đang bàn giao!</div>
            <% } else if ("maintenance".equals(message)) { %>
                <div class="notice error">❌ Không thể phê duyệt: Tài sản đang trong quá trình bảo trì kỹ thuật.</div>
            <% } else if ("unavailable".equals(message)) { %>
                <div class="notice error">❌ Không thể bàn giao: Tài sản hiện không ở trạng thái sẵn sàng hoặc đang được bàn giao cho yêu cầu khác!</div>
            <% } else if ("notpending".equals(message)) { %>
                <div class="notice error">❌ Yêu cầu không còn ở trạng thái Chờ duyệt (PENDING). Thao tác bị hủy.</div>
            <% } else if ("notapproved".equals(message)) { %>
                <div class="notice error">❌ Chỉ yêu cầu đã được phê duyệt (APPROVED) mới có thể thực hiện bàn giao.</div>
            <% } else if ("notcheckedout".equals(message)) { %>
                <div class="notice error">❌ Chỉ yêu cầu đang bàn giao (CHECKED_OUT) mới có thể thực hiện nhận lại (thu hồi).</div>
            <% } else if ("notfound".equals(message)) { %>
                <div class="notice error">❌ Không tìm thấy thông tin yêu cầu.</div>
            <% } else if ("error".equals(message)) { %>
                <div class="notice error">❌ Đã xảy ra lỗi trong quá trình xử lý giao dịch. Vui lòng thử lại.</div>
            <% } %>

            <div class="stats" style="grid-template-columns: repeat(5, minmax(0, 1fr));">
                <div class="stat">
                    <span>Chờ duyệt</span>
                    <strong style="color: #b7791f;"><%=pending%></strong>
                    <small>Cần xử lý duyệt/từ chối</small>
                </div>

                <div class="stat">
                    <span>Đã duyệt</span>
                    <strong style="color: #2b6cb0;"><%=approved%></strong>
                    <small>Chờ bàn giao thiết bị</small>
                </div>

                <div class="stat">
                    <span>Đang bàn giao</span>
                    <strong style="color: #3182ce;"><%=checked%></strong>
                    <small>Đang tác nghiệp thực tế</small>
                </div>

                <div class="stat">
                    <span>Đã nhận lại</span>
                    <strong style="color: #2f855a;"><%=returned%></strong>
                    <small>Đã hoàn tất chu trình</small>
                </div>

                <div class="stat">
                    <span>Đã từ chối</span>
                    <strong style="color: #c53030;"><%=rejected%></strong>
                    <small>Không được cấp phát</small>
                </div>
            </div>

            <section class="card">
                <div class="card-head">
                    <div>
                        <h2>Tìm kiếm và Lọc yêu cầu</h2>
                        <p>Lọc theo mã yêu cầu, đề tài, tài sản, phóng viên hoặc khoảng thời gian mượn.</p>
                    </div>
                    <span class="count"><%=total%> yêu cầu</span>
                </div>

                <% if (topicId != null && topicId > 0) { %>
                    <div style="background:#eff6ff; border:1px solid #bfdbfe; color:#1e40af; padding:10px 16px; border-radius:8px; margin-bottom:16px; font-size:13px; display:flex; justify-content:space-between; align-items:center;">
                        <span>🔍 Đang lọc danh sách yêu cầu tài sản cho Đề tài <b>#<%=topicId%></b></span>
                        <a href="<%=ctx%>/asset-requests" class="btn sm" style="background:#fff; color:#1e40af; border:1px solid #bfdbfe; text-decoration:none; padding:4px 10px; font-size:12px;">✕ Xóa bộ lọc đề tài</a>
                    </div>
                <% } %>

                <form class="filters" method="get" action="<%=ctx%>/asset-requests">
                    <% if (topicId != null && topicId > 0) { %>
                        <input type="hidden" name="topicId" value="<%=topicId%>">
                    <% } %>
                    <input type="text"
                           name="keyword"
                           value="<%=esc(keyword)%>"
                           placeholder="Mã yêu cầu, đề tài, tên/mã tài sản, người gửi...">

                    <select name="status">
                        <option value="ALL" <%="ALL".equalsIgnoreCase(status) ? "selected" : ""%>>-- Tất cả trạng thái --</option>
                        <option value="PENDING" <%="PENDING".equalsIgnoreCase(status) ? "selected" : ""%>>Chờ duyệt (PENDING)</option>
                        <option value="APPROVED" <%="APPROVED".equalsIgnoreCase(status) ? "selected" : ""%>>Đã duyệt (APPROVED)</option>
                        <option value="CHECKED_OUT" <%="CHECKED_OUT".equalsIgnoreCase(status) ? "selected" : ""%>>Đang bàn giao (CHECKED_OUT)</option>
                        <option value="RETURNED" <%="RETURNED".equalsIgnoreCase(status) ? "selected" : ""%>>Đã nhận lại (RETURNED)</option>
                        <option value="REJECTED" <%="REJECTED".equalsIgnoreCase(status) ? "selected" : ""%>>Đã từ chối (REJECTED)</option>
                    </select>

                    <div style="display: flex; gap: 6px; align-items: center;">
                        <input type="date"
                               name="fromDate"
                               value="<%=esc(from)%>"
                               title="Từ ngày"
                               style="min-width: 135px;">
                        <span>đến</span>
                        <input type="date"
                               name="toDate"
                               value="<%=esc(to)%>"
                               title="Đến ngày"
                               style="min-width: 135px;">
                    </div>

                    <button class="btn primary" type="submit">🔍 Lọc dữ liệu</button>
                    <a class="btn" href="<%=ctx%>/asset-requests">🔄 Đặt lại</a>
                </form>

                <div class="table-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th style="width: 100px;">Mã phiếu</th>
                                <th>Đề tài báo chí</th>
                                <th>Tài sản yêu cầu</th>
                                <th>Phóng viên yêu cầu</th>
                                <th>Thời gian bắt đầu</th>
                                <th>Thời gian kết thúc</th>
                                <th style="width: 130px;">Trạng thái</th>
                                <th style="width: 180px; text-align: center;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if (rows.isEmpty()) { %>
                                <tr>
                                    <td colspan="8" class="empty">
                                        <b>Không tìm thấy yêu cầu tài sản nào phù hợp.</b>
                                        <p>Vui lòng điều chỉnh lại từ khóa, trạng thái hoặc khoảng thời gian tìm kiếm.</p>
                                    </td>
                                </tr>
                            <% } else {
                                for (AssetRequest x : rows) {
                            %>
                                <tr>
                                    <td>
                                        <strong>#AR-<%=x.getRequestId()%></strong>
                                    </td>
                                    <td>
                                        <b><%=esc(x.getTopicTitle())%></b>
                                        <small class="sub"><%=esc(x.getTopicCode())%></small>
                                    </td>
                                    <td>
                                        <b><%=esc(x.getAssetName())%></b>
                                        <small class="sub"><%=esc(x.getAssetCode())%></small>
                                    </td>
                                    <td>
                                        <b><%=esc(x.getRequesterName())%></b>
                                        <small class="sub"><%=esc(x.getRequesterEmail())%></small>
                                    </td>
                                    <td>
                                        <%=x.getStartTime() != null ? df.format(x.getStartTime()) : "-"%>
                                    </td>
                                    <td>
                                        <%=x.getEndTime() != null ? df.format(x.getEndTime()) : "-"%>
                                    </td>
                                    <td>
                                        <span class="request-status <%=statusCss(x.getStatus())%>">
                                            <%=statusLabel(x.getStatus())%>
                                        </span>
                                    </td>
                                    <td>
                                        <div class="action-buttons" style="justify-content: center;">
                                            <a class="btn small primary"
                                               href="<%=ctx%>/asset-requests?action=detail&id=<%=x.getRequestId()%>"
                                               title="Xem chi tiết & xử lý nghiệp vụ">
                                                👁 Xử lý
                                            </a>
                                            <a class="btn small"
                                               href="<%=ctx%>/asset-requests?action=schedule&id=<%=x.getRequestId()%>"
                                               title="Kiểm tra lịch trùng">
                                                📅 Lịch
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            <%  }
                               }
                            %>
                        </tbody>
                    </table>
                </div>

                <% if (totalPages > 1) { %>
                    <div class="pagination">
                        <% if (currentPageNum > 1) { %>
                            <a href="<%=ctx%>/asset-requests?page=<%=currentPageNum - 1%>&<%=baseQuery%>">« Trước</a>
                        <% } %>

                        <% for (int p = 1; p <= totalPages; p++) { %>
                            <% if (p == currentPageNum) { %>
                                <span class="active"><%=p%></span>
                            <% } else if (p == 1 || p == totalPages || Math.abs(p - currentPageNum) <= 2) { %>
                                <a href="<%=ctx%>/asset-requests?page=<%=p%>&<%=baseQuery%>"><%=p%></a>
                            <% } else if (p == currentPageNum - 3 || p == currentPageNum + 3) { %>
                                <span style="border:none;">...</span>
                            <% } %>
                        <% } %>

                        <% if (currentPageNum < totalPages) { %>
                            <a href="<%=ctx%>/asset-requests?page=<%=currentPageNum + 1%>&<%=baseQuery%>">Sau »</a>
                        <% } %>
                    </div>
                <% } %>
            </section>
        </main>
    </section>
</div>

<script src="<%=ctx%>/assets/js/asset-management.js"></script>
</body>
</html>
