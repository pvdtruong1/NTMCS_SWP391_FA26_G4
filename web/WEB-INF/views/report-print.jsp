<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Print-ready report: the browser's print dialog opens by itself, choose "Save as PDF". --%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>NTMCS Performance Report</title>
        <style>
            body { font-family: Arial, sans-serif; margin: 24px; color: #222; }
            h1 { font-size: 20px; margin: 0 0 4px; }
            p { margin: 2px 0; color: #555; font-size: 13px; }
            table { border-collapse: collapse; margin-top: 16px; width: 100%; font-size: 13px; }
            th, td { border: 1px solid #999; padding: 6px 8px; text-align: left; }
            th { background: #e8ecf2; }
            .noprint { margin-bottom: 16px; }
            @media print { .noprint { display: none; } body { margin: 0; } }
        </style>
    </head>
    <body onload="window.print()">
        <div class="noprint"><button onclick="window.print()">Print / Save as PDF</button></div>
        <h1>NTMCS performance report</h1>
        <p><c:out value="${result.description}" />. Draft topics are not counted.</p>
        <p>Generated ${generatedAt} by <c:out value="${generatedBy}" /></p>

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
    </body>
</html>
