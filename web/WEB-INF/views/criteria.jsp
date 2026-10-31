<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Evaluation Criteria</title>
        <link rel="stylesheet" href="${base}/css/style.css">
    </head>
    <body>
        <jsp:include page="menu.jsp" />
        <div class="content">
            <h2>Evaluation criteria matrix</h2>
            <p>The criteria Desk Editors use to score topic pitches. The weights must add up to exactly 100%.</p>

            <c:if test="${param.msg == 'saved'}"><p class="ok">Criteria matrix saved.</p></c:if>
            <c:if test="${param.msg == 'nochange'}"><p class="ok">No changes were made.</p></c:if>
            <c:if test="${not empty error}">
                <p class="error"><c:out value="${error}" /></p>
            </c:if>
            <c:if test="${total != null}">
                <p>Current total: <b>${total}%</b></p>
            </c:if>

            <form method="post" action="${base}/criteria">
                <table>
                    <tr>
                        <th>#</th>
                        <th>Criterion name</th>
                        <th>Weight (%)</th>
                        <th>Remove</th>
                    </tr>
                    <c:forEach var="r" items="${rows}">
                        <tr>
                            <td>${r[0] == '0' ? 'new' : r[0]}</td>
                            <td>
                                <input type="hidden" name="criteriaId" value="${r[0]}">
                                <input type="text" name="criteriaName" value="<c:out value='${r[1]}' />" style="width:480px">
                            </td>
                            <td><input type="text" name="weight" value="<c:out value='${r[2]}' />" size="6"></td>
                            <td>
                                <c:if test="${r[0] != '0'}">
                                    <input type="checkbox" name="remove" value="${r[0]}" ${r[3] == '1' ? 'checked' : ''}>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
                <p><button type="submit">Save matrix</button></p>
            </form>
            <p style="color:#555">
                Fill a blank row at the bottom to add a criterion.
                A criterion already used in an evaluation cannot be removed: set its weight to 0 instead.
            </p>
        </div>
    </body>
</html>
