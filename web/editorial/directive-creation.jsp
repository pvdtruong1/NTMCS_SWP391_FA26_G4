<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tạo Đề Tài Đặt Hàng (SCR-2.4) | NTMCS — Tòa Soạn Hội Tụ</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .btn, .nav-item, .page-link-item, .tab-btn {
            text-decoration: none !important;
        }
        .step-progress {
            display: flex;
            align-items: center;
            gap: 16px;
            margin-bottom: 24px;
            background: #fff;
            padding: 16px 20px;
            border-radius: 12px;
            border: 1px solid var(--line);
        }
        .step-item {
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 13px;
            font-weight: 700;
            color: var(--muted);
        }
        .step-item.active {
            color: var(--primary);
        }
        .step-num {
            width: 28px;
            height: 28px;
            border-radius: 99px;
            background: var(--surface2);
            color: var(--muted);
            display: grid;
            place-items: center;
            font-size: 12px;
            font-weight: 800;
        }
        .step-item.active .step-num {
            background: var(--primary);
            color: #fff;
        }
        .step-arrow {
            color: var(--line);
            font-size: 16px;
        }
        .directive-layout {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 24px;
            align-items: start;
        }
        @media (max-width: 950px) {
            .directive-layout {
                grid-template-columns: 1fr;
            }
        }
        .form-group {
            margin-bottom: 20px;
        }
        .form-label {
            display: block;
            font-weight: 700;
            font-size: 13px;
            margin-bottom: 8px;
            color: var(--text);
        }
        .form-label .req {
            color: var(--danger);
            margin-left: 4px;
        }
        .form-control {
            width: 100%;
            border: 1px solid var(--line);
            border-radius: 10px;
            padding: 11px 14px;
            font-size: 14px;
            background: #fff;
            color: var(--text);
            box-sizing: border-box;
            transition: border-color .2s;
        }
        .form-control:focus {
            outline: none;
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(52, 88, 212, 0.15);
        }
        .helper-text {
            font-size: 12px;
            color: var(--muted);
            margin-top: 6px;
            line-height: 1.4;
        }
        .quota-card {
            background: linear-gradient(135deg, #1b263b, #2b4570);
            color: #fff;
            border-radius: 14px;
            padding: 20px;
            margin-bottom: 18px;
        }
        .quota-progress-bar {
            height: 8px;
            background: rgba(255,255,255,0.2);
            border-radius: 99px;
            overflow: hidden;
            margin-top: 10px;
        }
        .quota-progress-fill {
            height: 100%;
            background: #5de0a5;
            border-radius: 99px;
        }
    </style>
</head>
<body>
<div class="app">
    <!-- Static Sidebar -->
    <aside id="sidebar">
        <div class="brand">
            <div class="brand-logo">N</div>
            <div>
                <b>NTMCS</b>
                <small>Hệ Thống Tòa Soạn Hội Tụ</small>
            </div>
        </div>
                <nav>
            <div class="nav-group">
                <div class="nav-label">TỔNG QUAN</div>
                <a href="${pageContext.request.contextPath}/home" class="nav-item">
                    <i>🏠</i> Trang chủ điều hành
                </a>
            </div>

            <c:if test="${not empty sessionScope.permissions and sessionScope.permissions.contains('TOPIC_EVALUATE')}">
                <div class="nav-group">
                    <div class="nav-label">PHÂN HỆ BTV CHUYÊN MÔN (WF-02)</div>
                    <a href="${pageContext.request.contextPath}/editorial/pending-queue" class="nav-item">
                        <i>📋</i> Hàng đợi chờ thẩm định
                    </a>
                    <a href="${pageContext.request.contextPath}/editorial/directive-creation" class="nav-item active">
                        <i>⚡</i> Đặt hàng đề tài chỉ đạo
                    </a>
                    <a href="${pageContext.request.contextPath}/editorial/idea-archive" class="nav-item">
                        <i>🗄️</i> Kho lưu trữ ý tưởng
                    </a>
                </div>
            </c:if>

            <c:if test="${not empty sessionScope.permissions and (sessionScope.permissions.contains('USER_MANAGE') or sessionScope.permissions.contains('RBAC_MANAGE') or sessionScope.permissions.contains('AUDIT_VIEW'))}">
                <div class="nav-group">
                    <div class="nav-label">QUẢN TRỊ HỆ THỐNG (WF-05)</div>
                    <c:if test="${sessionScope.permissions.contains('USER_MANAGE')}">
                        <a href="${pageContext.request.contextPath}/users" class="nav-item">
                            <i>👥</i> Tài khoản người dùng
                        </a>
                    </c:if>
                    <c:if test="${sessionScope.permissions.contains('RBAC_MANAGE')}">
                        <a href="${pageContext.request.contextPath}/rbac" class="nav-item">
                            <i>🛡️</i> Ma trận quyền (RBAC)
                        </a>
                    </c:if>
                    <c:if test="${sessionScope.permissions.contains('AUDIT_VIEW')}">
                        <a href="${pageContext.request.contextPath}/audit-log" class="nav-item">
                            <i>📜</i> Nhật ký hệ thống (Audit)
                        </a>
                    </c:if>
                </div>
            </c:if>

            <c:if test="${not empty sessionScope.permissions and sessionScope.permissions.contains('QUOTA_MANAGE')}">
                <div class="nav-group">
                    <div class="nav-label">ĐIỀU HÀNH &amp; CHỈ TIÊU (WF-05)</div>
                    <a href="${pageContext.request.contextPath}/dashboard" class="nav-item">
                        <i>📊</i> Dashboard điều hành
                    </a>
                    <a href="${pageContext.request.contextPath}/report" class="nav-item">
                        <i>📈</i> Báo cáo hiệu suất
                    </a>
                    <a href="${pageContext.request.contextPath}/quota" class="nav-item">
                        <i>🎯</i> Chỉ tiêu phòng ban (Quota)
                    </a>
                    <a href="${pageContext.request.contextPath}/criteria" class="nav-item">
                        <i>⚖️</i> Bộ tiêu chí Rubric
                    </a>
                    <a href="${pageContext.request.contextPath}/categories" class="nav-item">
                        <i>🗂️</i> Chuyên mục &amp; Phòng ban
                    </a>
                </div>
            </c:if>

            <div class="nav-group">
                <div class="nav-label">HỆ THỐNG</div>
                <a href="${pageContext.request.contextPath}/logout" class="nav-item" style="color: #e53e3e;">
                    <i>🚪</i> Đăng xuất
                </a>
            </div>
        </nav>
        <div class="sidebar-bottom">
            <span class="demo">SE2066 — NHÓM 4</span>
            <small>Phân hệ Biên tập (WF-02)</small>
        </div>
    </aside>

    <!-- Main Workspace -->
    <div class="main">
        <!-- Topbar -->
                <header class="topbar">
            <button class="icon-btn mobile-menu" onclick="document.getElementById('sidebar').classList.toggle('open')">☰</button>
            <div class="brand-desk-info">
                <strong>🏛️ ${currentDesk != null ? currentDesk.deskName : 'Ban Thời sự - Chính trị'}</strong>
            </div>
            <div style="margin-left: auto; display: flex; align-items: center; gap: 12px;">
                <div class="account-chip">
                    <div class="avatar"><c:out value="${not empty sessionScope.loggedUser ? sessionScope.loggedUser.fullName.substring(0, 1).toUpperCase() : (not empty sessionScope.user ? sessionScope.user.fullName.substring(0, 1).toUpperCase() : 'T')}" /></div>
                    <div>
                        <b><c:out value="${not empty sessionScope.loggedUser ? sessionScope.loggedUser.fullName : (not empty sessionScope.user ? sessionScope.user.fullName : 'Trường')}" /></b>
                        <small>
                            <c:choose>
                                <c:when test="${(not empty sessionScope.loggedUser ? sessionScope.loggedUser.roleName : sessionScope.user.roleName) == 'DESK_EDITOR'}">Desk Editor (BTV Chuyên Môn)</c:when>
                                <c:when test="${(not empty sessionScope.loggedUser ? sessionScope.loggedUser.roleName : sessionScope.user.roleName) == 'EXECUTIVE_EDITOR'}">Tổng Biên Tập</c:when>
                                <c:when test="${(not empty sessionScope.loggedUser ? sessionScope.loggedUser.roleName : sessionScope.user.roleName) == 'BUSINESS_ADMINISTRATOR'}">Quản Trị Nghiệp Vụ</c:when>
                                <c:when test="${(not empty sessionScope.loggedUser ? sessionScope.loggedUser.roleName : sessionScope.user.roleName) == 'SYSTEM_ADMINISTRATOR'}">Quản Trị Hệ Thống</c:when>
                                <c:when test="${(not empty sessionScope.loggedUser ? sessionScope.loggedUser.roleName : sessionScope.user.roleName) == 'REPORTER'}">Phóng Viên Tác Nghiệp</c:when>
                                <c:otherwise><c:out value="${not empty sessionScope.loggedUser ? sessionScope.loggedUser.roleName : (not empty sessionScope.user ? sessionScope.user.roleName : 'Desk Editor')}" /></c:otherwise>
                            </c:choose>
                        </small>
                    </div>
                </div>
                <a href="${pageContext.request.contextPath}/logout" class="btn sm danger" style="padding: 6px 14px; font-size: 12px; font-weight: 700; text-decoration: none; border-radius: 8px;">Đăng xuất</a>
            </div>
        </header>

        <!-- Main Content Container -->
        <main>
            <!-- Page Header -->
            <div class="page-head">
                <div>
                    <div class="eyebrow">EDITORIAL WORKFLOW / SCR-2.4</div>
                    <h1>Tạo Đề Tài Đặt Hàng Ban Biên Tập</h1>
                    <p>Khởi tạo vệt bài điều tra, tin nóng hoặc nhiệm vụ tuyên truyền trọng điểm có tính chỉ đạo trực tiếp từ Ban Biên Tập.</p>
                </div>
                <div class="page-actions">
                    <a href="${pageContext.request.contextPath}/editorial/pending-queue" class="btn" style="border: 1px solid var(--line);">
                        &larr; Hàng đợi thẩm định
                    </a>
                </div>
            </div>

            <!-- 2-Step Workflow Progress Tracker -->
            <div class="step-progress">
                <div class="step-item active">
                    <div class="step-num">1</div>
                    <div>
                        <div>BƯỚC 1: SOẠN THẢO CHỈ ĐẠO</div>
                        <small style="font-weight: normal; color: var(--primary);">Form tạo đề tài đặt hàng (SCR-2.4)</small>
                    </div>
                </div>
                <div class="step-arrow">&rarr;</div>
                <div class="step-item">
                    <div class="step-num">2</div>
                    <div>
                        <div>BƯỚC 2: ĐIỀU ĐỘNG PHÓNG VIÊN</div>
                        <small style="font-weight: normal; color: var(--muted);">Bàn cân bằng tải & Giao việc (SCR-2.5)</small>
                    </div>
                </div>
            </div>

            <!-- Success Notification Banner -->
            <c:if test="${param.msg == 'draft_saved'}">
                <div style="margin-bottom: 18px; padding: 14px 18px; border-radius: 12px; font-weight: 600; display: flex; align-items: center; gap: 12px; background: #e8f7ef; color: #16845b; border: 1px solid #c2ebd5;">
                    <span style="font-size: 22px;">✔</span>
                    <div>
                        <b>Đã lưu bản thảo thành công!</b> Đề tài chỉ đạo đã được lưu nháp trong cơ sở dữ liệu. Bạn có thể tiếp tục chỉnh sửa hoặc chuyển sang bước Điều động phóng viên.
                    </div>
                </div>
            </c:if>

            <!-- Error Notification Banner -->
            <c:if test="${not empty errorMessage or not empty param.error}">
                <div style="margin-bottom: 18px; padding: 14px 18px; border-radius: 12px; font-weight: 600; display: flex; align-items: center; gap: 12px; background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce;">
                    <span style="font-size: 20px;">⚠️</span>
                    <div>
                        <b>Lỗi nhập liệu:</b> ${not empty errorMessage ? errorMessage : 'Không thể lưu bản thảo đề tài. Vui lòng kiểm tra lại.'}
                    </div>
                </div>
            </c:if>

            <!-- 2-Column Responsive Layout -->
            <div class="directive-layout">
                <!-- LEFT COLUMN: Creation Form -->
                <div class="card" style="padding: 24px;">
                    <form method="post" action="${pageContext.request.contextPath}/editorial/directive-topic" id="directiveForm">
                        <input type="hidden" name="topicId" value="${topic != null ? topic.topicId : ''}">

                        <!-- Form Field: Title -->
                        <div class="form-group">
                            <label class="form-label" for="topicTitle">
                                Tiêu đề đề tài chỉ đạo <span class="req">*</span>
                            </label>
                            <input type="text" id="topicTitle" name="title" class="form-control"
                                   placeholder="Ví dụ: Vệt bài điều tra tình trạng vi phạm đất đai và trật tự xây dựng tại vùng ven..."
                                   value="${topic != null ? topic.title : ''}" required maxlength="255">
                            <div class="helper-text">Tiêu đề phải súc tích, phản ánh rõ bản chất sự kiện và định hướng của tòa soạn (5 - 255 ký tự).</div>
                        </div>

                        <!-- Form Grid: Category & Deadline -->
                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                            <div class="form-group">
                                <label class="form-label" for="categorySelect">
                                    Chuyên mục tin tức <span class="req">*</span>
                                </label>
                                <select id="categorySelect" name="categoryId" class="form-control" required>
                                    <option value="">-- Chọn chuyên mục tin --</option>
                                    <c:forEach var="cat" items="${categories}">
                                        <option value="${cat.categoryId}" ${topic != null && topic.categoryId == cat.categoryId ? 'selected' : ''}>
                                            ${cat.parentCategoryId != null ? '&nbsp;&nbsp;↳ ' : '📁 '} ${cat.categoryName}
                                        </option>
                                    </c:forEach>
                                </select>
                                <div class="helper-text">Chuyên mục tin trực thuộc thẩm quyền của Ban.</div>
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="deadlineInput">
                                    Hạn chót hoàn thành bài (Deadline) <span class="req">*</span>
                                </label>
                                <input type="date" id="deadlineInput" name="deadline" class="form-control"
                                       min="${todayDate}"
                                       value="${topic != null && topic.deadline != null ? topic.deadline : ''}" required>
                                <div class="helper-text">Hạn nộp bản thảo bài viết để BTV nghiệm thu lên CMS.</div>
                            </div>
                        </div>

                        <!-- Form Field: Mandatory Angle & Direction -->
                        <div class="form-group">
                            <label class="form-label" for="topicAngle">
                                Định hướng tác nghiệp & Góc tiếp cận bắt buộc <span class="req">*</span>
                            </label>
                            <textarea id="topicAngle" name="angle" rows="4" class="form-control"
                                      placeholder="Nêu rõ các nguồn tin cần tiếp cận, tuyến nhân vật cần phỏng vấn, các luận điểm pháp lý cần làm rõ hoặc chứng cứ hiện trường cần thu thập..."
                                      required maxlength="255">${topic != null ? topic.angle : ''}</textarea>
                            <div class="helper-text">Chỉ đạo nghiệp vụ bắt buộc từ Ban Biên Tập mà phóng viên được giao phải bám sát (10 - 255 ký tự).</div>
                        </div>

                        <!-- Contextual Note on Directive Exemption -->
                        <div style="background: #f0f7ff; border-left: 4px solid var(--primary); padding: 14px 16px; border-radius: 8px; margin-bottom: 24px; font-size: 13px; line-height: 1.5; color: #1e3a8a;">
                            💡 <strong>CƠ CHẾ ĐẶC THÙ ĐỀ TÀI CHỈ ĐẠO:</strong><br>
                            Vì đề tài khởi nguồn từ cấp quản lý tòa soạn, hệ thống <strong>tự động miễn thẩm định Rubric cấp 1</strong>. Sau khi chuyển sang Bước 2 và phân công Phóng viên chủ trì, đề tài sẽ được kích hoạt ngay ở trạng thái <code>APPROVED</code> kèm mã định danh chính thức <code>TOPIC-...</code>.
                        </div>

                        <!-- Action Buttons -->
                        <div style="display: flex; gap: 14px; align-items: center; justify-content: flex-end; padding-top: 16px; border-top: 1px solid var(--line);">
                            <button type="submit" name="action" value="save_draft" class="btn" style="border: 1px solid var(--line); padding: 11px 20px; font-weight: 600;">
                                💾 Lưu Bản Thảo Chỉ Đạo
                            </button>
                            <button type="submit" name="action" value="proceed_dispatch" class="btn primary" style="padding: 11px 24px; font-weight: 700; box-shadow: 0 4px 14px rgba(52, 88, 212, 0.25);">
                                Tiếp Tục: Phân Công Phóng Viên & Cân Bằng Tải &rarr;
                            </button>
                        </div>
                    </form>
                </div>

                <!-- RIGHT COLUMN: Metadata & Production Quota Widget -->
                <div>
                    <!-- Quota & Performance Card -->
                    <div class="quota-card">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                            <span style="font-size: 11px; font-weight: 800; letter-spacing: 1px; text-transform: uppercase; color: #8fa8ff;">
                                🎯 CHỈ TIÊU SẢN XUẤT THÁNG
                            </span>
                            <span class="badge" style="background: rgba(255,255,255,0.15); color: #fff; font-size: 11px; padding: 2px 8px;">
                                ${currentPeriod}
                            </span>
                        </div>
                        <div style="display: flex; align-items: baseline; gap: 6px; margin: 12px 0 6px;">
                            <span style="font-size: 32px; font-weight: 900;">${productionCount}</span>
                            <span style="font-size: 16px; opacity: 0.7;">/ ${deskQuota} bài</span>
                        </div>
                        <small style="opacity: 0.85; font-size: 12px;">
                            <c:choose>
                                <c:when test="${productionCount >= deskQuota}">
                                    🎉 Ban đã hoàn thành 100% định ngạch tháng này!
                                </c:when>
                                <c:otherwise>
                                    Cần sản xuất thêm <b>${deskQuota - productionCount}</b> đề tài để đạt chỉ tiêu Quota tháng ${currentPeriod}.
                                </c:otherwise>
                            </c:choose>
                        </small>
                        <div class="quota-progress-bar">
                            <c:set var="pct" value="${(productionCount * 100) / deskQuota}" />
                            <div class="quota-progress-fill" style="width: ${pct > 100 ? 100 : pct}%;"></div>
                        </div>
                    </div>

                    <!-- Guidelines Card -->
                    <div class="card" style="padding: 18px; margin-bottom: 18px;">
                        <h3 style="font-size: 14px; margin-top: 0; margin-bottom: 12px; color: var(--text);">
                            📋 Tiêu Chuẩn Đề Tài Đặt Hàng
                        </h3>
                        <ul style="margin: 0; padding-left: 18px; font-size: 12.5px; color: var(--muted); line-height: 1.6;">
                            <li><strong>Tính cấp thiết:</strong> Phục vụ trực tiếp dòng sự kiện nóng hoặc các ngày kỷ niệm lớn.</li>
                            <li><strong>Phối hợp đa phương tiện:</strong> Khuyến khích kết hợp phóng viên ảnh, quay phim và đồ họa.</li>
                            <li><strong>Cân bằng tải:</strong> Ở bước tiếp theo, hãy kiểm tra kỹ số đề tài đang chạy của phóng viên để tránh quá tải.</li>
                        </ul>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>
</body>
</html>
