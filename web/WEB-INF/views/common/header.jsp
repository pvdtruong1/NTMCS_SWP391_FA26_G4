<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${param.pageTitle != null ? param.pageTitle : 'NTMCS — Newsroom Topic Management & Coordination System'}</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
  <script>
    if (localStorage.getItem('ntmcs_dark_v2') === '1') {
      document.documentElement.classList.add('dark');
      document.addEventListener('DOMContentLoaded', function() {
        document.body.classList.add('dark');
      });
    }
    window.NTMCS_CONTEXT_PATH = '${pageContext.request.contextPath}';
  </script>
</head>
<body>
<div class="app">
