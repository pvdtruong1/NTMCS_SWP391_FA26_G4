<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>User Accounts</title>
        <link rel="stylesheet" href="${base}/css/style.css">
    </head>
    <body>
        <jsp:include page="menu.jsp" />
        <div class="content">
            <h2>User Accounts</h2>

            <c:if test="${param.msg == 'created'}"><p class="ok">Account created.</p></c:if>
            <c:if test="${param.msg == 'updated'}"><p class="ok">Account updated.</p></c:if>
            <c:if test="${param.msg == 'locked'}"><p class="ok">Account locked.</p></c:if>
            <c:if test="${param.msg == 'unlocked'}"><p class="ok">Account unlocked.</p></c:if>
            <c:if test="${param.msg == 'selflock'}"><p class="error">You cannot lock your own account.</p></c:if>

            <p><a class="button" href="${base}/users?action=new">+ Add account</a></p>

            <table>
                <tr>
                    <th>ID</th>
                    <th>Full name</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Desk</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
                <c:forEach var="u" items="${userList}">
                    <tr>
                        <td>${u.userId}</td>
                        <td><c:out value="${u.fullName}" /></td>
                        <td><c:out value="${u.email}" /></td>
                        <td>${u.roleName}</td>
                        <td><c:out value="${u.deskName}" /></td>
                        <td>
                            <c:choose>
                                <c:when test="${u.activeStatus}">Active</c:when>
                                <c:otherwise>Locked</c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <a href="${base}/users?action=edit&id=${u.userId}">Edit</a>
                            <form method="post" action="${base}/users" style="display:inline">
                                <input type="hidden" name="id" value="${u.userId}">
                                <c:choose>
                                    <c:when test="${u.activeStatus}">
                                        <input type="hidden" name="action" value="lock">
                                        <button type="submit">Lock</button>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="hidden" name="action" value="unlock">
                                        <button type="submit">Unlock</button>
                                    </c:otherwise>
                                </c:choose>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </div>
    </body>
</html>
