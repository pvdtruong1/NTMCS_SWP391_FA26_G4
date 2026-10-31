<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${quota.quotaId > 0}" />
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Quota Form</title>
        <link rel="stylesheet" href="${base}/css/style.css">
    </head>
    <body>
        <jsp:include page="menu.jsp" />
        <div class="content">
            <h2>${isEdit ? 'Edit quota' : 'Add quota'}</h2>

            <c:if test="${not empty error}">
                <p class="error"><c:out value="${error}" /></p>
            </c:if>

            <form method="post" action="${base}/quota">
                <input type="hidden" name="action" value="${isEdit ? 'update' : 'create'}">
                <input type="hidden" name="id" value="${quota.quotaId}">

                <label>Desk</label>
                <c:choose>
                    <c:when test="${isEdit}">
                        <input type="text" value="<c:out value='${quota.deskName}' />" readonly>
                    </c:when>
                    <c:otherwise>
                        <select name="deskId">
                            <option value="">-- Choose a desk --</option>
                            <c:forEach var="d" items="${deskList}">
                                <option value="${d.deskId}" ${d.deskId == quota.deskId ? 'selected' : ''}><c:out value="${d.deskName}" /></option>
                            </c:forEach>
                        </select>
                    </c:otherwise>
                </c:choose>

                <label>Period</label>
                <input type="text" name="period" value="<c:out value='${quota.period}' />"
                       placeholder="2026-10 or 2026-Q4" ${isEdit ? 'readonly' : ''}>

                <label>Quota (number of topics)</label>
                <input type="text" name="quota" value="<c:out value='${countText}' />">

                <p>
                    <button type="submit">Save</button>
                    <a href="${base}/quota">Cancel</a>
                </p>
            </form>
            <p style="color:#555">Period is a month (2026-10) or a quarter (2026-Q4). The quota must be a whole number, 0 or more.</p>
        </div>
    </body>
</html>
