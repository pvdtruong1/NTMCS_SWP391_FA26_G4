<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${category.categoryId > 0}" />
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Category Form</title>
        <link rel="stylesheet" href="${base}/css/style.css">
    </head>
    <body>
        <jsp:include page="menu.jsp" />
        <div class="content">
            <h2>${isEdit ? 'Edit category' : 'Add category'}</h2>

            <c:if test="${not empty error}">
                <p class="error"><c:out value="${error}" /></p>
            </c:if>

            <form method="post" action="${base}/categories">
                <input type="hidden" name="action" value="${isEdit ? 'updateCategory' : 'createCategory'}">
                <input type="hidden" name="id" value="${category.categoryId}">

                <label>Category name</label>
                <input type="text" name="categoryName" value="<c:out value='${category.categoryName}' />">

                <label>Parent category</label>
                <select name="parentId">
                    <option value="">-- None (top level) --</option>
                    <c:forEach var="p" items="${treeList}">
                        <c:if test="${not excluded.contains(p.categoryId)}">
                            <option value="${p.categoryId}" ${p.categoryId == category.parentCategoryId ? 'selected' : ''}>${indents[p.categoryId]}<c:out value="${p.categoryName}" /></option>
                        </c:if>
                    </c:forEach>
                </select>

                <label>Desk</label>
                <select name="deskId">
                    <option value="">-- Choose a desk --</option>
                    <c:forEach var="d" items="${deskList}">
                        <option value="${d.deskId}" ${d.deskId == category.deskId ? 'selected' : ''}><c:out value="${d.deskName}" /></option>
                    </c:forEach>
                </select>

                <p>
                    <button type="submit">Save</button>
                    <a href="${base}/categories">Cancel</a>
                </p>
            </form>
        </div>
    </body>
</html>
