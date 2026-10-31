<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${desk.deskId > 0}" />
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Desk Form</title>
        <link rel="stylesheet" href="${base}/css/style.css">
    </head>
    <body>
        <jsp:include page="menu.jsp" />
        <div class="content">
            <h2>${isEdit ? 'Edit desk' : 'Add desk'}</h2>

            <c:if test="${not empty error}">
                <p class="error"><c:out value="${error}" /></p>
            </c:if>

            <form method="post" action="${base}/categories">
                <input type="hidden" name="action" value="${isEdit ? 'updateDesk' : 'createDesk'}">
                <input type="hidden" name="id" value="${desk.deskId}">

                <label>Desk name</label>
                <input type="text" name="deskName" value="<c:out value='${desk.deskName}' />">

                <p>
                    <button type="submit">Save</button>
                    <a href="${base}/categories">Cancel</a>
                </p>
            </form>
        </div>
    </body>
</html>
