<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Home</title>
        <link rel="stylesheet" href="${base}/css/style.css">
    </head>
    <body>
        <jsp:include page="menu.jsp" />
        <div class="content">
            <h2>Welcome, <c:out value="${sessionScope.loggedUser.fullName}" /></h2>
            <p>Role: ${sessionScope.loggedUser.roleName}</p>

            <c:choose>
                <c:when test="${empty screens}">
                    <p>No screens are assigned to your role yet.</p>
                </c:when>
                <c:otherwise>
                    <p>You can open:</p>
                    <ul>
                        <c:forEach var="s" items="${screens}">
                            <li><a href="${base}${s[1]}">${s[0]}</a></li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </div>
    </body>
</html>
