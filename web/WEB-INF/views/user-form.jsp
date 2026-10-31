<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${editUser.userId > 0}" />
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>User Form</title>
        <link rel="stylesheet" href="${base}/css/style.css">
    </head>
    <body>
        <jsp:include page="menu.jsp" />
        <div class="content">
            <h2>${isEdit ? 'Edit account' : 'Add account'}</h2>

            <c:if test="${not empty error}">
                <p class="error"><c:out value="${error}" /></p>
            </c:if>

            <form method="post" action="${base}/users">
                <input type="hidden" name="action" value="${isEdit ? 'update' : 'create'}">
                <input type="hidden" name="id" value="${editUser.userId}">

                <label>Full name</label>
                <input type="text" name="fullName" value="<c:out value='${editUser.fullName}' />">

                <label>Email</label>
                <input type="text" name="email" value="<c:out value='${editUser.email}' />" ${isEdit ? 'readonly' : ''}>

                <label>${isEdit ? 'New password (leave blank to keep the old one)' : 'Password (min 6 characters)'}</label>
                <input type="password" name="password">

                <label>Role</label>
                <select name="roleId">
                    <c:forEach var="r" items="${roleList}">
                        <option value="${r.roleId}" ${r.roleId == editUser.roleId ? 'selected' : ''}>${r.roleName}</option>
                    </c:forEach>
                </select>

                <label>Desk</label>
                <select name="deskId">
                    <option value="">-- None --</option>
                    <c:forEach var="d" items="${deskList}">
                        <option value="${d.deskId}" ${d.deskId == editUser.deskId ? 'selected' : ''}><c:out value="${d.deskName}" /></option>
                    </c:forEach>
                </select>

                <p>
                    <button type="submit">Save</button>
                    <a href="${base}/users">Cancel</a>
                </p>
            </form>
        </div>
    </body>
</html>
