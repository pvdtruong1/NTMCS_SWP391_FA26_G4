<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.AssetRequest,java.text.SimpleDateFormat" %>

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
    if ("PENDING".equals(s)) return "Chờ duyệt (PENDING)";
    if ("APPROVED".equals(s)) return "Đã duyệt (APPROVED)";
    if ("REJECTED".equals(s)) return "Đã từ chối (REJECTED)";
    if ("CHECKED_OUT".equals(s)) return "Đang bàn giao (CHECKED_OUT)";
    if ("RETURNED".equals(s)) return "Đã nhận lại (RETURNED)";
    return s != null ? s : "Không xác định";
}

private String assetStatusLabel(String s) {
    if ("AVAILABLE".equals(s)) return "Sẵn sàng";
    if ("CHECKED_OUT".equals(s)) return "Đang bàn giao";
    if ("MAINTENANCE".equals(s)) return "Đang bảo trì";
    return s != null ? s : "Không xác định";
}

private String statusCss(String s) {
    if (s == null) return "";
    return s.toUpperCase();
}
%>

<%
AssetRequest x = (AssetRequest) request.getAttribute("assetRequest");

if (x == null) {
    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy yêu cầu tài sản.");
    return;
}

String ctx = request.getContextPath();
String status = x.getStatus() != null ? x.getStatus() : "";

SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

long durationHours = 0;
if (x.getStartTime() != null && x.getEndTime() != null) {
    durationHours = Math.max(0, (x.getEndTime().getTime() - x.getStartTime().getTime()) / 3600000);
}

