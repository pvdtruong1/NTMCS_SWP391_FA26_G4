<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Desk Quotas</title>
        <link rel="stylesheet" href="${base}/css/style.css">
    </head>
    <body>
        <jsp:include page="menu.jsp" />
        <div class="content">
            <h2>Desk quotas</h2>
            <p>How many topics each desk is expected to produce in a period.</p>

            <c:if test="${param.msg == 'created'}"><p class="ok">Quota created.</p></c:if>
            <c:if test="${param.msg == 'updated'}"><p class="ok">Quota updated.</p></c:if>
            <c:if test="${param.msg == 'deleted'}"><p class="ok">Quota deleted.</p></c:if>
            <c:if test="${param.msg == 'nochange'}"><p class="ok">No changes were made.</p></c:if>

            <p><a class="button" href="${base}/quota?action=new">+ Add quota</a></p>

            <c:choose>
                <c:when test="${empty quotaList}">
                    <p>No quotas yet.</p>
                </c:when>
                <c:otherwise>
                    <table>
                        <tr>
                            <th>Desk</th>
                            <th>Period</th>
                            <th>Quota (topics)</th>
                            <th>Actions</th>
                        </tr>
                        <c:forEach var="q" items="${quotaList}">
                            <tr>
                                <td><c:out value="${q.deskName}" /></td>
                                <td><c:out value="${q.period}" /></td>
                                <td>${q.quotaCount}</td>
                                <td>
                                    <a href="${base}/quota?action=edit&id=${q.quotaId}">Edit</a>
                                    <form method="post" action="${base}/quota" style="display:inline"
                                          onsubmit="return confirm('Delete this quota?');">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${q.quotaId}">
                                        <button type="submit">Delete</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:otherwise>
            </c:choose>
        </div>
    </body>
</html>
