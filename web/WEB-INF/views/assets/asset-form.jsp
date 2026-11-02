<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Asset" %>
<%!
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;")
                .replace(">","&gt;").replace("\"","&quot;")
                .replace("'","&#39;");
    }
%>
<%
    Asset asset = (Asset) request.getAttribute("asset");
    if (asset == null) asset = new Asset();

    boolean editing = asset.getAssetId() > 0;
    String ctx = request.getContextPath();
    String error = (String) request.getAttribute("error");
    String category = asset.getAssetCategory();
    String status = asset.getAssetStatus();
    String imagePath = asset.getImagePath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title><%= editing ? "Cập nhật" : "Thêm" %> tài sản | NTMCS</title>
<link rel="stylesheet" href="<%= ctx %>/assets/css/asset-management.css">
</head>
<body>
<div class="app">
<aside class="sidebar">
  <div class="brand"><div class="logo">N</div><div><strong>NTMCS</strong><small>Newsroom Topic Management</small></div></div>
  <div class="nav-label">TÁC NGHIỆP</div>
  <a class="nav-link" href="<%= ctx %>/assets">▣ &nbsp; Quản lý tài sản</a>
  <a class="nav-link active" href="<%= ctx %>/assets?action=add">＋ &nbsp; Thêm tài sản</a>
  <div class="side-foot">NTMCS · Manager Assets<br>Quản lý thiết bị và phương tiện</div>
</aside>

