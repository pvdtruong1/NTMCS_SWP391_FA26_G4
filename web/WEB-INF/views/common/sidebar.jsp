<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<aside id="sidebar">
  <a href="${pageContext.request.contextPath}/reporter/dashboard" class="brand">
    <div class="brand-logo">N</div>
    <div>
      <b>NTMCS</b>
      <small>Newsroom OS</small>
    </div>
  </a>

  <nav>
    <div class="nav-group">
      <div class="nav-label">CHÍNH (ITERATION 1)</div>
      <a class="nav-item ${activeNav eq 'dashboard' ? 'active' : ''}" href="${pageContext.request.contextPath}/reporter/dashboard">
        <i>📊</i>
        <span>Dashboard</span>
      </a>
      <a class="nav-item ${activeNav eq 'topics' ? 'active' : ''}" href="${pageContext.request.contextPath}/reporter/topics">
        <i>📑</i>
        <span>Đề tài của tôi</span>
      </a>
      <a class="nav-item ${activeNav eq 'duplicate-search' ? 'active' : ''}" href="${pageContext.request.contextPath}/reporter/duplicate-search">
        <i>🔍</i>
        <span>Tra cứu trùng lặp</span>
      </a>
      <a class="nav-item ${activeNav eq 'compose' ? 'active' : ''}" href="${pageContext.request.contextPath}/reporter/compose">
        <i>✍️</i>
        <span>+ Soạn đề xuất</span>
      </a>
    </div>

    <div class="nav-group">
      <div class="nav-label">HỆ THỐNG</div>
      <button type="button" class="nav-item" data-notif-trigger>
        <i>🔔</i>
        <span>Thông báo</span>
      </button>
      <button type="button" class="nav-item" data-account-trigger>
        <i>👤</i>
        <span>Tài khoản phóng viên</span>
      </button>
      <a class="nav-item" href="${pageContext.request.contextPath}/reporter/reset" onclick="return confirm('Khôi phục toàn bộ đề tài về dữ liệu mẫu seed?')">
        <i>🔄</i>
        <span>Reset về Seed DB</span>
      </a>
    </div>

    <div class="nav-group">
      <div class="nav-label">PHẠM VI TIẾP THEO</div>
      <div class="nav-item" style="opacity: 0.55; cursor: default;">
        <i>⚖️</i>
        <span>Đánh giá & Thẩm định</span>
        <span class="demo">Iter 2</span>
      </div>
      <div class="nav-item" style="opacity: 0.55; cursor: default;">
        <i>👥</i>
        <span>Ekip & Phân công</span>
        <span class="demo">Iter 2</span>
      </div>
      <div class="nav-item" style="opacity: 0.55; cursor: default;">
        <i>₫</i>
        <span>Dự toán kinh phí</span>
        <span class="demo">Iter 2</span>
      </div>
      <div class="nav-item" style="opacity: 0.55; cursor: default;">
        <i>📦</i>
        <span>Tài sản & Xe công tác</span>
        <span class="demo">Iter 2</span>
      </div>
      <div class="nav-item" style="opacity: 0.55; cursor: default;">
        <i>☑</i>
        <span>Fieldwork Task</span>
        <span class="demo">Iter 2</span>
      </div>
    </div>
  </nav>

  <div class="sidebar-bottom">
    <div style="display:flex;align-items:center;justify-content:space-between">
      <span class="demo">ITERATION 1</span>
      <span style="font-size:11px;color:#7e8ea6">v1.2</span>
    </div>
    <small>Phóng viên tác nghiệp (WF1)</small>
  </div>
</aside>
<section class="main">