request.setAttribute("wf03Active", "requests");
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chi tiết yêu cầu #AR-<%=x.getRequestId()%> | NTMCS</title>
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
            <jsp:param name="title" value="<%=\"📋 Xử lý Yêu cầu #AR-\" + x.getRequestId()%>"/>
        </jsp:include>

        <main>
            <div class="head">
                <div>
                    <div class="eyebrow">WF-03 • XỬ LÝ PHIẾU YÊU CẦU #AR-<%=x.getRequestId()%></div>
                    <h1>Chi tiết yêu cầu sử dụng tài sản</h1>
                    <p>Đề tài: <b><%=esc(x.getTopicCode())%> - <%=esc(x.getTopicTitle())%></b></p>
                </div>

                <div class="head-actions">
                    <a class="btn" href="<%=ctx%>/asset-requests">
                        ⬅ Danh sách yêu cầu
                    </a>
                    <a class="btn" href="<%=ctx%>/asset-requests?action=schedule&id=<%=x.getRequestId()%>">
                        📅 Kiểm tra trùng lịch
                    </a>
                    <a class="btn" href="<%=ctx%>/asset-requests?action=history&assetId=<%=x.getAssetId()%>">
                        📜 Lịch sử mượn trả
                    </a>
                </div>
            </div>

            <section class="card">
                <div class="detail-hero">
                    <div class="brand-mark" style="width: 64px; height: 64px; font-size: 32px; border-radius: 16px;">
                        📋
                    </div>

                    <div>
                        <div style="margin-bottom: 8px;">
                            <span class="request-status <%=statusCss(status)%>" style="font-size: 13px; padding: 6px 14px;">
                                <%=statusLabel(status)%>
                            </span>
                        </div>
                        <h2 style="margin: 0; font-size: 22px;">Phiếu yêu cầu #AR-<%=x.getRequestId()%></h2>
                        <p style="margin: 4px 0 0; color: #718096;">
                            Tài sản: <b><%=esc(x.getAssetName())%></b> (<%=esc(x.getAssetCode())%>) • Phóng viên: <b><%=esc(x.getRequesterName())%></b>
                        </p>
                    </div>
                </div>

                <h3 style="margin: 20px 0 12px; color: #2d3748; border-bottom: 1px solid #edf2f7; padding-bottom: 8px;">
                    Thông tin đề tài và tài sản tác nghiệp
                </h3>

                <div class="detail-grid">
                    <div class="detail-item">
                        <label>Mã phiếu yêu cầu</label>
                        <b>#AR-<%=x.getRequestId()%></b>
                    </div>

                    <div class="detail-item">
                        <label>Trạng thái yêu cầu</label>
                        <b class="request-status <%=statusCss(status)%>">
                            <%=statusLabel(status)%>
                        </b>
                    </div>

                    <div class="detail-item">
                        <label>Tên đề tài báo chí</label>
                        <b><%=esc(x.getTopicTitle())%></b>
                    </div>

                    <div class="detail-item">
                        <label>Mã đề tài</label>
                        <b><%=esc(x.getTopicCode())%></b>
                    </div>

                    <div class="detail-item">
                        <label>Tài sản mượn tác nghiệp</label>
                        <b><%=esc(x.getAssetName())%></b>
                    </div>

                    <div class="detail-item">
                        <label>Mã tài sản & Trạng thái tài sản hiện tại</label>
                        <b><%=esc(x.getAssetCode())%></b> —
                        <span class="badge <%=statusCss(x.getAssetStatus()).toLowerCase()%>">
                            <%=assetStatusLabel(x.getAssetStatus())%>
                        </span>
                    </div>

                    <div class="detail-item">
                        <label>Phóng viên / Người gửi yêu cầu</label>
                        <b><%=esc(x.getRequesterName())%></b>
                    </div>

                    <div class="detail-item">
                        <label>Email người gửi</label>
                        <b><%=esc(x.getRequesterEmail())%></b>
                    </div>

                    <div class="detail-item">
                        <label>Thời điểm bắt đầu mượn</label>
                        <b style="color: #2b6cb0;"><%=x.getStartTime() != null ? df.format(x.getStartTime()) : "-"%></b>
                    </div>

                    <div class="detail-item">
                        <label>Thời điểm dự kiến kết thúc</label>
                        <b style="color: #2b6cb0;"><%=x.getEndTime() != null ? df.format(x.getEndTime()) : "-"%></b>
                    </div>

                    <div class="detail-item" style="grid-column: 1 / -1;">
                        <label>Thời lượng đăng ký tác nghiệp</label>
                        <b><%=durationHours%> giờ</b>
                    </div>
                </div>

                <h3 style="margin: 28px 0 12px; color: #2d3748; border-bottom: 1px solid #edf2f7; padding-bottom: 8px;">
                    Thao tác xử lý nghiệp vụ theo quy trình
                </h3>

                <div class="detail-actions" style="margin-top: 16px;">
                    <a class="btn" href="<%=ctx%>/asset-requests?action=schedule&id=<%=x.getRequestId()%>">
                        📅 Kiểm tra trùng lịch
                    </a>

                    <% if ("PENDING".equals(status)) { %>
                        <!-- Nghiệp vụ Duyệt yêu cầu: kiểm tra xung đột và chuyển sang APPROVED -->
                        <form method="post" action="<%=ctx%>/asset-requests" style="display:inline;"
                              onsubmit="return confirm('Bạn có chắc chắn muốn PHÊ DUYỆT yêu cầu sử dụng tài sản này?');">
                            <input type="hidden" name="id" value="<%=x.getRequestId()%>">
                            <input type="hidden" name="action" value="approve">
                            <button class="btn primary" type="submit">
                                ✔ Phê duyệt yêu cầu
                            </button>
                        </form>

                        <!-- Nghiệp vụ Từ chối yêu cầu: chuyển sang REJECTED -->
                        <form method="post" action="<%=ctx%>/asset-requests" style="display:inline;"
                              onsubmit="return confirm('Bạn có chắc chắn muốn TỪ CHỐI yêu cầu sử dụng tài sản này?');">
                            <input type="hidden" name="id" value="<%=x.getRequestId()%>">
                            <input type="hidden" name="action" value="reject">
                            <button class="btn danger" type="submit">
                                ✖ Từ chối yêu cầu
                            </button>
                        </form>

                    <% } else if ("APPROVED".equals(status)) { %>
                        <!-- Nghiệp vụ Bàn giao: Chuyển request -> CHECKED_OUT và asset -> CHECKED_OUT trong 1 transaction -->
                        <form method="post" action="<%=ctx%>/asset-requests" style="display:inline;"
                              onsubmit="return confirm('Xác nhận ĐÃ BÀN GIAO tài sản cho phóng viên tác nghiệp?');">
                            <input type="hidden" name="id" value="<%=x.getRequestId()%>">
                            <input type="hidden" name="action" value="checkout">
                            <button class="btn primary" type="submit" style="background:#2b6cb0;">
                                🚚 Xác nhận bàn giao tài sản
                            </button>
                        </form>

                    <% } else if ("CHECKED_OUT".equals(status)) { %>
                        <!-- Nghiệp vụ Nhận lại (Thu hồi): Chuyển request -> RETURNED và asset -> AVAILABLE hoặc MAINTENANCE -->
                        <form method="post" action="<%=ctx%>/asset-requests" style="display:inline-flex; gap: 8px; align-items: center;"
                              onsubmit="return confirm('Xác nhận ĐÃ THU HỒI / NHẬN LẠI tài sản từ phóng viên?');">
                            <input type="hidden" name="id" value="<%=x.getRequestId()%>">
                            <input type="hidden" name="action" value="return">

                            <label style="font-weight: 600; font-size: 13px;">Tình trạng tài sản sau kiểm tra:</label>
                            <select name="returnStatus" style="min-width: 220px; min-height: 38px;">
                                <option value="AVAILABLE">Đạt yêu cầu - Đưa về Sẵn sàng (AVAILABLE)</option>
                                <option value="MAINTENANCE">Cần sửa chữa - Đưa về Bảo trì (MAINTENANCE)</option>
                            </select>

                            <button class="btn primary" type="submit" style="background:#2f855a;">
                                🔄 Xác nhận nhận lại (Thu hồi)
                            </button>
                        </form>
                    <% } %>
                </div>

                <% if ("REJECTED".equals(status)) { %>
                    <div class="notice error" style="margin-top: 18px;">
                        🚫 Yêu cầu này đã bị từ chối. Không được phép bàn giao tài sản.
                    </div>
                <% } else if ("RETURNED".equals(status)) { %>
                    <div class="notice success" style="margin-top: 18px;">
                        ✅ Tài sản đã được nhận lại hoàn tất. Chu trình mượn - trả cho phiếu này đã kết thúc.
                    </div>
                <% } %>
            </section>
        </main>
    </section>
</div>

<script src="<%=ctx%>/assets/js/asset-management.js"></script>
</body>
</html>
