<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>RBAC Matrix</title>
        <link rel="stylesheet" href="${base}/css/style.css">
    </head>
    <body>
        <jsp:include page="menu.jsp" />
        <div class="content">
            <h2>Roles and Permissions (RBAC)</h2>

            <c:if test="${param.msg == 'saved'}"><p class="ok">Permissions saved.</p></c:if>
            <c:if test="${param.msg == 'failed'}"><p class="error">Could not save. Nothing was changed.</p></c:if>

            <p>Tick the box to allow a role to open that screen.
                The SYSTEM_ADMINISTRATOR column is locked.</p>

            <form method="post" action="${base}/rbac">
                <table>
                    <tr>
                        <th>Permission</th>
                        <c:forEach var="r" items="${roleList}">
                            <th>${r.roleName}</th>
                        </c:forEach>
                    </tr>
                    <c:forEach var="p" items="${permissionList}">
                        <tr>
                            <td>${p.permissionCode}<br><small><c:out value="${p.description}" /></small></td>
                            <c:forEach var="r" items="${roleList}">
                                <c:set var="key" value="${r.roleId}-${p.permissionId}" />
                                <td>
                                    <input type="checkbox" name="rp" value="${key}"
                                           ${pairs.contains(key) ? 'checked' : ''}
                                           ${r.roleName == 'SYSTEM_ADMINISTRATOR' ? 'disabled' : ''}>
                                </td>
                            </c:forEach>
                        </tr>
                    </c:forEach>
                </table>
                <p><button type="submit">Save</button></p>
            </form>
        </div>
    </body>
</html>
