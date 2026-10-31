<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<div class="menu">
    <span class="right">
        <c:out value="${sessionScope.loggedUser.fullName}" /> |
        <a href="${base}/logout">Logout</a>
    </span>
    <b>NTMCS</b>
    <a href="${base}/home">Home</a>
    <c:if test="${not empty sessionScope.permissions and sessionScope.permissions.contains('USER_MANAGE')}">
        <a href="${base}/users">Users</a>
    </c:if>
    <c:if test="${not empty sessionScope.permissions and sessionScope.permissions.contains('RBAC_MANAGE')}">
        <a href="${base}/rbac">RBAC</a>
    </c:if>
    <c:if test="${not empty sessionScope.permissions and sessionScope.permissions.contains('AUDIT_VIEW')}">
        <a href="${base}/audit-log">Audit Log</a>
    </c:if>
    <c:if test="${not empty sessionScope.permissions and sessionScope.permissions.contains('QUOTA_MANAGE')}">
        <a href="${base}/dashboard">Dashboard</a>
        <a href="${base}/report">Reports</a>
        <a href="${base}/quota">Quotas</a>
        <a href="${base}/criteria">Criteria</a>
        <a href="${base}/categories">Categories</a>
    </c:if>
</div>
