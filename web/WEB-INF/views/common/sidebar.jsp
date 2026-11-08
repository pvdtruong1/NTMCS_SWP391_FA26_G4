<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
String wf03Active = (String) request.getAttribute("wf03Active");
if (wf03Active == null) {
    wf03Active = "";
}

String reqStatus = request.getParameter("status");
if (reqStatus == null) {
    reqStatus = "";
}

String ctx = request.getContextPath();
%>

<aside id="sidebar" class="sidebar">

    <a class="brand" href="<%=ctx%>/assets">
        <span class="brand-mark">N</span>
        <span>
            <b>NTMCS</b>
            <small>Newsroom Management</small>
        </span>
    </a>

    <div class="nav-label">QUẢN LÝ TÀI SẢN</div>

    <a class="nav-link <%="assets".equals(wf03Active) ? "active" : ""%>"
       href="<%=ctx%>/assets">
        <span>📦</span>
        Danh sách tài sản
    </a>

    <a class="nav-link <%="add".equals(wf03Active) ? "active" : ""%>"
       href="<%=ctx%>/assets?action=add">
        <span>➕</span>
        Thêm tài sản mới
    </a>

    <div class="nav-label">YÊU CẦU TÀI SẢN</div>

    <a class="nav-link <%="requests".equals(wf03Active) && reqStatus.isEmpty() ? "active" : ""%>"
       href="<%=ctx%>/asset-requests">
        <span>📋</span>
        Tất cả yêu cầu
    </a>

    <a class="nav-link <%="PENDING".equalsIgnoreCase(reqStatus) ? "active" : ""%>"
       href="<%=ctx%>/asset-requests?status=PENDING">
        <span>⏳</span>
        Chờ duyệt
    </a>

    <a class="nav-link <%="APPROVED".equalsIgnoreCase(reqStatus) ? "active" : ""%>"
       href="<%=ctx%>/asset-requests?status=APPROVED">
        <span>🚚</span>
        Chờ bàn giao
    </a>

    <a class="nav-link <%="CHECKED_OUT".equalsIgnoreCase(reqStatus) ? "active" : ""%>"
       href="<%=ctx%>/asset-requests?status=CHECKED_OUT">
        <span>🔄</span>
        Đang bàn giao / Thu hồi
    </a>

    <div class="sidebar-bottom">
        <span class="demo">SWP391 — NHÓM 4</span>
        <small>Tòa Soạn Hội Tụ (NTMCS)</small>
    </div>

</aside>