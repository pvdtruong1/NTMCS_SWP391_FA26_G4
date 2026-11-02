<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,model.Asset" %>
<%!
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;")
                .replace(">","&gt;").replace("\"","&quot;")
                .replace("'","&#39;");
    }
    private String categoryLabel(String s) {
        if ("CAMERA_EQUIPMENT".equals(s)) return "Thiết bị quay phim";
        if ("PHOTO_EQUIPMENT".equals(s)) return "Thiết bị chụp ảnh";
        if ("AUDIO_EQUIPMENT".equals(s)) return "Thiết bị âm thanh";
        if ("ACCESSORIES".equals(s)) return "Phụ kiện tác nghiệp";
        if ("IT_EQUIPMENT".equals(s)) return "Thiết bị công nghệ";
        if ("VEHICLE".equals(s)) return "Phương tiện vận chuyển";
        return "Chưa phân loại";
    }
    private String statusLabel(String s) {
        if ("AVAILABLE".equals(s)) return "Sẵn sàng";
        if ("CHECKED_OUT".equals(s)) return "Đang sử dụng";
        if ("MAINTENANCE".equals(s)) return "Đang bảo trì";
        return "Không xác định";
    }
    private String statusClass(String s) {
        if ("AVAILABLE".equals(s)) return "available";
        if ("CHECKED_OUT".equals(s)) return "checked";
        if ("MAINTENANCE".equals(s)) return "maintenance";
        return "";
    }
