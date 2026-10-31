<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Executive Dashboard</title>
        <link rel="stylesheet" href="${base}/css/style.css">
    </head>
    <body>
        <jsp:include page="menu.jsp" />
        <div class="content">
            <h2>Executive dashboard</h2>
            <p>The whole newsroom at a glance. Draft topics are not counted.</p>

            <c:if test="${not empty error}">
                <p class="error"><c:out value="${error}" /></p>
            </c:if>

            <div class="cards">
                <div class="card">
                    <div class="card-label">Topics</div>
                    <div class="card-value">${dash.total_topics}</div>
                    <div>${dash.pending_topics} waiting for review</div>
                </div>
                <div class="card">
                    <div class="card-label">Approval rate</div>
                    <div class="card-value"><c:out value="${dash.approval_rate}" default="-" />%</div>
                    <div>${dash.approved_topics} approved, ${dash.rejected_topics} rejected</div>
                </div>
                <div class="card">
                    <div class="card-label">On-time rate</div>
                    <div class="card-value"><c:out value="${dash.on_time_rate}" default="-" />%</div>
                    <div>${dash.finished_topics} finished, ${dash.overdue_count} overdue</div>
                </div>
                <div class="card">
                    <div class="card-label">Waiting for the Executive Editor</div>
                    <div class="card-value">${dash.pending_executive}</div>
                    <div>high legal risk or key topics</div>
                </div>
                <div class="card">
                    <div class="card-label">Estimated budget</div>
                    <div class="card-value"><fmt:formatNumber value="${dash.estimate_total}" groupingUsed="true" /></div>
                    <div>VND, all topics</div>
                </div>
                <div class="card">
                    <div class="card-label">Assets in the field</div>
                    <div class="card-value">${dash.assets_checked_out} / ${dash.assets_total}</div>
                    <div>${dash.assets_maintenance} in maintenance</div>
                </div>
            </div>

            <h3>Desk quota progress</h3>
            <form method="get" action="${base}/dashboard">
                Period
                <input type="text" name="period" value="<c:out value='${dash.period}' />" placeholder="2026-10 or 2026-Q4" style="width:120px">
                <button type="submit">Show</button>
            </form>
            <p style="color:#555">Approved topics whose deadline is in <c:out value="${dash.period}" />, against the desk's quota.</p>
            <table>
                <tr>
                    <th>Desk</th>
                    <th>Quota</th>
                    <th>Approved topics</th>
                    <th>Progress</th>
                </tr>
                <c:forEach var="q" items="${dash.quota_progress}">
                    <tr>
                        <td><c:out value="${q.desk_name}" /></td>
                        <td><c:out value="${q.quota_count}" default="no quota" /></td>
                        <td>${q.actual}</td>
                        <td>
                            <c:choose>
                                <c:when test="${q.percent == null}">-</c:when>
                                <c:otherwise>
                                    <span class="bar"><span style="width:${q.percent > 100 ? 100 : q.percent}%"></span></span>
                                    ${q.percent}%
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
            </table>

            <h3>Topics by status</h3>
            <table>
                <tr><th>Status</th><th>Topics</th></tr>
                <c:forEach var="s" items="${dash.status_counts}">
                    <tr><td>${s.status}</td><td>${s.count}</td></tr>
                </c:forEach>
            </table>

            <h3>Overdue topics</h3>
            <c:choose>
                <c:when test="${empty dash.overdue_topics}"><p>No topic is past its deadline.</p></c:when>
                <c:otherwise>
                    <table>
                        <tr><th>Code</th><th>Title</th><th>Desk</th><th>Reporter</th><th>Status</th><th>Deadline</th></tr>
                        <c:forEach var="t" items="${dash.overdue_topics}">
                            <tr>
                                <td><c:out value="${t.topic_code}" default="-" /></td>
                                <td><c:out value="${t.title}" /></td>
                                <td><c:out value="${t.desk_name}" /></td>
                                <td><c:out value="${t.author_name}" /></td>
                                <td>${t.status}</td>
                                <td class="error">${t.deadline}</td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:otherwise>
            </c:choose>

            <h3>Waiting for the Executive Editor</h3>
            <c:choose>
                <c:when test="${empty dash.executive_queue}"><p>Nothing is waiting.</p></c:when>
                <c:otherwise>
                    <table>
                        <tr><th>Title</th><th>Desk</th><th>Reporter</th><th>Deadline</th></tr>
                        <c:forEach var="t" items="${dash.executive_queue}">
                            <tr>
                                <td><c:out value="${t.title}" /></td>
                                <td><c:out value="${t.desk_name}" /></td>
                                <td><c:out value="${t.author_name}" /></td>
                                <td><c:out value="${t.deadline}" default="-" /></td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:otherwise>
            </c:choose>

            <p><a href="${base}/report">Open the report builder</a></p>
        </div>
    </body>
</html>
