<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.util.ArrayList,java.text.SimpleDateFormat,model.AssetRequest" %>

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
    if ("CHECKED_OUT".equals(s)) return "Đang bàn giao";
    if ("RETURNED".equals(s)) return "Đã nhận lại (Thu hồi)";
    return s != null ? s : "Không xác định";
}

private String statusCss(String s) {
    if (s == null) return "";
    return s.toUpperCase();
}
%>

<%
List<AssetRequest> history = (List<AssetRequest>) request.getAttribute("history");
if (history == null) {
    history = new ArrayList<>();
}

Object assetIdObject = request.getAttribute("assetId");
int assetId = assetIdObject instanceof Number ? ((Number) assetIdObject).intValue() : 0;

String ctx = request.getContextPath();
SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

request.setAttribute("wf03Active", "assets");
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Lịch sử sử dụng tài sản #<%=assetId%> | NTMCS</title>
    <link rel="stylesheet" href="<%=ctx%>/css/style.css">
    <link rel="stylesheet" href="<%=ctx%>/assets/css/asset-management.css">
</head>

<body>
<div class="app">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>

    <section class="main">
        <jsp:include page="/WEB-INF/views/topbar.jsp">
            <jsp:param name="title" value="<%=\"📜 Nhật ký Luân chuyển Thiết bị #\" + assetId%>"/>
        </jsp:include>

        <main>
            <div class="head">
                <div>
                    <div class="eyebrow">WF-03 • NHẬT KÝ ĐIỀU PHỐI TÀI SẢN</div>
                    <h1>Lịch sử sử dụng tài sản #<%=assetId%></h1>
                    <p>Theo dõi các lần tài sản được bàn giao và nhận lại phục vụ các đề tài tác nghiệp báo chí.</p>
                </div>

                <div class="head-actions">
                    <a class="btn" href="<%=ctx%>/assets?action=detail&id=<%=assetId%>">
                        ⬅ Chi tiết tài sản
                    </a>
                    <a class="btn" href="<%=ctx%>/assets">
                        📦 Danh sách tài sản
                    </a>
                </div>
            </div>

            <section class="card">
                <div class="card-head">
                    <div>
                        <h2>Lịch sử bàn giao và nhận lại (Thu hồi)</h2>
                        <p>Các yêu cầu ở trạng thái <b>Đang bàn giao (CHECKED_OUT)</b> hoặc <b>Đã nhận lại (RETURNED)</b>.</p>
                    </div>
                    <span class="count"><%=history.size()%> lượt sử dụng</span>
                </div>

                <div class="table-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th style="width: 110px;">Mã phiếu</th>
                                <th>Đề tài báo chí</th>
                                <th>Phóng viên sử dụng</th>
                                <th>Tên & Mã tài sản</th>
                                <th>Thời gian bắt đầu</th>
                                <th>Thời gian kết thúc</th>
                                <th style="width: 140px;">Trạng thái</th>
                                <th style="width: 100px; text-align: center;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if (history.isEmpty()) { %>
                                <tr>
                                    <td colspan="8" class="empty">
                                        <b>Chưa có lịch sử bàn giao hoặc nhận lại nào cho tài sản này.</b>
                                        <p>Tài sản chưa từng được xuất kho phục vụ tác nghiệp.</p>
                                    </td>
                                </tr>
                            <% } else {
                                for (AssetRequest x : history) {
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
                                        <b><%=esc(x.getRequesterName())%></b>
                                        <small class="sub"><%=esc(x.getRequesterEmail())%></small>
                                    </td>
                                    <td>
                                        <b><%=esc(x.getAssetName())%></b>
                                        <small class="sub"><%=esc(x.getAssetCode())%></small>
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
                                    <td style="text-align: center;">
                                        <a class="btn small"
                                           href="<%=ctx%>/asset-requests?action=detail&id=<%=x.getRequestId()%>">
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
                    <a class="btn" href="<%=ctx%>/assets?action=detail&id=<%=assetId%>">
                        ⬅ Quay lại chi tiết tài sản #<%=assetId%>
                    </a>
                    <a class="btn" href="<%=ctx%>/assets">
                        📦 Danh sách tài sản
                    </a>
                    <a class="btn primary" href="<%=ctx%>/asset-requests">
                        📋 Quản lý yêu cầu tài sản
                    </a>
                </div>
            </section>
        </main>
    </section>
</div>

<script src="<%=ctx%>/assets/js/asset-management.js"></script>
</body>
</html>
