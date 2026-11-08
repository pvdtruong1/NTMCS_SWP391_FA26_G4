<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.AssetRequest,java.util.List,java.util.ArrayList,java.text.SimpleDateFormat" %>

<%!
private String esc(String s) {
    if (s == null || s.isEmpty()) return "-";
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
%>

<%
AssetRequest item = (AssetRequest) request.getAttribute("assetRequest");

if (item == null) {
    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy yêu cầu tài sản.");
    return;
}

List<AssetRequest> conflicts = (List<AssetRequest>) request.getAttribute("conflicts");
if (conflicts == null) {
    conflicts = new ArrayList<>();
}

boolean hasBlockingConflict = Boolean.TRUE.equals(request.getAttribute("hasConflict"));

String ctx = request.getContextPath();
SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

request.setAttribute("wf03Active", "requests");
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Kiểm tra trùng lịch yêu cầu #AR-<%=item.getRequestId()%> | NTMCS</title>
    <link rel="stylesheet" href="<%=ctx%>/css/style.css">
    <link rel="stylesheet" href="<%=ctx%>/assets/css/asset-management.css">
</head>

<body>
<div class="app">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>

    <section class="main">
        <jsp:include page="/WEB-INF/views/topbar.jsp">
            <jsp:param name="title" value="<%=\"📅 Kiểm soát Lịch Thiết Bị #AR-\" + item.getRequestId()%>"/>
        </jsp:include>

        <main>
            <div class="head">
                <div>
                    <div class="eyebrow">WF-03 • KIỂM SOÁT XUNG ĐỘT LỊCH (CONFLICT RESOLUTION)</div>
                    <h1>Kiểm tra trùng lịch sử dụng tài sản</h1>
                    <p>So sánh khoảng thời gian đăng ký với toàn bộ các yêu cầu khác của cùng một tài sản.</p>
                </div>

                <div class="head-actions">
                    <a class="btn" href="<%=ctx%>/asset-requests?action=detail&id=<%=item.getRequestId()%>">
                        ⬅ Chi tiết yêu cầu
                    </a>
                    <a class="btn" href="<%=ctx%>/asset-requests">
                        📋 Danh sách yêu cầu
                    </a>
                </div>
            </div>

            <section class="card">
                <div class="card-head">
                    <div>
                        <h2>Thông tin yêu cầu đang kiểm tra</h2>
                        <p>Phiếu: <b>#AR-<%=item.getRequestId()%></b> • Đề tài: <%=esc(item.getTopicTitle())%></p>
                    </div>
                    <span class="request-status <%=statusCss(item.getStatus())%>">
                        <%=statusLabel(item.getStatus())%>
                    </span>
                </div>

                <div class="detail-grid">
                    <div class="detail-item">
                        <label>Mã phiếu yêu cầu</label>
                        <b>#AR-<%=item.getRequestId()%></b>
                    </div>

                    <div class="detail-item">
                        <label>Tài sản kiểm tra</label>
                        <b><%=esc(item.getAssetName())%> (<%=esc(item.getAssetCode())%>)</b>
                    </div>

                    <div class="detail-item">
                        <label>Đề tài tác nghiệp</label>
                        <b><%=esc(item.getTopicCode())%> - <%=esc(item.getTopicTitle())%></b>
                    </div>

                    <div class="detail-item">
                        <label>Phóng viên đăng ký</label>
                        <b><%=esc(item.getRequesterName())%> (<%=esc(item.getRequesterEmail())%>)</b>
                    </div>

                    <div class="detail-item">
                        <label>Thời điểm bắt đầu</label>
                        <b style="color: #2b6cb0;"><%=item.getStartTime() != null ? df.format(item.getStartTime()) : "-"%></b>
                    </div>

                    <div class="detail-item">
                        <label>Thời điểm kết thúc</label>
                        <b style="color: #2b6cb0;"><%=item.getEndTime() != null ? df.format(item.getEndTime()) : "-"%></b>
                    </div>
                </div>

                <div style="margin-top: 24px;">
                    <% if (hasBlockingConflict) { %>
                        <div class="notice error">
                            <b>⚠️ CẢNH BÁO XUNG ĐỘT NGHIÊM TRỌNG:</b>
                            <p style="margin: 4px 0 0;">
                                Phát hiện có yêu cầu khác ở trạng thái <b>ĐÃ DUYỆT (APPROVED)</b> hoặc <b>ĐANG BÀN GIAO (CHECKED_OUT)</b> trong khoảng thời gian này.
                                Hệ thống sẽ <b>CHẶN PHÊ DUYỆT</b> cho đến khi giải quyết xung đột lịch.
                            </p>
                        </div>
                    <% } else if (!conflicts.isEmpty()) { %>
                        <div class="notice" style="background: #fffbeb; color: #b45309; border: 1px solid #fde68a;">
                            <b>ℹ️ LƯU Ý LỊCH TRÙNG HÀNG ĐỢI:</b>
                            <p style="margin: 4px 0 0;">
                                Có <%=conflicts.size()%> yêu cầu khác cùng khoảng thời gian đang ở trạng thái <b>Chờ duyệt (PENDING)</b>.
                                Các yêu cầu chờ duyệt không chặn lẫn nhau, nhưng người điều phối cần cân nhắc phê duyệt yêu cầu ưu tiên trước.
                            </p>
                        </div>
                    <% } else { %>
                        <div class="notice success">
                            <b>✅ LỊCH SỬ DỤNG HOÀN TOÀN TRỐNG:</b>
                            <p style="margin: 4px 0 0;">
                                Không phát hiện bất kỳ yêu cầu nào khác của tài sản này giao nhau với khoảng thời gian đã chọn. Đủ điều kiện phê duyệt an toàn.
                            </p>
                        </div>
                    <% } %>
                </div>

                <h3 style="margin: 24px 0 12px; color: #2d3748;">
                    Danh sách các yêu cầu có thời gian giao nhau (<%=conflicts.size()%>)
                </h3>

                <div class="table-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th style="width: 100px;">Mã phiếu</th>
                                <th>Đề tài báo chí</th>
                                <th>Phóng viên</th>
                                <th>Thời gian bắt đầu</th>
                                <th>Thời gian kết thúc</th>
                                <th style="width: 130px;">Trạng thái</th>
                                <th style="width: 100px; text-align: center;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if (conflicts.isEmpty()) { %>
                                <tr>
                                    <td colspan="7" class="empty">
                                        <b>Không có yêu cầu nào trùng lịch.</b>
                                        <p>Tài sản hoàn toàn rảnh trong khung thời gian đăng ký.</p>
                                    </td>
                                </tr>
                            <% } else {
                                for (AssetRequest c : conflicts) {
                            %>
                                <tr>
                                    <td>
                                        <strong>#AR-<%=c.getRequestId()%></strong>
                                    </td>
                                    <td>
                                        <b><%=esc(c.getTopicTitle())%></b>
                                        <small class="sub"><%=esc(c.getTopicCode())%></small>
                                    </td>
                                    <td>
                                        <%=esc(c.getRequesterName())%>
                                    </td>
                                    <td>
                                        <%=c.getStartTime() != null ? df.format(c.getStartTime()) : "-"%>
                                    </td>
                                    <td>
                                        <%=c.getEndTime() != null ? df.format(c.getEndTime()) : "-"%>
                                    </td>
                                    <td>
                                        <span class="request-status <%=statusCss(c.getStatus())%>">
                                            <%=statusLabel(c.getStatus())%>
                                        </span>
                                    </td>
                                    <td style="text-align: center;">
                                        <a class="btn small"
                                           href="<%=ctx%>/asset-requests?action=detail&id=<%=c.getRequestId()%>">
                                            Xem
                                        </a>
                                    </td>
                                </tr>
                            <%  }
                               }
                            %>
                        </tbody>
                    </table>
                </div>

                <div class="actions" style="margin-top: 24px;">
                    <a class="btn" href="<%=ctx%>/asset-requests?action=detail&id=<%=item.getRequestId()%>">
                        ⬅ Quay lại chi tiết yêu cầu
                    </a>
                    <a class="btn primary" href="<%=ctx%>/asset-requests">
                        📋 Về danh sách yêu cầu
                    </a>
                </div>
            </section>
        </main>
    </section>
</div>

<script src="<%=ctx%>/assets/js/asset-management.js"></script>
</body>
</html>
