<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Login</title>
        <link rel="stylesheet" href="${base}/css/style.css">
    </head>
    <body>
        <div class="content">
            <h2>NTMCS Login</h2>
            <c:if test="${not empty error}">
                <p class="error"><c:out value="${error}" /></p>
            </c:if>
            <form method="post" action="${base}/login">
                <label>Email</label>
                <input type="text" name="email" required>
                <label>Password</label>
                <input type="password" name="password" required>
                <p><button type="submit">Login</button></p>
            </form>
        </div>
    </body>
</html>
