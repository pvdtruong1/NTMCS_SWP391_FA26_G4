<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Asset" %>
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
    Asset asset = (Asset) request.getAttribute("asset");
    if (asset == null) {
        response.sendError(404, "Không tìm thấy tài sản.");
        return;
    }
    String ctx = request.getContextPath();
    String image = asset.getImagePath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Chi tiết tài sản | NTMCS</title>
<link rel="stylesheet" href="<%= ctx %>/assets/css/asset-management.css">
</head>
<body>
<div class="app">
<aside class="sidebar">
  <div class="brand"><div class="logo">N</div><div><strong>NTMCS</strong><small>Newsroom Topic Management</small></div></div>
  <div class="nav-label">TÁC NGHIỆP</div>
  <a class="nav-link active" href="<%= ctx %>/assets">▣ &nbsp; Quản lý tài sản</a>
  <a class="nav-link" href="<%= ctx %>/assets?action=add">＋ &nbsp; Thêm tài sản</a>
  <div class="side-foot">NTMCS · WF-03<br>Quản lý thiết bị và phương tiện</div>
</aside>

<section class="main">
<header class="topbar">NTMCS &nbsp;/&nbsp; Tác nghiệp &nbsp;/&nbsp; <b>Chi tiết tài sản</b><span>ASSET MANAGEMENT</span></header>
<main>
  <div class="head">
    <div>
      <div class="eyebrow">NTMCS / WF-03 / ASSET DETAIL</div>
      <h1>Chi tiết tài sản</h1>
      <p>Thông tin nhận diện và tình trạng hiện tại của tài sản.</p>
    </div>
    <div class="head-actions">
      <a class="btn" href="<%= ctx %>/assets">← Danh sách</a>
      <a class="btn primary" href="<%= ctx %>/assets?action=edit&id=<%= asset.getAssetId() %>">Chỉnh sửa</a>
    </div>
  </div>

  <div class="layout">
    <section class="card">
      <div class="detail-hero">
        <% if (image != null && !image.isBlank()) { %>
          <img class="detail-image" src="<%= ctx + esc(image) %>" alt="Ảnh <%= esc(asset.getAssetName()) %>">
        <% } else { %>
          <div class="detail-image image-placeholder">Chưa có ảnh</div>
        <% } %>
        <div>
          <div class="eyebrow">ASSET #<%= asset.getAssetId() %></div>
          <h2><%= esc(asset.getAssetName()) %></h2>
          <p>Mã tài sản: <strong><%= esc(asset.getAssetCode()) %></strong></p>
          <span class="badge <%= statusClass(asset.getAssetStatus()) %>"><%= statusLabel(asset.getAssetStatus()) %></span>
        </div>
      </div>

      <div class="fields">
        <div class="field">
          <span>Loại tài sản</span>
          <strong><%= categoryLabel(asset.getAssetCategory()) %></strong>
        </div>
        <div class="field">
          <span>Trạng thái</span>
          <strong><%= statusLabel(asset.getAssetStatus()) %></strong>
        </div>
        <div class="field">
          <span>Số serial</span>
          <strong><%= asset.getSerialNumber() == null || asset.getSerialNumber().isBlank()
                    ? "Chưa cập nhật" : esc(asset.getSerialNumber()) %></strong>
        </div>
        <div class="field">
          <span>ID bản ghi</span>
          <strong>#<%= asset.getAssetId() %></strong>
        </div>
        <div class="field full">
          <span>Mô tả</span>
          <strong class="description"><%= asset.getDescription() == null || asset.getDescription().isBlank()
                    ? "Chưa có mô tả." : esc(asset.getDescription()) %></strong>
        </div>
      </div>
    </section>

    <aside>
      <div class="info-card">
        <div class="eyebrow">TÌNH TRẠNG TÀI SẢN</div>
        <h2><%= statusLabel(asset.getAssetStatus()) %></h2>
        <p>
        <% if ("AVAILABLE".equals(asset.getAssetStatus())) { %>
          Tài sản đang được đánh dấu sẵn sàng. Hãy kiểm tra trực tiếp thiết bị trước khi bàn giao.
        <% } else if ("CHECKED_OUT".equals(asset.getAssetStatus())) { %>
          Tài sản đang được đánh dấu đang sử dụng. Không thể xóa tài sản này.
        <% } else if ("MAINTENANCE".equals(asset.getAssetStatus())) { %>
          Tài sản đang bảo trì. Không nên điều phối cho đến khi được xác nhận đủ điều kiện sử dụng.
        <% } else { %>
          Trạng thái không nằm trong danh mục chuẩn.
        <% } %>
        </p>
      </div>
      <section class="card">
        <h3>Thông tin quản lý</h3>
        <div class="info-item"><strong>Mã tài sản</strong><small><%= esc(asset.getAssetCode()) %></small></div>
        <div class="info-item"><strong>Ảnh đại diện</strong><small>Ảnh nhận diện thiết bị, không phải lịch sử ảnh bàn giao/trả.</small></div>
        <div class="info-item"><strong>Quy tắc xóa</strong><small>Không xóa nếu đang sử dụng hoặc có bản ghi yêu cầu tham chiếu.</small></div>
      </section>
    </aside>
  </div>
</main>
</section>
</div>
<script src="<%= ctx %>/assets/js/asset-management.js"></script>
</body>
</html>