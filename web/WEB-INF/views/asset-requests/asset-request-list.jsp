<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="model.AssetRequest"%>
<%@page import="java.text.SimpleDateFormat"%>

<%
    List<AssetRequest> requests =
        (List<AssetRequest>) request.getAttribute("requests");

    String keyword = (String) request.getAttribute("keyword");
    String status = (String) request.getAttribute("status");
    String fromDate = (String) request.getAttribute("fromDate");
    String toDate = (String) request.getAttribute("toDate");

    int pendingCount = (Integer) request.getAttribute("pendingCount");
    int approvedCount = (Integer) request.getAttribute("approvedCount");
    int rejectedCount = (Integer) request.getAttribute("rejectedCount");

    int totalRecords = (Integer) request.getAttribute("totalRecords");
    int currentPage = (Integer) request.getAttribute("currentPage");
    int totalPages = (Integer) request.getAttribute("totalPages");

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    String ctx = request.getContextPath();
    String message = request.getParameter("message");
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý yêu cầu tài sản</title>

    <link rel="stylesheet"
          href="<%=ctx%>/assets/css/asset-management.css">

    <style>
        .request-status {
            display: inline-block;
            padding: 5px 11px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
        }

        .request-status.PENDING {
            background: #fff3cd;
            color: #856404;
        }

        .request-status.APPROVED {
            background: #d1e7dd;
            color: #0f5132;
        }

        .request-status.REJECTED {
            background: #f8d7da;
            color: #842029;
        }

        .request-status.CHECKED_OUT {
            background: #cfe2ff;
            color: #084298;
        }

        .request-status.RETURNED {
            background: #e2e3e5;
            color: #41464b;
        }

        .filter-form {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            align-items: end;
        }

        .filter-form .field {
            display: flex;
            flex-direction: column;
            gap: 5px;
        }

        .filter-form input,
        .filter-form select {
            min-height: 38px;
            padding: 7px 10px;
            border: 1px solid #d7dce2;
            border-radius: 7px;
        }

        .btn-detail {
            color: #2563eb;
            text-decoration: none;
            font-weight: 600;
        }

        .btn-schedule {
            color: #0f766e;
            text-decoration: none;
            font-weight: 600;
        }

        .pagination {
            display: flex;
            justify-content: flex-end;
            align-items: center;
            gap: 7px;
            margin-top: 20px;
        }

        .pagination a,
        .pagination span {
            padding: 7px 12px;
            border: 1px solid #ddd;
            border-radius: 6px;
            text-decoration: none;
            color: #333;
        }

        .pagination .active {
            background: #2563eb;
            color: white;
            border-color: #2563eb;
        }

        .modal-overlay {
            display: none;
            position: fixed;
            inset: 0;
            z-index: 9999;
            background: rgba(15, 23, 42, .55);
            align-items: center;
            justify-content: center;
            padding: 20px;
        }

        .modal-overlay.show {
            display: flex;
        }

        .confirm-modal {
            width: 100%;
            max-width: 430px;
            background: white;
            border-radius: 14px;
            padding: 25px;
            box-shadow: 0 20px 60px rgba(0,0,0,.2);
            animation: modalIn .18s ease-out;
        }

        @keyframes modalIn {
            from {
                opacity: 0;
                transform: translateY(10px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .confirm-modal h3 {
            margin: 0 0 12px;
            font-size: 20px;
        }

        .confirm-modal p {
            color: #64748b;
            line-height: 1.6;
        }

        .modal-actions {
            display: flex;
            justify-content: flex-end;
            gap: 10px;
            margin-top: 24px;
        }

        .modal-actions button {
            border: 0;
            border-radius: 7px;
            padding: 10px 18px;
            cursor: pointer;
            font-weight: 600;
        }

        .modal-cancel {
            background: #e5e7eb;
            color: #374151;
        }

        .modal-approve {
            background: #16a34a;
            color: white;
        }

        .modal-reject {
            background: #dc2626;
            color: white;
        }

        .alert-message {
            padding: 12px 15px;
            border-radius: 8px;
            margin-bottom: 15px;
        }

        .alert-message.success {
            background: #d1e7dd;
            color: #0f5132;
        }

        .alert-message.error {
            background: #f8d7da;
            color: #842029;
        }

        .table-wrap table {
            min-width: 1050px;
            width: 100%;
        }

        .action-buttons {
            display: flex;
            flex-wrap: wrap;
            gap: 9px;
            align-items: center;
        }

        .action-buttons form {
            display: inline;
        }

        .action-buttons button {
            cursor: pointer;
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

        <div class="side-foot">
            Logistics Management
        </div>
    </aside>

    <div class="main">

        <header class="topbar">
            <div>
                <strong>Quản lý yêu cầu tài sản</strong>
            </div>
        </header>

        <main>

            <div class="head">
                <div>
                    <div class="eyebrow">LOGISTICS / ITERATION 2</div>
                    <h2>Danh sách yêu cầu tài sản</h2>
                    <p>Tiếp nhận và xử lý yêu cầu sử dụng thiết bị, phương tiện.</p>
                </div>
            </div>

            <% if ("approved".equals(message)) { %>
                <div class="alert-message success">
                    Đã duyệt yêu cầu thành công.
                </div>
            <% } else if ("rejected".equals(message)) { %>
                <div class="alert-message success">
                    Đã từ chối yêu cầu thành công.
                </div>
            <% } else if ("conflict".equals(message)) { %>
                <div class="alert-message error">
                    Không thể duyệt vì tài sản đang có yêu cầu trùng lịch.
                    Hãy kiểm tra lịch trước khi duyệt.
                </div>
            <% } else if ("notpending".equals(message)) { %>
                <div class="alert-message error">
                    Yêu cầu không còn ở trạng thái chờ xử lý.
                </div>
            <% } else if ("notfound".equals(message)) { %>
                <div class="alert-message error">
                    Không tìm thấy yêu cầu.
                </div>
            <% } else if ("error".equals(message)) { %>
                <div class="alert-message error">
                    Có lỗi xảy ra khi xử lý yêu cầu.
                </div>
            <% } %>

            <section class="stats">
                <div class="stat">
                    <span>Chờ xử lý</span>
                    <h3><%=pendingCount%></h3>
                </div>

                <div class="stat">
                    <span>Đã duyệt</span>
                    <h3><%=approvedCount%></h3>
                </div>

                <div class="stat">
                    <span>Đã từ chối</span>
                    <h3><%=rejectedCount%></h3>
                </div>
            </section>

            <section class="card">

                <div class="card-head">
                    <div>
                        <h3>Danh sách yêu cầu</h3>
                        <span class="count">
                            Tổng cộng: <%=totalRecords%> yêu cầu
                        </span>
                    </div>
                </div>

                <form class="filter-form" method="get"
                      action="<%=ctx%>/asset-requests">

                    <div class="field">
                        <label>Tìm kiếm</label>
                        <input type="text"
                               name="keyword"
                               value="<%=keyword == null ? "" : keyword%>"
                               placeholder="Đề tài, tài sản, người yêu cầu...">
                    </div>

                    <div class="field">
                        <label>Trạng thái</label>
                        <select name="status">
                            <option value="ALL"
                                <%="ALL".equals(status) ? "selected" : ""%>>
                                Tất cả
                            </option>

                            <option value="PENDING"
                                <%="PENDING".equals(status) ? "selected" : ""%>>
                                Chờ xử lý
                            </option>

                            <option value="APPROVED"
                                <%="APPROVED".equals(status) ? "selected" : ""%>>
                                Đã duyệt
                            </option>

                            <option value="REJECTED"
                                <%="REJECTED".equals(status) ? "selected" : ""%>>
                                Đã từ chối
                            </option>

                            <option value="CHECKED_OUT"
                                <%="CHECKED_OUT".equals(status) ? "selected" : ""%>>
                                Đang sử dụng
                            </option>

                            <option value="RETURNED"
                                <%="RETURNED".equals(status) ? "selected" : ""%>>
                                Đã trả
                            </option>
                        </select>
                    </div>

                    <div class="field">
                        <label>Từ ngày</label>
                        <input type="date" name="fromDate"
                               value="<%=fromDate == null ? "" : fromDate%>">
                    </div>

                    <div class="field">
                        <label>Đến ngày</label>
                        <input type="date" name="toDate"
                               value="<%=toDate == null ? "" : toDate%>">
                    </div>

                    <button class="btn primary" type="submit">
                        Tìm kiếm
                    </button>

                    <a class="btn" href="<%=ctx%>/asset-requests">
                        Xóa lọc
                    </a>
                </form>

                <div class="table-wrap" style="margin-top:20px;">
                    <table>
                        <thead>
                        <tr>
                            <th>Mã yêu cầu</th>
                            <th>Đề tài</th>
                            <th>Tài sản</th>
                            <th>Người yêu cầu</th>
                            <th>Thời gian bắt đầu</th>
                            <th>Thời gian kết thúc</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                        </thead>

                        <tbody>

                        <% if (requests == null || requests.isEmpty()) { %>
                            <tr>
                                <td colspan="8" style="text-align:center;padding:30px;">
                                    Không tìm thấy yêu cầu tài sản.
                                </td>
                            </tr>
                        <% } else {
                            for (AssetRequest item : requests) {
                        %>

                            <tr>
                                <td>#<%=item.getRequestId()%></td>

                                <td>
                                    <strong><%=item.getTopicTitle()%></strong>
                                    <br>
                                    <small><%=item.getTopicCode() == null
                                        ? "" : item.getTopicCode()%></small>
                                </td>

                                <td><%=item.getAssetName()%></td>

                                <td>
                                    <%=item.getRequesterName()%>
                                    <br>
                                    <small><%=item.getRequesterEmail() == null
                                        ? "" : item.getRequesterEmail()%></small>
                                </td>

                                <td><%=dateFormat.format(item.getStartTime())%></td>

                                <td><%=dateFormat.format(item.getEndTime())%></td>

                                <td>
                                    <span class="request-status <%=item.getStatus()%>">
                                        <%=item.getStatus()%>
                                    </span>
                                </td>

                                <td>
                                    <div class="action-buttons">

                                        <a class="btn-detail"
                                           href="<%=ctx%>/asset-requests?action=detail&id=<%=item.getRequestId()%>">
                                            Chi tiết
                                        </a>

                                        <a class="btn-schedule"
                                           href="<%=ctx%>/asset-requests?action=schedule&id=<%=item.getRequestId()%>">
                                            Kiểm tra lịch
                                        </a>

                                        <% if ("PENDING".equals(item.getStatus())) { %>

                                            <button type="button"
                                                    class="btn small primary"
                                                    onclick="openDecisionModal('approve', <%=item.getRequestId()%>)">
                                                Duyệt
                                            </button>

                                            <button type="button"
                                                    class="btn small danger"
                                                    onclick="openDecisionModal('reject', <%=item.getRequestId()%>)">
                                                Từ chối
                                            </button>

                                        <% } %>

                                    </div>
                                </td>
                            </tr>

                        <%  }
                           } %>

                        </tbody>
                    </table>
                </div>

                <div class="pagination">

                    <% if (currentPage > 1) { %>
                        <a href="<%=ctx%>/asset-requests?keyword=<%=keyword == null ? "" : java.net.URLEncoder.encode(keyword, "UTF-8")%>&status=<%=status%>&fromDate=<%=fromDate%>&toDate=<%=toDate%>&page=<%=currentPage - 1%>">
                            Trước
                        </a>
                    <% } %>

                    <span class="active">
                        <%=currentPage%> / <%=totalPages%>
                    </span>

                    <% if (currentPage < totalPages) { %>
                        <a href="<%=ctx%>/asset-requests?keyword=<%=keyword == null ? "" : java.net.URLEncoder.encode(keyword, "UTF-8")%>&status=<%=status%>&fromDate=<%=fromDate%>&toDate=<%=toDate%>&page=<%=currentPage + 1%>">
                            Tiếp
                        </a>
                    <% } %>

                </div>

            </section>
        </main>
    </div>
</div>

<!-- MODAL XÁC NHẬN -->
<div class="modal-overlay" id="decisionModal">
    <div class="confirm-modal">

        <h3 id="modalTitle">Xác nhận xử lý</h3>

        <p id="modalMessage">
            Bạn có chắc chắn muốn thực hiện thao tác này?
        </p>

        <form id="decisionForm"
              method="post"
              action="<%=ctx%>/asset-requests">

            <input type="hidden" name="id" id="decisionRequestId">
            <input type="hidden" name="action" id="decisionAction">

            <div class="modal-actions">
                <button type="button"
                        class="modal-cancel"
                        onclick="closeDecisionModal()">
                    Hủy
                </button>

                <button type="submit"
                        id="modalConfirmButton"
                        class="modal-approve">
                    Xác nhận
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    const decisionModal = document.getElementById("decisionModal");

    function openDecisionModal(action, requestId) {
        document.getElementById("decisionRequestId").value = requestId;
        document.getElementById("decisionAction").value = action;

        const title = document.getElementById("modalTitle");
        const message = document.getElementById("modalMessage");
        const confirmButton = document.getElementById("modalConfirmButton");

        if (action === "approve") {
            title.textContent = "Xác nhận duyệt yêu cầu";
            message.textContent =
                "Bạn có chắc chắn muốn duyệt yêu cầu tài sản #" +
                requestId + "? Hệ thống sẽ kiểm tra trùng lịch khi xác nhận.";

            confirmButton.textContent = "Duyệt yêu cầu";
            confirmButton.className = "modal-approve";

        } else {
            title.textContent = "Xác nhận từ chối yêu cầu";
            message.textContent =
                "Bạn có chắc chắn muốn từ chối yêu cầu tài sản #" +
                requestId + "?";

            confirmButton.textContent = "Từ chối yêu cầu";
            confirmButton.className = "modal-reject";
        }

        decisionModal.classList.add("show");
    }

    function closeDecisionModal() {
        decisionModal.classList.remove("show");
    }

    decisionModal.addEventListener("click", function(event) {
        if (event.target === decisionModal) {
            closeDecisionModal();
        }
    });

    document.addEventListener("keydown", function(event) {
        if (event.key === "Escape") {
            closeDecisionModal();
        }
    });
</script>

</body>
</html>