%>
<%
    List<Asset> assets = (List<Asset>) request.getAttribute("assets");
    if (assets == null) assets = new java.util.ArrayList<>();

    String ctx = request.getContextPath();
    String message = request.getParameter("message");
    int available = 0, checkedOut = 0, maintenance = 0;

    for (Asset a : assets) {
        if ("AVAILABLE".equals(a.getAssetStatus())) available++;
        if ("CHECKED_OUT".equals(a.getAssetStatus())) checkedOut++;
        if ("MAINTENANCE".equals(a.getAssetStatus())) maintenance++;
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Quản lý tài sản | NTMCS</title>
<link rel="stylesheet" href="<%= ctx %>/assets/css/asset-management.css">
</head>
<body>
<div class="app">
<aside class="sidebar">
  <div class="brand">
    <div class="logo">N</div>
    <div><strong>NTMCS</strong><small>Newsroom Topic Management</small></div>
  </div>
  <div class="nav-label">TÁC NGHIỆP</div>
  <a class="nav-link active" href="<%= ctx %>/assets">▣ &nbsp; Quản lý tài sản</a>
  <a class="nav-link" href="<%= ctx %>/assets?action=add">＋ &nbsp; Thêm tài sản</a>
  <div class="side-foot">NTMCS · WF-03<br>Quản lý thiết bị và phương tiện</div>
</aside>

<section class="main">
<header class="topbar">
  NTMCS &nbsp;/&nbsp; Tác nghiệp &nbsp;/&nbsp; <b>Quản lý tài sản</b>
  <span>ASSET MANAGEMENT</span>
</header>

<main>
  <div class="head">
    <div>
      <div class="eyebrow">NTMCS / Manager Assets / RESOURCE MANAGEMENT</div>
      <h1>Quản lý tài sản</h1>
      <p>Quản lý thiết bị tác nghiệp, phụ kiện và phương tiện vận chuyển.</p>
    </div>
    <a class="btn primary" href="<%= ctx %>/assets?action=add">＋ Thêm tài sản</a>
  </div>

  <% if ("added".equals(message)) { %>
    <div class="notice success">Đã thêm tài sản thành công.</div>
  <% } else if ("updated".equals(message)) { %>
    <div class="notice success">Đã cập nhật tài sản thành công.</div>
  <% } else if ("deleted".equals(message)) { %>
    <div class="notice success">Đã xóa tài sản thành công.</div>
  <% } else if ("inuse".equals(message)) { %>
    <div class="notice error">Không thể xóa: thiết bị đang được sử dụng hoặc đã được đánh dấu CHECKED_OUT.</div>
  <% } else if ("history".equals(message)) { %>
    <div class="notice error">Không thể xóa: tài sản đang được tham chiếu bởi lịch sử yêu cầu mượn/trả. Hãy giữ lại bản ghi.</div>
  <% } else if ("notfound".equals(message)) { %>
    <div class="notice error">Không tìm thấy tài sản cần xóa; danh sách đã được tải lại.</div>
  <% } else if ("deleteerror".equals(message)) { %>
    <div class="notice error">Không thể xóa tài sản. Hãy kiểm tra dữ liệu liên quan.</div>
  <% } %>

  <div class="stats">
    <div class="stat"><div class="stat-icon">▣</div><div><span>Tổng tài sản</span><strong><%= assets.size() %></strong><small>Tài sản được tải từ database</small></div></div>
    <div class="stat"><div class="stat-icon">✓</div><div><span>Sẵn sàng</span><strong><%= available %></strong><small>AVAILABLE</small></div></div>
    <div class="stat"><div class="stat-icon">↔</div><div><span>Đang sử dụng</span><strong><%= checkedOut %></strong><small>CHECKED_OUT</small></div></div>
    <div class="stat"><div class="stat-icon">⚙</div><div><span>Đang bảo trì</span><strong><%= maintenance %></strong><small>MAINTENANCE</small></div></div>
  </div>

  <section class="card">
    <div class="card-head">
      <div><h2>Danh sách tài sản</h2><p>Tìm kiếm và lọc dữ liệu tài sản.</p></div>
      <span class="muted"><%= assets.size() %> bản ghi</span>
    </div>

    <div class="filters">
      <input type="search" id="searchInput" placeholder="Tìm mã, tên, serial hoặc mô tả...">
      <select id="categoryFilter">
        <option value="">Tất cả loại tài sản</option>
        <option value="CAMERA_EQUIPMENT">Thiết bị quay phim</option>
        <option value="PHOTO_EQUIPMENT">Thiết bị chụp ảnh</option>
        <option value="AUDIO_EQUIPMENT">Thiết bị âm thanh</option>
        <option value="ACCESSORIES">Phụ kiện tác nghiệp</option>
        <option value="IT_EQUIPMENT">Thiết bị công nghệ</option>
        <option value="VEHICLE">Phương tiện vận chuyển</option>
      </select>
      <select id="statusFilter">
        <option value="">Tất cả trạng thái</option>
        <option value="AVAILABLE">Sẵn sàng</option>
        <option value="CHECKED_OUT">Đang sử dụng</option>
        <option value="MAINTENANCE">Đang bảo trì</option>
      </select>
      <button class="btn" type="button" id="resetFilters">Đặt lại</button>
    </div>

    <div class="table-wrap">
      <table id="assetTable">
        <thead>
          <tr>
            <th>Ảnh</th><th>Tài sản</th><th>Loại tài sản</th>
            <th>Serial</th><th>Trạng thái</th><th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
        <% if (assets.isEmpty()) { %>
          <tr><td colspan="6">
            <div class="empty">
              <strong>Chưa có tài sản nào</strong>
              Thêm tài sản để bắt đầu quản lý.
              <br><a class="btn primary" href="<%= ctx %>/assets?action=add">＋ Thêm tài sản</a>
            </div>
          </td></tr>
        <% } else { for (Asset a : assets) {
            String category = a.getAssetCategory();
            String status = a.getAssetStatus();
            String image = a.getImagePath();
        %>
          <tr data-category="<%= esc(category) %>" data-status="<%= esc(status) %>">
            <td>
              <% if (image != null && !image.isBlank()) { %>
                <img class="asset-thumb" src="<%= ctx + esc(image) %>" alt="Ảnh <%= esc(a.getAssetName()) %>">
              <% } else { %>
                <div class="asset-thumb placeholder" aria-label="Chưa có ảnh">▣</div>
              <% } %>
            </td>
            <td>
              <strong><%= esc(a.getAssetName()) %></strong>
              <span class="sub"><%= esc(a.getAssetCode()) %></span>
            </td>
            <td><span class="category"><%= categoryLabel(category) %></span></td>
            <td><%= a.getSerialNumber() == null || a.getSerialNumber().isBlank()
                    ? "—" : esc(a.getSerialNumber()) %></td>
            <td><span class="badge <%= statusClass(status) %>"><%= statusLabel(status) %></span></td>
            <td>
              <div class="actions">
                <a class="btn small" href="<%= ctx %>/assets?action=detail&id=<%= a.getAssetId() %>">Xem</a>
                <a class="btn small" href="<%= ctx %>/assets?action=edit&id=<%= a.getAssetId() %>">Sửa</a>
                <form method="post" action="<%= ctx %>/assets" class="delete-form">
                  <input type="hidden" name="action" value="delete">
                  <input type="hidden" name="id" value="<%= a.getAssetId() %>">
                  <button class="btn small danger" type="submit"
                    <%= "CHECKED_OUT".equals(status) ? "disabled title=\"Thiết bị đang sử dụng\"" : "" %>>
                    Xóa
                  </button>
                </form>
              </div>
            </td>
          </tr>
        <% } } %>
        </tbody>
      </table>
    </div>
    <div class="count">
      <span id="resultCount">Đang hiển thị <%= assets.size() %> tài sản</span>
      <span>Ảnh đại diện · 6 loại tài sản · 3 trạng thái</span>
    </div>
  </section>
</main>
</section>
</div>
<script src="<%= ctx %>/assets/js/asset-management.js"></script>
</body>
</html>