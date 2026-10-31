<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Categories and Desks</title>
        <link rel="stylesheet" href="${base}/css/style.css">
    </head>
    <body>
        <jsp:include page="menu.jsp" />
        <div class="content">
            <h2>Categories and desks</h2>

            <c:if test="${param.msg == 'deskCreated'}"><p class="ok">Desk created.</p></c:if>
            <c:if test="${param.msg == 'deskUpdated'}"><p class="ok">Desk updated.</p></c:if>
            <c:if test="${param.msg == 'deskDeleted'}"><p class="ok">Desk deleted.</p></c:if>
            <c:if test="${param.msg == 'categoryCreated'}"><p class="ok">Category created.</p></c:if>
            <c:if test="${param.msg == 'categoryUpdated'}"><p class="ok">Category updated.</p></c:if>
            <c:if test="${param.msg == 'categoryDeleted'}"><p class="ok">Category deleted.</p></c:if>
            <c:if test="${param.msg == 'nochange'}"><p class="ok">No changes were made.</p></c:if>
            <c:if test="${not empty error}">
                <p class="error"><c:out value="${error}" /></p>
            </c:if>

            <h3>Desks</h3>
            <p><a class="button" href="${base}/categories?action=newDesk">+ Add desk</a></p>
            <table>
                <tr>
                    <th>ID</th>
                    <th>Desk name</th>
                    <th>Actions</th>
                </tr>
                <c:forEach var="d" items="${deskList}">
                    <tr>
                        <td>${d.deskId}</td>
                        <td><c:out value="${d.deskName}" /></td>
                        <td>
                            <a href="${base}/categories?action=editDesk&id=${d.deskId}">Edit</a>
                            <form method="post" action="${base}/categories" style="display:inline"
                                  onsubmit="return confirm('Delete this desk?');">
                                <input type="hidden" name="action" value="deleteDesk">
                                <input type="hidden" name="id" value="${d.deskId}">
                                <button type="submit">Delete</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </table>

            <h3>Category tree</h3>
            <p><a class="button" href="${base}/categories?action=newCategory">+ Add category</a></p>
            <table>
                <tr>
                    <th>Category</th>
                    <th>Desk</th>
                    <th>Actions</th>
                </tr>
                <c:forEach var="c" items="${categoryTree}">
                    <tr>
                        <td>${indents[c.categoryId]}<c:out value="${c.categoryName}" /></td>
                        <td><c:out value="${deskNames[c.deskId]}" /></td>
                        <td>
                            <a href="${base}/categories?action=editCategory&id=${c.categoryId}">Edit</a>
                            <form method="post" action="${base}/categories" style="display:inline"
                                  onsubmit="return confirm('Delete this category?');">
                                <input type="hidden" name="action" value="deleteCategory">
                                <input type="hidden" name="id" value="${c.categoryId}">
                                <button type="submit">Delete</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </div>
    </body>
</html>
