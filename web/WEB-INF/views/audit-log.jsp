<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>System Audit Log</title>
        <link rel="stylesheet" href="${base}/css/style.css">
    </head>
    <body>
        <jsp:include page="menu.jsp" />
        <div class="content">
            <h2>System Audit Log</h2>

            <form method="get" action="${base}/audit-log">
                <input type="text" name="keyword" placeholder="Search who / action / details"
                       value="<c:out value='${keyword}' />">
                <button type="submit">Search</button>
                <a href="${base}/audit-log">Clear</a>
            </form>
            <p>Showing the newest 200 entries.</p>

            <table>
                <tr>
                    <th>Time</th>
                    <th>Who</th>
                    <th>Action</th>
                    <th>On</th>
                    <th>Details</th>
                </tr>
                <c:forEach var="log" items="${logList}">
                    <tr>
                        <td>${log.createdAt}</td>
                        <td><c:out value="${log.actorName}" /></td>
                        <td>${log.action}</td>
                        <td>${log.entityType} <c:if test="${log.entityId != null}">#${log.entityId}</c:if></td>
                        <td><c:out value="${log.details}" /></td>
                    </tr>
                </c:forEach>
            </table>
        </div>
    </body>
</html>
