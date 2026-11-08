<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Asset" %>

<%!
private String esc(String s) {
    if (s == null || s.isEmpty()) return "-";
    return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
}

private String categoryLabel(String s) {
    if ("CAMERA_EQUIPMENT".equals(s)) return "Thiết bị quay phim";
    if ("PHOTO_EQUIPMENT".equals(s)) return "Thiết bị chụp ảnh";
    if ("AUDIO_EQUIPMENT".equals(s)) return "Thiết bị âm thanh";
    if ("ACCESSORIES".equals(s)) return "Phụ kiện tác nghiệp";
    if ("IT_EQUIPMENT".equals(s)) return "Thiết bị công nghệ";
    if ("VEHICLE".equals(s)) return "Phương tiện vận chuyển";
    return s != null ? s : "Khác";
}

private String statusLabel(String s) {
    if ("AVAILABLE".equals(s)) return "Sẵn sàng";
    if ("CHECKED_OUT".equals(s)) return "Đang bàn giao";
    if ("MAINTENANCE".equals(s)) return "Đang bảo trì";
    return s != null ? s : "Không xác định";
}

private String statusBadgeClass(String s) {
    if ("AVAILABLE".equals(s)) return "available";
    if ("CHECKED_OUT".equals(s)) return "checked";
    if ("MAINTENANCE".equals(s)) return "maintenance";
    return "";
}
%>

<%
Asset a = (Asset) request.getAttribute("asset");

if (a == null) {
    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy tài sản.");
    return;
}

String ctx = request.getContextPath();
String status = a.getAssetStatus();
String label = statusLabel(status);

request.setAttribute("wf03Active", "assets");
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chi tiết tài sản [<%=esc(a.getAssetCode())%>] | NTMCS</title>
    <link rel="stylesheet" href="<%=ctx%>/assets/css/asset-management.css">
</head>

<body>
<div class="app">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>

    <section class="main">
        <header class="topbar">
            <span>NTMCS / Hậu cần / <a href="<%=ctx%>/assets" style="color:inherit;text-decoration:none;">Tài sản</a> / <b>Chi tiết</b></span>
            <span>LOGISTICS COORDINATOR</span>
        </header>

        <main>
            <div class="head">
                <div>
                    <div class="eyebrow">WF-03 • CHI TIẾT TÀI SẢN #<%=a.getAssetId()%></div>
                    <h1><%=esc(a.getAssetName())%></h1>
                    <p>Mã định danh: <b><%=esc(a.getAssetCode())%></b> • Nhóm: <%=categoryLabel(a.getAssetCategory())%></p>
                </div>

                <div class="head-actions">
                    <a class="btn" href="<%=ctx%>/assets">
                        ⬅ Quay lại danh sách
                    </a>
                    <a class="btn" href="<%=ctx%>/asset-requests?action=history&assetId=<%=a.getAssetId()%>">
                        📜 Lịch sử bàn giao / nhận lại
                    </a>
                    <a class="btn primary" href="<%=ctx%>/assets?action=edit&id=<%=a.getAssetId()%>">
                        ✏ Chỉnh sửa
                    </a>
                </div>
            </div>

            <section class="card">
                <div class="detail-hero">
                    <div class="brand-mark" style="width: 64px; height: 64px; font-size: 32px; border-radius: 16px;">
                        📦
                    </div>

                    <div>
                        <div style="margin-bottom: 8px;">
                            <span class="badge <%=statusBadgeClass(status)%>" style="font-size: 13px; padding: 6px 12px;">
                                <%=label%>
                            </span>
                        </div>
                        <h2 style="margin: 0; font-size: 22px;"><%=esc(a.getAssetName())%></h2>
                        <p style="margin: 4px 0 0; color: #718096;">Mã kho: <b><%=esc(a.getAssetCode())%></b> • <%=categoryLabel(a.getAssetCategory())%></p>
                    </div>
                </div>

                <div class="detail-grid">
                    <div class="detail-item">
                        <label>Mã tài sản</label>
                        <b><%=esc(a.getAssetCode())%></b>
                    </div>

                    <div class="detail-item">
                        <label>Tên tài sản</label>
                        <b><%=esc(a.getAssetName())%></b>
                    </div>

                    <div class="detail-item">
                        <label>Phân loại tài sản</label>
                        <b><%=categoryLabel(a.getAssetCategory())%></b>
                    </div>

                    <div class="detail-item">
                        <label>Trạng thái hiện tại</label>
                        <b class="<%=statusBadgeClass(status)%>" style="padding: 2px 6px; border-radius: 4px;">
                            <%=label%> (<%=esc(status)%>)
                        </b>
                    </div>

                    <div class="detail-item">
                        <label>Số serial / Biển số phương tiện</label>
                        <b><%=esc(a.getSerialNumber())%></b>
                    </div>

                    <div class="detail-item">
                        <label>Lịch sử sử dụng</label>
                        <a href="<%=ctx%>/asset-requests?action=history&assetId=<%=a.getAssetId()%>" style="color: #2868d7; text-decoration: none; font-weight: bold;">
                            Xem nhật ký các lần mượn / trả »
                        </a>
                    </div>

                    <div class="detail-item" style="grid-column: 1 / -1;">
                        <label>Mô tả kỹ thuật & Ghi chú</label>
                        <div class="description" style="margin-top: 6px; line-height: 1.6; color: #2d3748;">
                            <%=esc(a.getDescription())%>
                        </div>
                    </div>
                </div>

                <div class="actions" style="margin-top: 24px;">
                    <a class="btn" href="<%=ctx%>/assets">⬅ Danh sách tài sản</a>
                    <a class="btn primary" href="<%=ctx%>/assets?action=edit&id=<%=a.getAssetId()%>">✏ Chỉnh sửa thông tin</a>
                    <a class="btn" href="<%=ctx%>/asset-requests?action=history&assetId=<%=a.getAssetId()%>">📜 Lịch sử sử dụng</a>
                </div>
            </section>
        </main>
    </section>
</div>

<script src="<%=ctx%>/assets/js/asset-management.js"></script>
</body>
</html>
