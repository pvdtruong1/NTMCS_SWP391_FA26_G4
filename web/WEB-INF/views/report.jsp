<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Performance Report</title>
        <link rel="stylesheet" href="${base}/css/style.css">
    </head>
    <body>
        <jsp:include page="menu.jsp" />
        <div class="content">
            <h2>Performance report builder</h2>
            <p>Pick how to group the topics, a deadline range and the columns, then build or export the report.</p>

            <c:if test="${not empty error}">
                <p class="error"><c:out value="${error}" /></p>
            </c:if>

            <form method="get" action="${base}/report">
                <label>Group by</label>
                <select name="group">
                    <c:forEach var="g" items="${groups}">
                        <option value="${g[0]}" ${g[0] == report.groupBy ? 'selected' : ''}>${g[1]}</option>
                    </c:forEach>
                </select>

                <label>Deadline from / to (optional)</label>
                <input type="date" name="from" value="<c:out value='${report.fromText}' />">
                <input type="date" name="to" value="<c:out value='${report.toText}' />">

                <label>Desk</label>
                <select name="desk_id">
                    <option value="0">-- All desks --</option>
                    <c:forEach var="d" items="${deskList}">
                        <option value="${d.deskId}" ${d.deskId == report.deskId ? 'selected' : ''}><c:out value="${d.deskName}" /></option>
                    </c:forEach>
                </select>

                <label>Columns</label>
                <c:forEach var="m" items="${metrics}">
                    <span style="display:inline-block; margin-right:14px">
                        <input type="checkbox" name="metric" value="${m[0]}" id="m_${m[0]}"
                               ${report.metrics.contains(m[0]) ? 'checked' : ''}>
                        <label for="m_${m[0]}" style="display:inline; font-weight:normal">${m[1]}</label>
                    </span>
                </c:forEach>

                <p>
                    <button type="submit">Build report</button>
                    <button type="submit" name="format" value="xlsx">Export Excel</button>
                    <button type="submit" name="format" value="print" formtarget="_blank">Export PDF (print)</button>
                    <a href="${base}/report">Reset</a>
                </p>
            </form>

            <c:if test="${not empty result}">
                <h3>Result</h3>
                <p style="color:#555"><c:out value="${result.description}" />. Draft topics are not counted.</p>
                <c:choose>
                    <c:when test="${empty result.rows}">
                        <p>No topics match these filters.</p>
                    </c:when>
                    <c:otherwise>
                        <table>
                            <tr>
                                <th>${report.groupLabel}</th>
                                <c:forEach var="col" items="${report.metricColumns}"><th>${col[1]}</th></c:forEach>
                            </tr>
                            <c:forEach var="row" items="${result.rows}">
                                <tr>
                                    <td><c:out value="${row.group_label}" /></td>
                                    <c:forEach var="col" items="${report.metricColumns}">
                                        <td><c:out value="${row[col[0]]}" default="-" /></td>
                                    </c:forEach>
                                </tr>
                            </c:forEach>
                            <tr>
                                <th>Total</th>
                                <c:forEach var="col" items="${report.metricColumns}">
                                    <th><c:out value="${result.total[col[0]]}" default="-" /></th>
                                </c:forEach>
                            </tr>
                        </table>
                    </c:otherwise>
                </c:choose>
            </c:if>
        </div>
    </body>
</html>
