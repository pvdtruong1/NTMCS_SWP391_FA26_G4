<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Asset" %>

<%!
private String esc(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
}
%>

<%
Asset asset = (Asset) request.getAttribute("asset");
if (asset == null) {
    asset = new Asset();
}

boolean editing = asset.getAssetId() > 0;
String ctx = request.getContextPath();
String error = (String) request.getAttribute("error");

String category = asset.getAssetCategory() != null ? asset.getAssetCategory() : "";
String status = asset.getAssetStatus() != null ? asset.getAssetStatus() : "AVAILABLE";
boolean isCheckedOut = "CHECKED_OUT".equals(status);

request.setAttribute("wf03Active", editing ? "assets" : "add");
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><%=editing ? "Cập nhật tài sản" : "Thêm tài sản mới"%> | NTMCS</title>
    <link rel="stylesheet" href="<%=ctx%>/assets/css/asset-management.css">
</head>

<body>
<div class="app">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>

    <section class="main">
        <header class="topbar">
            <span>NTMCS / Hậu cần / <b><%=editing ? "Cập nhật tài sản" : "Thêm tài sản mới"%></b></span>
            <span>LOGISTICS COORDINATOR</span>
        </header>

        <main>
            <div class="head">
                <div>
                    <div class="eyebrow">WF-03 • QUẢN LÝ THIẾT BỊ VÀ PHƯƠNG TIỆN</div>
                    <h1><%=editing ? "Cập nhật tài sản: " + esc(asset.getAssetCode()) : "Thêm tài sản mới"%></h1>
                    <p>Nhập thông tin kỹ thuật và phân loại danh mục chuẩn phục vụ điều phối tác nghiệp.</p>
                </div>

                <div class="head-actions">
                    <a class="btn" href="<%=ctx%>/assets">
                        ⬅ Quay lại danh sách
                    </a>
                </div>
            </div>

            <% if (error != null && !error.trim().isEmpty()) { %>
                <div class="notice error">
                    <strong>❌ Không thể lưu dữ liệu:</strong> <%=esc(error)%>
                </div>
            <% } %>

            <div class="layout">
                <section class="card">
                    <div class="card-head">
                        <div>
                            <h2>Thông tin thông số kỹ thuật</h2>
                            <p>Các trường có dấu <span class="req">*</span> là bắt buộc nhập.</p>
                        </div>
                        <span class="badge <%=editing ? "checked" : "available"%>">
                            <%=editing ? "CHẾ ĐỘ CHỈNH SỬA" : "TẠO MỚI"%>
                        </span>
                    </div>

                    <form method="post" action="<%=ctx%>/assets" id="assetForm">
                        <input type="hidden" name="action" value="<%=editing ? "update" : "insert"%>">

                        <% if (editing) { %>
                            <input type="hidden" name="id" value="<%=asset.getAssetId()%>">
                        <% } %>

                        <div class="grid">
                            <div class="field">
                                <label for="assetCode">Mã tài sản <span class="req">*</span></label>
                                <input id="assetCode"
                                       name="assetCode"
                                       type="text"
                                       required
                                       maxlength="30"
                                       value="<%=esc(asset.getAssetCode())%>"
                                       placeholder="Ví dụ: CAM-001, CAR-01...">
                                <small class="hint">Mã định danh duy nhất (tối đa 30 ký tự, không trùng lặp).</small>
                            </div>

                            <div class="field">
                                <label for="assetName">Tên tài sản <span class="req">*</span></label>
                                <input id="assetName"
                                       name="assetName"
                                       type="text"
                                       required
                                       maxlength="150"
                                       value="<%=esc(asset.getAssetName())%>"
                                       placeholder="Tên thiết bị hoặc loại xe vận chuyển...">
                                <small class="hint">Tên model hoặc biển số xe (tối đa 150 ký tự).</small>
                            </div>

                            <div class="field">
                                <label for="assetCategory">Loại tài sản <span class="req">*</span></label>
                                <select id="assetCategory" name="assetCategory" required>
                                    <option value="">-- Chọn phân loại tài sản --</option>
                                    <option value="CAMERA_EQUIPMENT" <%="CAMERA_EQUIPMENT".equals(category) ? "selected" : ""%>>Thiết bị quay phim</option>
                                    <option value="PHOTO_EQUIPMENT" <%="PHOTO_EQUIPMENT".equals(category) ? "selected" : ""%>>Thiết bị chụp ảnh</option>
                                    <option value="AUDIO_EQUIPMENT" <%="AUDIO_EQUIPMENT".equals(category) ? "selected" : ""%>>Thiết bị âm thanh</option>
                                    <option value="ACCESSORIES" <%="ACCESSORIES".equals(category) ? "selected" : ""%>>Phụ kiện tác nghiệp</option>
                                    <option value="IT_EQUIPMENT" <%="IT_EQUIPMENT".equals(category) ? "selected" : ""%>>Thiết bị công nghệ</option>
                                    <option value="VEHICLE" <%="VEHICLE".equals(category) ? "selected" : ""%>>Phương tiện vận chuyển</option>
                                </select>
                            </div>

                            <div class="field">
                                <label for="assetStatus">Trạng thái vận hành <span class="req">*</span></label>
                                <% if (isCheckedOut) { %>
                                    <!-- Đang bàn giao: không cho phép tự đổi trạng thái bằng form sửa -->
                                    <input type="hidden" name="assetStatus" value="CHECKED_OUT">
                                    <input type="text"
                                           disabled
                                           value="Đang bàn giao (Cập nhật qua luồng thu hồi)"
                                           style="background: #edf2f7; color: #2861b2; font-weight: bold;">
                                    <small class="hint" style="color: #2861b2;">
                                        Tài sản đang được bàn giao phục vụ tác nghiệp. Để chuyển trạng thái, vui lòng thực hiện xác nhận nhận lại (thu hồi) tại phiếu yêu cầu.
                                    </small>
                                <% } else { %>
                                    <select id="assetStatus" name="assetStatus" required>
                                        <option value="AVAILABLE" <%="AVAILABLE".equals(status) ? "selected" : ""%>>Sẵn sàng (AVAILABLE)</option>
                                        <option value="MAINTENANCE" <%="MAINTENANCE".equals(status) ? "selected" : ""%>>Đang bảo trì (MAINTENANCE)</option>
                                    </select>
                                    <small class="hint">Tài sản mới tạo hoặc chỉnh sửa có thể chọn Sẵn sàng hoặc Bảo trì.</small>
                                <% } %>
                            </div>

                            <div class="field full">
                                <label for="serialNumber">Số serial / Biển số</label>
                                <input id="serialNumber"
                                       name="serialNumber"
                                       type="text"
                                       maxlength="100"
                                       value="<%=esc(asset.getSerialNumber())%>"
                                       placeholder="Số serial kỹ thuật hoặc biển số đăng ký...">
                            </div>

                            <div class="field full">
                                <label for="description">Mô tả chi tiết / Tình trạng kỹ thuật</label>
                                <textarea id="description"
                                          name="description"
                                          maxlength="500"
                                          placeholder="Ghi chú chi tiết về phụ kiện đi kèm, tình trạng máy, lưu ý khi tác nghiệp..."><%=esc(asset.getDescription())%></textarea>
                                <small class="hint">Tối đa 500 ký tự theo chuẩn cơ sở dữ liệu.</small>
                            </div>
                        </div>

                        <div class="actions" style="margin-top: 20px;">
                            <a class="btn" href="<%=ctx%>/assets">Hủy</a>
                            <button class="btn primary" type="submit">
                                💾 <%=editing ? "Lưu thay đổi" : "Lưu tài sản"%>
                            </button>
                        </div>
                    </form>
                </section>

                <aside>
                    <div class="info-card">
                        <div class="eyebrow">QUY TẮC NGHIỆP VỤ HẬU CẦN</div>
                        <h2>Lưu ý nhập liệu</h2>
                        <div class="info-item">
                            <b>Mã tài sản</b>
                            <small>Phải là mã duy nhất trong kho, viết liền không dấu, ví dụ: CAM-FX3-01.</small>
                        </div>
                        <div class="info-item">
                            <b>Trạng thái bàn giao</b>
                            <small>Trạng thái 'Đang bàn giao' được hệ thống tự động cập nhật khi điều phối bàn giao tài sản, không thể chọn thủ công tại đây.</small>
                        </div>
                        <div class="info-item">
                            <b>Bảo trì</b>
                            <small>Khi đưa vào bảo trì, hệ thống sẽ kiểm tra để đảm bảo tài sản không có lịch mượn nào đang chờ duyệt hoặc đang thực thi.</small>
                        </div>
                    </div>
                </aside>
            </div>
        </main>
    </section>
</div>

<script src="<%=ctx%>/assets/js/asset-management.js"></script>
</body>
</html>
