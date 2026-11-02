<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="model.AssetRequest"%>
<%@page import="java.util.List"%>
<%@page import="java.text.SimpleDateFormat"%>

<%
    AssetRequest item =
        (AssetRequest) request.getAttribute("assetRequest");

    List<AssetRequest> conflicts =
        (List<AssetRequest>) request.getAttribute("conflicts");

    Boolean hasConflict =
        (Boolean) request.getAttribute("hasConflict");

    if (item == null) {
        response.sendError(404, "Không tìm thấy yêu cầu.");
        return;
    }

    String ctx = request.getContextPath();

    SimpleDateFormat dateFormat =
        new SimpleDateFormat("dd/MM/yyyy HH:mm");

    boolean conflict = Boolean.TRUE.equals(hasConflict);
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kiểm tra lịch tài sản</title>

    <link rel="stylesheet"
          href="<%=ctx%>/assets/css/asset-management.css">

    <style>
        .schedule-summary {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 15px;
            margin-bottom: 20px;
        }

        .schedule-item {
            padding: 17px;
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 9px;
        }

        .schedule-item span {
            display: block;
            color: #64748b;
            font-size: 13px;
            margin-bottom: 7px;
        }

        .schedule-item strong {
            color: #1e293b;
        }

        .conflict-result {
            padding: 16px;
            border-radius: 9px;
            margin-bottom: 20px;
        }

        .conflict-result.safe {
            background: #d1e7dd;
            color: #0f5132;
        }

        .conflict-result.danger {
            background: #f8d7da;
            color: #842029;
        }

        .schedule-actions {
            display: flex;
            gap: 10px;
            margin-top: 20px;
        }

        .table-wrap table {
            min-width: 850px;
            width: 100%;
        }

        @media(max-width: 650px) {
            .schedule-summary {
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
            <strong>Kiểm tra lịch tài sản</strong>
        </header>

        <main>

            <div class="head">
                <div>
                    <div class="eyebrow">ASSET REQUEST / SCHEDULE</div>
                    <h2>Kiểm tra trùng lịch</h2>
                    <p>Kiểm tra tài sản có yêu cầu khác trong cùng khoảng thời gian hay không.</p>
                </div>
            </div>

            <section class="card">

                <div class="card-head">
                    <h3>Yêu cầu đang kiểm tra #<%=item.getRequestId()%></h3>
                </div>

                <div class="schedule-summary">

                    <div class="schedule-item">
                        <span>Tài sản</span>
                        <strong><%=item.getAssetName()%></strong>
                    </div>

                    <div class="schedule-item">
                        <span>Đề tài</span>
                        <strong><%=item.getTopicTitle()%></strong>
                    </div>

                    <div class="schedule-item">
                        <span>Bắt đầu</span>
                        <strong><%=dateFormat.format(item.getStartTime())%></strong>
                    </div>

                    <div class="schedule-item">
                        <span>Kết thúc</span>
                        <strong><%=dateFormat.format(item.getEndTime())%></strong>
                    </div>

                </div>

                <% if (conflict) { %>

                    <div class="conflict-result danger">
                        <strong>Phát hiện xung đột lịch!</strong>
                        <p>
                            Tài sản này đang có
                            <%=conflicts.size()%> yêu cầu khác
                            trong cùng khoảng thời gian.
                            Không nên duyệt yêu cầu này.
                        </p>
                    </div>

                <% } else { %>

                    <div class="conflict-result safe">
                        <strong>Không phát hiện xung đột lịch.</strong>
                        <p>
                            Hiện tại không có yêu cầu đang chiếm lịch
                            của tài sản trong khoảng thời gian này.
                        </p>
                    </div>

                <% } %>

                <div class="card-head">
                    <h3>Các yêu cầu bị trùng lịch</h3>
                </div>

                <div class="table-wrap">
                    <table>
                        <thead>
                        <tr>
                            <th>Mã yêu cầu</th>
                            <th>Đề tài</th>
                            <th>Người yêu cầu</th>
                            <th>Bắt đầu</th>
                            <th>Kết thúc</th>
                            <th>Trạng thái</th>
                            <th>Chi tiết</th>
                        </tr>
                        </thead>

                        <tbody>

                        <% if (conflicts == null || conflicts.isEmpty()) { %>

                            <tr>
                                <td colspan="7"
                                    style="text-align:center;padding:25px;">
                                    Không có yêu cầu nào bị trùng lịch.
                                </td>
                            </tr>

                        <% } else {
                            for (AssetRequest other : conflicts) {
                        %>

                            <tr>
                                <td>#<%=other.getRequestId()%></td>

                                <td><%=other.getTopicTitle()%></td>

                                <td><%=other.getRequesterName()%></td>

                                <td><%=dateFormat.format(other.getStartTime())%></td>

                                <td><%=dateFormat.format(other.getEndTime())%></td>

                                <td>
                                    <span class="request-status <%=other.getStatus()%>">
                                        <%=other.getStatus()%>
                                    </span>
                                </td>

                                <td>
                                    <a href="<%=ctx%>/asset-requests?action=detail&id=<%=other.getRequestId()%>">
                                        Xem chi tiết
                                    </a>
                                </td>
                            </tr>

                        <%  }
                           } %>

                        </tbody>
                    </table>
                </div>

                <div class="schedule-actions">

                    <a class="btn"
                       href="<%=ctx%>/asset-requests">
                        Quay lại danh sách
                    </a>

                    <a class="btn"
                       href="<%=ctx%>/asset-requests?action=detail&id=<%=item.getRequestId()%>">
                        Xem chi tiết yêu cầu
                    </a>

                </div>

            </section>
        </main>
    </div>
</div>
</body>
</html>