<section class="main">
<header class="topbar">NTMCS &nbsp;/&nbsp; Tác nghiệp &nbsp;/&nbsp; <b><%= editing ? "Cập nhật tài sản" : "Thêm tài sản" %></b><span>ASSET MANAGEMENT</span></header>
<main>
  <div class="head">
    <div>
      <div class="eyebrow">NTMCS / Manager Assets / RESOURCE MANAGEMENT</div>
      <h1><%= editing ? "Cập nhật tài sản" : "Thêm tài sản mới" %></h1>
      <p>Nhập thông tin chính xác để phục vụ điều phối thiết bị tác nghiệp.</p>
    </div>
    <a class="btn" href="<%= ctx %>/assets">← Quay lại danh sách</a>
  </div>

  <% if (error != null && !error.isBlank()) { %>
    <div class="notice error"><strong>Không thể lưu dữ liệu:</strong> <%= esc(error) %></div>
  <% } %>

  <div class="layout">
    <section class="card">
      <div class="card-head">
        <div><h2>Thông tin tài sản</h2><p>Các trường có dấu * là bắt buộc.</p></div>
        <span class="badge"><%= editing ? "CHỈNH SỬA" : "TẠO MỚI" %></span>
      </div>

      <form method="post" action="<%= ctx %>/assets" enctype="multipart/form-data" id="assetForm">
        <input type="hidden" name="action" value="<%= editing ? "update" : "insert" %>">
        <% if (editing) { %>
          <input type="hidden" name="id" value="<%= asset.getAssetId() %>">
        <% } %>

        <div class="grid">
          <div class="field">
            <label for="assetCode">Mã tài sản <span class="req">*</span></label>
            <input id="assetCode" name="assetCode" required maxlength="100"
                   pattern="[\p{L}\p{N}][\p{L}\p{N}_./-]*"
                   title="Chỉ dùng chữ, số, dấu gạch ngang, gạch dưới, dấu chấm hoặc dấu /."
                   value="<%= esc(asset.getAssetCode()) %>" placeholder="Ví dụ: AST-0001">
            <small class="hint">Mã duy nhất, không có khoảng trắng.</small>
          </div>

          <div class="field">
            <label for="assetName">Tên tài sản <span class="req">*</span></label>
            <input id="assetName" name="assetName" required maxlength="200"
                   value="<%= esc(asset.getAssetName()) %>"
                   placeholder="Tên thiết bị hoặc phương tiện">
          </div>

          <div class="field">
            <label for="assetCategory">Loại tài sản <span class="req">*</span></label>
            <select id="assetCategory" name="assetCategory" required>
              <option value="">-- Chọn loại tài sản --</option>
              <option value="CAMERA_EQUIPMENT" <%= "CAMERA_EQUIPMENT".equals(category) ? "selected" : "" %>>Thiết bị quay phim</option>
              <option value="PHOTO_EQUIPMENT" <%= "PHOTO_EQUIPMENT".equals(category) ? "selected" : "" %>>Thiết bị chụp ảnh</option>
              <option value="AUDIO_EQUIPMENT" <%= "AUDIO_EQUIPMENT".equals(category) ? "selected" : "" %>>Thiết bị âm thanh</option>
              <option value="ACCESSORIES" <%= "ACCESSORIES".equals(category) ? "selected" : "" %>>Phụ kiện tác nghiệp</option>
              <option value="IT_EQUIPMENT" <%= "IT_EQUIPMENT".equals(category) ? "selected" : "" %>>Thiết bị công nghệ</option>
              <option value="VEHICLE" <%= "VEHICLE".equals(category) ? "selected" : "" %>>Phương tiện vận chuyển</option>
            </select>
          </div>

          <div class="field">
            <label for="assetStatus">Trạng thái <span class="req">*</span></label>
            <select id="assetStatus" name="assetStatus" required>
              <option value="">-- Chọn trạng thái --</option>
              <option value="AVAILABLE" <%= "AVAILABLE".equals(status) ? "selected" : "" %>>Sẵn sàng</option>
              <option value="CHECKED_OUT" <%= "CHECKED_OUT".equals(status) ? "selected" : "" %>>Đang sử dụng</option>
              <option value="MAINTENANCE" <%= "MAINTENANCE".equals(status) ? "selected" : "" %>>Đang bảo trì</option>
            </select>
            <small class="hint">Chọn trạng thái đúng với tình trạng vận hành.</small>
          </div>

          <div class="field full">
            <label for="serialNumber">Số serial</label>
            <input id="serialNumber" name="serialNumber" maxlength="150"
                   value="<%= esc(asset.getSerialNumber()) %>"
                   placeholder="Nhập số serial nếu có">
          </div>

          <div class="field full">
            <label for="description">Mô tả</label>
            <textarea id="description" name="description" maxlength="2000"
                      placeholder="Model, phụ kiện đi kèm, ghi chú..."><%= esc(asset.getDescription()) %></textarea>
          </div>

          <div class="field full">
            <label for="imageFile">Ảnh đại diện tài sản</label>
            <input id="imageFile" name="imageFile" type="file" accept=".jpg,.jpeg,.png,image/jpeg,image/png">
            <small class="hint">Chỉ JPG/PNG, tối đa 5 MB. Không bắt buộc tải ảnh.</small>
            <div class="image-preview-wrap">
              <img id="imagePreview"
                   class="image-preview"
                   alt="Xem trước ảnh tài sản"
                   <% if (imagePath != null && !imagePath.isBlank()) { %>
                     src="<%= ctx + esc(imagePath) %>"
                   <% } else { %>
                     hidden
                   <% } %>>
              <div id="noImage" class="image-placeholder"
                   <%= imagePath != null && !imagePath.isBlank() ? "hidden" : "" %>>
                Chưa có ảnh đại diện
              </div>
            </div>
            <% if (editing && imagePath != null && !imagePath.isBlank()) { %>
              <small class="hint">Nếu không chọn ảnh mới, ảnh hiện tại sẽ được giữ lại.</small>
            <% } %>
          </div>
        </div>

        <div class="actions">
          <a class="btn" href="<%= ctx %>/assets">Hủy</a>
          <button class="btn primary" type="submit"><%= editing ? "Lưu thay đổi" : "Tạo tài sản" %></button>
        </div>
      </form>
    </section>

    <aside>
      <div class="info-card">
        <div class="eyebrow">Manager Assets / ASSET</div>
        <h2>Thông tin tài sản</h2>
        <p>Ảnh đại diện giúp người dùng nhận diện thiết bị. Ảnh này không thay thế ảnh bàn giao/trả ở từng lần mượn.</p>
      </div>
      <section class="card">
        <h3>Quy tắc nhập liệu</h3>
        <div class="info-item"><strong>01 · Mã tài sản</strong><small>Bắt buộc và không được trùng mã tài sản khác.</small></div>
        <div class="info-item"><strong>02 · Loại và trạng thái</strong><small>Chỉ chọn giá trị thuộc danh mục cố định.</small></div>
        <div class="info-item"><strong>03 · Ảnh</strong><small>JPG/PNG, dung lượng tối đa 5 MB.</small></div>
        <div class="info-item"><strong>04 · Lịch sử mượn/trả</strong><small>Tài sản có lịch sử yêu cầu sẽ không bị xóa bởi chức năng này.</small></div>
      </section>
    </aside>
  </div>
</main>
</section>
<script src="<%= ctx %>/assets/js/asset-management.js"></script>
</body>
</html>