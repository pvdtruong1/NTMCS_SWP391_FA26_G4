<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="model.AssetRequest"%>
<%@page import="java.text.SimpleDateFormat"%>

<%
    AssetRequest item =
        (AssetRequest) request.getAttribute("assetRequest");

    if (item == null) {
        response.sendError(404, "Không tìm thấy yêu cầu.");
        return;
    }

    String ctx = request.getContextPath();

    SimpleDateFormat dateFormat =
        new SimpleDateFormat("dd/MM/yyyy HH:mm");
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết yêu cầu tài sản</title>

    <link rel="stylesheet"
          href="<%=ctx%>/assets/css/asset-management.css">

    <style>
        .detail-grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 18px;
        }

        .detail-item {
            padding: 15px;
            border: 1px solid #e5e7eb;
            border-radius: 9px;
        }

        .detail-item label {
            display: block;
            color: #64748b;
            font-size: 13px;
            margin-bottom: 8px;
        }

        .detail-item strong {
            font-size: 15px;
            color: #1e293b;
        }

        .detail-actions {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-top: 22px;
        }

        @media(max-width: 650px) {
            .detail-grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>

<body>
<div class="app">

    <aside class="sidebar">
        <div class="brand">NTMCS</div>

        <div class="nav-label">QUẢN LÝ</div>

        <a class="nav-link" href="<%=ctx%>/assets">
            Quản lý tài sản
        </a>

        <a class="nav-link active" href="<%=ctx%>/asset-requests">
            Yêu cầu tài sản
        </a>

        <div class="side-foot">Logistics Management</div>
    </aside>

    <div class="main">

        <header class="topbar">
            <strong>Chi tiết yêu cầu</strong>
        </header>

        <main>

            <div class="head">
                <div>
                    <div class="eyebrow">ASSET REQUEST / DETAIL</div>
                    <h2>Yêu cầu tài sản #<%=item.getRequestId()%></h2>
                    <p>Thông tin chi tiết yêu cầu sử dụng tài sản.</p>
                </div>
            </div>

            <section class="card">

                <div class="card-head">
                    <div>
                        <h3>Thông tin yêu cầu</h3>
                    </div>

                    <span class="request-status <%=item.getStatus()%>">
                        <%=item.getStatus()%>
                    </span>
                </div>

                <div class="detail-grid">

                    <div class="detail-item">
                        <label>Mã yêu cầu</label>
                        <strong>#<%=item.getRequestId()%></strong>
                    </div>

                    <div class="detail-item">
                        <label>Trạng thái</label>
                        <strong><%=item.getStatus()%></strong>
                    </div>

                    <div class="detail-item">
                        <label>Mã đề tài</label>
                        <strong><%=item.getTopicCode() == null
                            ? "Chưa có mã" : item.getTopicCode()%></strong>
                    </div>

                    <div class="detail-item">
                        <label>Tên đề tài</label>
                        <strong><%=item.getTopicTitle()%></strong>
                    </div>

                    <div class="detail-item">
                        <label>Tài sản yêu cầu</label>
                        <strong><%=item.getAssetName()%></strong>
                    </div>

                    <div class="detail-item">
                        <label>Mã tài sản nội bộ</label>
                        <strong><%=item.getAssetId()%></strong>
                    </div>

                    <div class="detail-item">
                        <label>Người yêu cầu</label>
                        <strong><%=item.getRequesterName()%></strong>
                    </div>

                    <div class="detail-item">
                        <label>Email người yêu cầu</label>
                        <strong><%=item.getRequesterEmail() == null
                            ? "Chưa cập nhật" : item.getRequesterEmail()%></strong>
                    </div>

                    <div class="detail-item">
                        <label>Thời gian bắt đầu</label>
                        <strong><%=dateFormat.format(item.getStartTime())%></strong>
                    </div>

                    <div class="detail-item">
                        <label>Thời gian kết thúc</label>
                        <strong><%=dateFormat.format(item.getEndTime())%></strong>
                    </div>

                </div>

                <div class="detail-actions">

                    <a class="btn"
                       href="<%=ctx%>/asset-requests">
                        Quay lại danh sách
                    </a>

                    <a class="btn primary"
                       href="<%=ctx%>/asset-requests?action=schedule&id=<%=item.getRequestId()%>">
                        Kiểm tra lịch
                    </a>

                </div>

            </section>

        </main>
    </div>
</div>
</body>
</html>