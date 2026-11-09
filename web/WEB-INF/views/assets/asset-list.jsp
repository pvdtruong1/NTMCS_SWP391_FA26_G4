<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.util.ArrayList,java.net.URLEncoder,model.Asset" %>

<%!
private String esc(String s) {
    if (s == null) return "";
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

private int getIntAttr(Object val, int fallback) {
    if (val instanceof Number) {
        return ((Number) val).intValue();
    }
    return fallback;
}
%>

<%
List<Asset> assets = (List<Asset>) request.getAttribute("assets");
if (assets == null) {
    assets = new ArrayList<>();
}

String ctx = request.getContextPath();
String msg = request.getParameter("message");

String keyword = (String) request.getAttribute("keyword");
if (keyword == null) keyword = "";

String category = (String) request.getAttribute("category");
if (category == null) category = "";

String status = (String) request.getAttribute("status");
if (status == null) status = "";

int total = getIntAttr(request.getAttribute("totalAssets"), 0);
int available = getIntAttr(request.getAttribute("availableCount"), 0);
int checked = getIntAttr(request.getAttribute("checkedOutCount"), 0);
int maintenance = getIntAttr(request.getAttribute("maintenanceCount"), 0);

int current = Math.max(1, getIntAttr(request.getAttribute("currentPage"), 1));
int pages = Math.max(1, getIntAttr(request.getAttribute("totalPages"), 1));
int records = getIntAttr(request.getAttribute("totalRecords"), 0);

request.setAttribute("wf03Active", "assets");

String qEnc = URLEncoder.encode(keyword, "UTF-8");
String catEnc = URLEncoder.encode(category, "UTF-8");
String statEnc = URLEncoder.encode(status, "UTF-8");
String baseQuery = "keyword=" + qEnc + "&category=" + catEnc + "&status=" + statEnc;
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý danh sách tài sản | NTMCS</title>
    <link rel="stylesheet" href="<%=ctx%>/css/style.css">
    <link rel="stylesheet" href="<%=ctx%>/assets/css/asset-management.css">
</head>
<body>

<div class="app">
    <jsp:include page="/WEB-INF/views/sidebar.jsp">
        <jsp:param name="active" value="assets"/>
    </jsp:include>

    <section class="main">
        <jsp:include page="/WEB-INF/views/topbar.jsp">
            <jsp:param name="title" value="📦 Quản Lý Trang Thiết Bị (WF-03)"/>
        </jsp:include>

        <main>
            <div class="head">
                <div>
                    <div class="eyebrow">WF-03 • QUẢN LÝ THIẾT BỊ VÀ PHƯƠNG TIỆN</div>
                    <h1>Danh sách tài sản</h1>
                    <p>Quản lý toàn bộ thiết bị quay phim, chụp ảnh, âm thanh, phụ kiện và phương tiện tác nghiệp.</p>
                </div>

                <div class="head-actions">
                    <a class="btn primary" href="<%=ctx%>/assets?action=add">
                        ➕ Thêm tài sản mới
                    </a>
                </div>
            </div>

            <% if ("added".equals(msg)) { %>
                <div class="notice success">✅ Đã thêm tài sản mới thành công.</div>
            <% } else if ("updated".equals(msg)) { %>
                <div class="notice success">✅ Đã cập nhật thông tin tài sản thành công.</div>
            <% } else if ("deleted".equals(msg)) { %>
                <div class="notice success">✅ Đã xóa tài sản khỏi hệ thống.</div>
            <% } else if ("inuse".equals(msg)) { %>
                <div class="notice error">❌ Không thể xóa tài sản đang ở trạng thái 'Đang bàn giao'.</div>
            <% } else if ("history".equals(msg)) { %>
                <div class="notice error">❌ Không thể xóa tài sản đã có lịch sử yêu cầu sử dụng trong hệ thống.</div>
            <% } else if ("notfound".equals(msg)) { %>
                <div class="notice error">❌ Không tìm thấy thông tin tài sản yêu cầu.</div>
            <% } else if ("deleteerror".equals(msg)) { %>
                <div class="notice error">❌ Thao tác xóa không thành công do ràng buộc toàn vẹn dữ liệu.</div>
            <% } %>

            <div class="stats">
                <div class="stat">
                    <span>Tổng số tài sản</span>
                    <strong><%=total%></strong>
                    <small>Trong toàn bộ kho</small>
                </div>

                <div class="stat">
                    <span>Sẵn sàng điều phối</span>
                    <strong style="color: #16804a;"><%=available%></strong>
                    <small>Có thể cấp phát</small>
                </div>

                <div class="stat">
                    <span>Đang bàn giao tác nghiệp</span>
                    <strong style="color: #2861b2;"><%=checked%></strong>
                    <small>Đang sử dụng thực tế</small>
                </div>

                <div class="stat">
                    <span>Đang bảo trì</span>
                    <strong style="color: #b65b21;"><%=maintenance%></strong>
                    <small>Tạm ngừng cấp phát</small>
                </div>
            </div>

            <section class="card">
                <div class="card-head">
                    <div>
                        <h2>Tìm kiếm và Bộ lọc</h2>
                        <p>Tìm kiếm theo mã tài sản, tên, serial hoặc mô tả kỹ thuật.</p>
                    </div>
                    <span class="count"><%=records%> kết quả phù hợp</span>
                </div>

                <form class="filters" method="get" action="<%=ctx%>/assets">
                    <input type="text"
                           name="keyword"
                           value="<%=esc(keyword)%>"
                           placeholder="Nhập mã, tên tài sản, serial hoặc mô tả...">

                    <select name="category">
                        <option value="">-- Tất cả loại tài sản --</option>
                        <option value="CAMERA_EQUIPMENT" <%="CAMERA_EQUIPMENT".equals(category) ? "selected" : ""%>>Thiết bị quay phim</option>
                        <option value="PHOTO_EQUIPMENT" <%="PHOTO_EQUIPMENT".equals(category) ? "selected" : ""%>>Thiết bị chụp ảnh</option>
                        <option value="AUDIO_EQUIPMENT" <%="AUDIO_EQUIPMENT".equals(category) ? "selected" : ""%>>Thiết bị âm thanh</option>
                        <option value="ACCESSORIES" <%="ACCESSORIES".equals(category) ? "selected" : ""%>>Phụ kiện tác nghiệp</option>
                        <option value="IT_EQUIPMENT" <%="IT_EQUIPMENT".equals(category) ? "selected" : ""%>>Thiết bị công nghệ</option>
                        <option value="VEHICLE" <%="VEHICLE".equals(category) ? "selected" : ""%>>Phương tiện vận chuyển</option>
                    </select>

                    <select name="status">
                        <option value="">-- Tất cả trạng thái --</option>
                        <option value="AVAILABLE" <%="AVAILABLE".equals(status) ? "selected" : ""%>>Sẵn sàng</option>
                        <option value="CHECKED_OUT" <%="CHECKED_OUT".equals(status) ? "selected" : ""%>>Đang bàn giao</option>
                        <option value="MAINTENANCE" <%="MAINTENANCE".equals(status) ? "selected" : ""%>>Đang bảo trì</option>
                    </select>

                    <button class="btn primary" type="submit">🔍 Tìm kiếm</button>
                    <a class="btn" href="<%=ctx%>/assets">🔄 Đặt lại</a>
                </form>

                <div class="table-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th style="width: 120px;">Mã tài sản</th>
                                <th>Tên tài sản</th>
                                <th>Loại tài sản</th>
                                <th style="width: 130px;">Trạng thái</th>
                                <th>Serial / Biển số</th>
                                <th>Mô tả</th>
                                <th style="width: 220px; text-align: center;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if (assets.isEmpty()) { %>
                                <tr>
                                    <td colspan="7" class="empty">
                                        <b>Không tìm thấy tài sản nào phù hợp.</b>
                                        <p>Vui lòng thử điều chỉnh lại từ khóa hoặc bộ lọc tìm kiếm.</p>
                                    </td>
                                </tr>
                            <% } else {
                                for (Asset a : assets) {
                            %>
                                <tr>
                                    <td>
                                        <strong><%=esc(a.getAssetCode())%></strong>
                                    </td>
                                    <td>
                                        <b><%=esc(a.getAssetName())%></b>
                                    </td>
                                    <td>
                                        <%=categoryLabel(a.getAssetCategory())%>
                                    </td>
                                    <td>
                                        <span class="badge <%=statusBadgeClass(a.getAssetStatus())%>">
                                            <%=statusLabel(a.getAssetStatus())%>
                                        </span>
                                    </td>
                                    <td>
                                        <%=a.getSerialNumber() != null && !a.getSerialNumber().isEmpty() ? esc(a.getSerialNumber()) : "<span style='color:#a0aec0;'>-</span>"%>
                                    </td>
                                    <td>
                                        <small><%=a.getDescription() != null && !a.getDescription().isEmpty() ? esc(a.getDescription()) : "<span style='color:#a0aec0;'>-</span>"%></small>
                                    </td>
                                    <td>
                                        <div class="action-buttons" style="justify-content: center;">
                                            <a class="btn small"
                                               href="<%=ctx%>/assets?action=detail&id=<%=a.getAssetId()%>"
                                               title="Xem chi tiết tài sản">
                                                👁 Xem
                                            </a>

                                            <a class="btn small"
                                               href="<%=ctx%>/assets?action=edit&id=<%=a.getAssetId()%>"
                                               title="Chỉnh sửa thông tin">
                                                ✏ Sửa
                                            </a>

                                            <a class="btn small"
                                               href="<%=ctx%>/asset-requests?action=history&assetId=<%=a.getAssetId()%>"
                                               title="Xem lịch sử sử dụng">
                                                📜 Lịch sử
                                            </a>

                                            <% if (!"CHECKED_OUT".equals(a.getAssetStatus())) { %>
                                                <form class="delete-form" method="post" action="<%=ctx%>/assets">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="id" value="<%=a.getAssetId()%>">
                                                    <button class="btn small danger"
                                                            type="submit"
                                                            title="Xóa tài sản">
                                                        🗑
                                                    </button>
                                                </form>
                                            <% } else { %>
                                                <button class="btn small"
                                                        disabled
                                                        title="Không thể xóa khi đang bàn giao"
                                                        style="opacity: 0.4;">
                                                    🔒
                                                </button>
                                            <% } %>
                                        </div>
                                    </td>
                                </tr>
                            <%  }
                               }
                            %>
                        </tbody>
                    </table>
                </div>

                <% if (pages > 1) { %>
                    <div class="pagination">
                        <% if (current > 1) { %>
                            <a href="<%=ctx%>/assets?page=<%=current - 1%>&<%=baseQuery%>">« Trước</a>
                        <% } %>

                        <% for (int p = 1; p <= pages; p++) { %>
                            <% if (p == current) { %>
                                <span class="active"><%=p%></span>
                            <% } else if (p == 1 || p == pages || Math.abs(p - current) <= 2) { %>
                                <a href="<%=ctx%>/assets?page=<%=p%>&<%=baseQuery%>"><%=p%></a>
                            <% } else if (p == current - 3 || p == current + 3) { %>
                                <span style="border:none;">...</span>
                            <% } %>
                        <% } %>

                        <% if (current < pages) { %>
                            <a href="<%=ctx%>/assets?page=<%=current + 1%>&<%=baseQuery%>">Sau »</a>
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
