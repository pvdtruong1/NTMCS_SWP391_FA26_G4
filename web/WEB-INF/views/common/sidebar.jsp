<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- Unified Legacy Bridge: forwards to the system-wide unified sidebar --%>
<jsp:include page="/WEB-INF/views/sidebar.jsp">
    <jsp:param name="active" value="${not empty requestScope.wf03Active ? requestScope.wf03Active : 'assets'}"/>
</jsp:include>
