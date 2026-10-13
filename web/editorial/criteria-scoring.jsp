<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bàn Thẩm Định Tiêu Chí Rubric (SCR-2.3) | NTMCS — Tòa Soạn Hội Tụ</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        a, a:hover, a:focus, a:visited, a:active {
            text-decoration: none !important;
        }
        .btn, .nav-item {
            text-decoration: none !important;
        }
        .scoring-layout {
            display: grid;
            grid-template-columns: 1.1fr 1.3fr;
            gap: 20px;
            align-items: start;
        }
        @media(max-width: 1050px) {
            .scoring-layout {
                grid-template-columns: 1fr;
            }
        }
        .criteria-card {
            background: var(--surface2);
            border: 1px solid var(--line);
            border-radius: 12px;
            padding: 14px;
            margin-bottom: 12px;
        }
        .criteria-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
        }
        .criteria-header b {
            font-size: 14px;
        }
        .criteria-inputs {
            display: flex;
            align-items: center;
            gap: 14px;
        }
        .criteria-inputs input[type="range"] {
            flex: 1;
            height: 6px;
            accent-color: var(--primary);
            cursor: pointer;
        }
        .score-box {
            width: 60px;
            text-align: center;
            font-weight: 800;
            font-size: 16px;
            padding: 6px;
            border-radius: 8px;
            border: 1px solid var(--line);
            background: #fff;
            color: var(--primary);
        }
        .total-summary-card {
            background: linear-gradient(135deg, #1b263b, #2b4570);
            color: #fff;
            border-radius: 14px;
            padding: 18px 20px;
            margin-bottom: 16px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .total-score-val {
            font-size: 36px;
            font-weight: 900;
            color: #5de0a5;
        }
        .legal-risk-box {
            border: 2px dashed #f4ddb2;
            background: #fffdf8;
            border-radius: 12px;
            padding: 14px;
            margin-bottom: 16px;
            transition: all 0.2s ease;
        }
        .legal-risk-box.high-risk {
            border-color: #c83a4a;
            background: #fff5f5;
        }
        .guard-alert {
            display: none;
            background: #ffebee;
            color: #c83a4a;
            border: 1px solid #ffcdd2;
            border-radius: 10px;
            padding: 10px 14px;
            font-size: 13px;
            font-weight: 600;
            margin-top: 10px;
            animation: fadeIn 0.2s ease;
        }
        .btn-decision-group {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-top: 16px;
        }
        .btn-decision {
            padding: 12px 18px;
            font-size: 14px;
            font-weight: 700;
            border-radius: 10px;
            cursor: pointer;
            border: 1px solid transparent;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            transition: transform 0.1s ease, box-shadow 0.15s ease;
        }
        .btn-decision:active {
            transform: scale(0.98);
        }
        .btn-approve {
            background: #16845b;
            color: #fff;
        }
        .btn-approve:hover {
            background: #126e4b;
        }
        .btn-approve:disabled {
            background: #a5d3c0;
            cursor: not-allowed;
            opacity: 0.6;
        }
        .btn-escalate {
            background: #b26a00;
            color: #fff;
        }
        .btn-escalate:hover {
            background: #8f5500;
        }
        .btn-revise {
            background: #f1ebff;
            color: #6e4cb1;
            border-color: #d8c7ff;
        }
        .btn-revise:hover {
            background: #e4d7ff;
        }
        .btn-reject {
            background: #fff0f2;
            color: #c83a4a;
            border-color: #f0c8ce;
        }
        .btn-reject:hover {
            background: #ffdce1;
        }
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-4px); }
            to { opacity: 1; transform: translateY(0); }
        }
    </style>
</head>
<body>
<div class="app">
    <!-- Sidebar Navigation -->
    <aside id="sidebar">
        <div class="brand">
            <div class="brand-logo">N</div>
            <div>
                <b>NTMCS</b>
                <small>Newsroom Operating System</small>
            </div>
        </div>
        <nav>
            <div class="nav-group">
                <div class="nav-label">PHÂN HỆ BTV CHUYÊN MÔN (WF-02)</div>
                <a href="${pageContext.request.contextPath}/editorial/pending-queue" class="nav-item">
                    <i>📋</i> Hàng đợi chờ thẩm định
                </a>
                <a href="${pageContext.request.contextPath}/editorial/idea-archive" class="nav-item">
                    <i>🗄️</i> Kho lưu trữ ý tưởng
                </a>
            </div>
            <div class="nav-group">
                <div class="nav-label">CÁC PHÂN HỆ KHÁC</div>
                <a href="${pageContext.request.contextPath}/" class="nav-item">
                    <i>🏠</i> Trang chủ tòa soạn
                </a>
            </div>
        </nav>
        <div class="sidebar-bottom">
            <span class="demo">ITERATION 1 — WALKING SKELETON</span>
            <small>Actor: Trường (Desk Editor)</small>
        </div>
    </aside>

    <!-- Main Content Area -->
    <div class="main">
        <!-- Top Bar -->
        <header class="topbar">
            <div class="brand-desk-info">
                <strong>🏛️ ${topic.deskName != null ? topic.deskName : 'Ban Thời sự - Chính trị'}</strong>
            </div>
            <div class="account-chip">
                <div class="avatar">T</div>
                <div>
                    <b>${sessionScope.user != null ? sessionScope.user.fullName : 'Trường'}</b>
                    <small>Desk Editor (BTV Chuyên Môn)</small>
                </div>
            </div>
        </header>

        <!-- Main Body -->
        <main>
            <!-- Page Header -->
            <div class="page-head">
                <div>
                    <span class="eyebrow">EDITORIAL WORKFLOW / SCR-2.3</span>
                    <h1>Bàn Thẩm Định Tiêu Chí Rubric</h1>
                    <p>Bàn thẩm định đa tiêu chí Rubric cấp 1 & Phê duyệt / Điều hướng đề tài nộp từ Phóng viên.</p>
                </div>
                <div>
                    <a href="${pageContext.request.contextPath}/editorial/pending-queue" class="btn">
                        &larr; Quay lại Hàng đợi
                    </a>
                </div>
            </div>

            <!-- Error Alert Banner -->
            <c:if test="${not empty param.error}">
                <div style="margin-bottom: 20px; padding: 14px 18px; border-radius: 12px; font-weight: 600; display: flex; align-items: center; gap: 12px; background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce;">
                    <span style="font-size: 20px;">⚠️</span>
                    <div>
                        <c:choose>
                            <c:when test="${param.error == 'score_below_threshold'}">
                                <b>Không đủ điều kiện phê duyệt Cấp 1:</b> Điểm tổng hợp Rubric của đề tài chưa đạt ngưỡng chất lượng tối thiểu (yêu cầu &ge; 5.0/10). BTV chỉ được phép chọn <i>"Yêu cầu chỉnh sửa đề cương"</i> hoặc <i>"Bác bỏ đề tài"</i>.
                            </c:when>
                            <c:when test="${param.error == 'comment_required'}">
                                <b>Yêu cầu nhập nhận xét:</b> Khi yêu cầu phóng viên chỉnh sửa hoặc bác bỏ đề tài, BTV bắt buộc phải nhập ý kiến/lý do cụ thể.
                            </c:when>
                            <c:when test="${param.error == 'invalid_decision'}">
                                <b>Quyết định không hợp lệ:</b> Quyết định phê duyệt gửi lên không đúng định dạng.
                            </c:when>
                            <c:when test="${param.error == 'eval_failed'}">
                                <b>Không thể lưu thẩm định:</b> Giao dịch cập nhật cơ sở dữ liệu không thành công. Vui lòng thử lại.
                            </c:when>
                            <c:when test="${param.error == 'server_error'}">
                                <b>Lỗi hệ thống:</b> Đã xảy ra lỗi trong quá trình xử lý. Vui lòng kiểm tra lại kết nối.
                            </c:when>
                            <c:otherwise>
                                <b>Có lỗi xảy ra:</b> Không thể hoàn tất thẩm định (${param.error}).
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:if>

            <!-- Two-Column Layout -->
            <div class="scoring-layout">
                <!-- LEFT COLUMN: Topic Dossier & Itemized Estimates -->
                <div>
                    <!-- Topic Overview Card -->
                    <div class="card">
                        <div class="card-title">
                            <h2>📄 Hồ Sơ Đề Cương Đề Tài #${topic.topicId}</h2>
                            <span class="badge b-submitted">Chờ Thẩm Định</span>
                        </div>

                        <div style="margin-bottom: 16px;">
                            <h3 style="font-size: 19px; line-height: 1.4; margin: 0 0 10px; color: var(--text);">
                                ${topic.title}
                            </h3>
                            <c:if test="${not empty topic.angle}">
                                <div style="background: var(--surface2); border-left: 4px solid var(--primary); padding: 12px 14px; border-radius: 0 8px 8px 0; margin-bottom: 14px;">
                                    <strong style="color: var(--primary); font-size: 12px; text-transform: uppercase;">🎯 Góc tiếp cận phóng sự (Angle):</strong>
                                    <p style="margin: 4px 0 0; color: var(--text); font-size: 13px;">${topic.angle}</p>
                                </div>
                            </c:if>
                        </div>

                        <div class="meta-grid" style="grid-template-columns: repeat(2, 1fr); margin-top: 14px;">
                            <div>
                                <span>Chuyên mục</span>
                                <b>${topic.categoryName}</b>
                            </div>
                            <div>
                                <span>Ban chuyên môn</span>
                                <b>${topic.deskName}</b>
                            </div>
                            <div>
                                <span>Phóng viên đề xuất</span>
                                <b>${topic.authorName}</b>
                                <small>${topic.authorEmail}</small>
                            </div>
                            <div>
                                <span>Hạn chót dự kiến</span>
                                <b>${topic.deadline != null ? topic.deadline : 'Chưa đặt hạn'}</b>
                            </div>
                        </div>
                    </div>

                    <!-- Operational Cost Estimates Card -->
                    <div class="card">
                        <div class="card-title">
                            <h2>💰 Dự Toán Chi Phí Tác Nghiệp (Topic Estimates)</h2>
                            <span class="tag">Tổng cộng: <fmt:formatNumber value="${totalEstimate}" type="currency" currencySymbol="VNĐ"/></span>
                        </div>

                        <div class="table-scroll">
                            <table>
                                <thead>
                                <tr>
                                    <th style="width: 40px;">#</th>
                                    <th>Khoản mục chi phí</th>
                                    <th style="text-align: right; width: 140px;">Số tiền dự toán</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:choose>
                                    <c:when test="${not empty estimates}">
                                        <c:forEach items="${estimates}" var="e" varStatus="loop">
                                            <tr>
                                                <td>${loop.index + 1}</td>
                                                <td><b>${e.costItem}</b></td>
                                                <td style="text-align: right; font-weight: 700; color: var(--primary);">
                                                    <fmt:formatNumber value="${e.amount}" type="currency" currencySymbol="VNĐ"/>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="3" style="text-align: center; color: var(--muted); padding: 18px;">
                                                Chưa có khoản mục chi phí nào được khai báo cho đề tài này.
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- RIGHT COLUMN: Rubric Scoring Desk & Decision Panel -->
                <div>
                    <form id="evalForm" method="post" action="${pageContext.request.contextPath}/editorial/criteria-scoring">
                        <input type="hidden" name="topicId" value="${topic.topicId}">
                        <input type="hidden" name="decision" id="decisionInput" value="APPROVED">

                        <!-- Rubric Criteria Scoring Card -->
                        <div class="card">
                            <div class="card-title">
                                <h2>⚖️ Bàn Chấm Điểm Rubric Tiêu Chí (1 - 10)</h2>
                                <span class="tag" style="background:#eef2ff; color:var(--primary); font-weight:700;">4 Tiêu chí chuẩn</span>
                            </div>

                            <!-- Realtime Total Score Display -->
                            <div class="total-summary-card">
                                <div>
                                    <span style="font-size: 11px; text-transform: uppercase; letter-spacing: 1px; opacity: 0.8;">
                                        ĐIỂM TỔNG HỢP CÓ TRỌNG SỐ (WEIGHTED SCORE)
                                    </span>
                                    <div style="display: flex; align-items: baseline; gap: 8px; margin-top: 4px;">
                                        <span class="total-score-val" id="totalWeightedScore">8.5</span>
                                        <span style="font-size: 18px; opacity: 0.7;">/ 10</span>
                                    </div>
                                    <small id="verdictLabel" style="color: #5de0a5; font-weight: 700; margin-top: 4px; display: block;">
                                        ⭐ Xuất sắc — Đạt điều kiện phê duyệt trực tiếp
                                    </small>
                                </div>
                                <div style="text-align: right;">
                                    <span style="display:block; font-size: 11px; opacity: 0.8;">CÔNG THỨC</span>
                                    <code style="background: rgba(255,255,255,0.15); color: #fff; font-size: 11px;">
                                        &sum;(Điểm &times; Trọng số)
                                    </code>
                                </div>
                            </div>

                            <!-- List of Rubric Criteria -->
                            <c:forEach items="${criteriaList}" var="c">
                                <div class="criteria-card">
                                    <div class="criteria-header">
                                        <b>${c.criteriaName}</b>
                                        <span class="badge" style="background: #eef2ff; color: var(--primary);">
                                            Trọng số: ${c.weightPercent}%
                                        </span>
                                    </div>
                                    <div class="criteria-inputs">
                                        <input type="range"
                                               min="1.0" max="10.0" step="0.5" value="8.5"
                                               data-weight="${c.weightPercent}"
                                               data-criteria-id="${c.criteriaId}"
                                               class="criterion-slider"
                                               oninput="syncScore(this)">
                                        <input type="number"
                                               min="1.0" max="10.0" step="0.5" value="8.5"
                                               name="score_${c.criteriaId}"
                                               id="score_input_${c.criteriaId}"
                                               class="score-box criterion-number"
                                               oninput="syncSlider(this, ${c.criteriaId})">
                                    </div>
                                    <div style="margin-top: 8px;">
                                        <input type="text"
                                               name="comment_${c.criteriaId}"
                                               placeholder="Ghi chú đánh giá cho tiêu chí này (tùy chọn)..."
                                               style="width: 100%; border: 1px solid var(--line); border-radius: 8px; padding: 6px 10px; font-size: 12px; background: #fff;">
                                    </div>
                                </div>
                            </c:forEach>

                            <!-- Legal Risk Exposure Slider (Guard Clause) -->
                            <div class="legal-risk-box" id="legalRiskContainer">
                                <div class="criteria-header">
                                    <div>
                                        <b style="color: #b26a00;">🛡️ Rủi Ro Pháp Lý & Khiếu Kiện (Legal Risk Exposure)</b>
                                        <small style="display: block; color: var(--muted); margin-top: 2px;">
                                            Thang 1-10: Đánh giá nguy cơ vi phạm bảo mật nguồn tin, vu khống, bản quyền hoặc tranh chấp pháp lý.
                                        </small>
                                    </div>
                                    <span class="badge b-escalated" id="legalBadge">Rủi ro: 3.0 / 10</span>
                                </div>

                                <div class="criteria-inputs" style="margin-top: 8px;">
                                    <input type="range"
                                           min="1.0" max="10.0" step="0.5" value="3.0"
                                           name="legalRiskScore"
                                           id="legalRiskSlider"
                                           oninput="handleLegalRiskChange(this.value)">
                                    <span id="legalScoreDisplay" class="score-box" style="color: #b26a00;">3.0</span>
                                </div>

                                <div class="guard-alert" id="guardAlertBox">
                                    ⚠️ <strong>CHỐT CHẶN AN TOÀN (LEGAL RISK &ge; 8.0):</strong> Đề tài có nguy cơ pháp lý đặc biệt nghiêm trọng, vượt quá thẩm quyền quyết định của BTV Chuyên môn. Hệ thống bắt buộc chuyển lên <strong>Tổng Biên Tập</strong> xem xét phê duyệt tối cao!
                                </div>
                            </div>

                            <!-- Overall Editorial Comments -->
                            <div style="margin-top: 16px;">
                                <label style="display: block; font-weight: 700; margin-bottom: 6px; font-size: 13px;">
                                    💬 Ý Kiến & Nhận Xét Của BTV (Gửi Phóng Viên & Ban Biên Tập):
                                </label>
                                <textarea name="comments" id="editorialComments" rows="3"
                                          placeholder="Nhập lý do phê duyệt, nội dung cần phóng viên hoàn thiện hoặc căn cứ chuyển TBT..."
                                          style="width: 100%; border: 1px solid var(--line); border-radius: 10px; padding: 10px 12px; resize: vertical;"></textarea>
                            </div>

                            <!-- Score Threshold Warning Box -->
                            <div class="guard-alert" id="scoreThresholdAlertBox" style="display: none; background: #fff1f2; border: 1px solid #fecdd3; color: #9f1239; border-radius: 8px; padding: 12px 14px; margin-top: 14px; font-size: 13px; line-height: 1.4;">
                                🚫 <strong>ĐIỂM CHƯA ĐẠT CHUẨN (Tổng: <span id="scoreAlertVal">0.0</span> / 10 &lt; 5.0):</strong> Đề tài không đáp ứng ngưỡng chất lượng tối thiểu để Phê duyệt Cấp 1 trực tiếp. BTV chỉ được phép chọn <strong>"Yêu cầu chỉnh sửa đề cương"</strong> hoặc <strong>"Bác bỏ đề tài"</strong>.
                            </div>

                            <!-- Action Buttons Group -->
                            <div class="btn-decision-group">
                                <button type="button" id="btnApprove" class="btn-decision btn-approve"
                                        onclick="submitDecision('APPROVED')">
                                    ✔ Phê duyệt Cấp 1 (APPROVED)
                                </button>
                                <button type="button" id="btnEscalate" class="btn-decision btn-escalate"
                                        onclick="submitDecision('ESCALATED')">
                                    🚀 Chuyển lên Tổng Biên Tập
                                </button>
                                <button type="button" class="btn-decision btn-revise"
                                        onclick="submitDecision('REVISION_REQUESTED')">
                                    🔄 Yêu cầu chỉnh sửa đề cương
                                </button>
                                <button type="button" class="btn-decision btn-reject"
                                        onclick="submitDecision('REJECTED')">
                                    ✖ Bác bỏ đề tài
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </main>
    </div>
</div>

<script>
    // 1. Sync Slider to Number Box
    function syncScore(slider) {
        var criteriaId = slider.getAttribute('data-criteria-id');
        var input = document.getElementById('score_input_' + criteriaId);
        if (input) input.value = slider.value;
        recalculateTotal();
    }

    // 2. Sync Number Box to Slider
    function syncSlider(input, criteriaId) {
        var slider = document.querySelector('input[type="range"][data-criteria-id="' + criteriaId + '"]');
        if (slider) slider.value = input.value;
        recalculateTotal();
    }

    // 3. Recalculate Weighted Total Score
    function recalculateTotal() {
        var sliders = document.querySelectorAll('.criterion-slider');
        var totalWeighted = 0.0;
        var totalWeight = 0.0;

        sliders.forEach(function(slider) {
            var score = parseFloat(slider.value) || 0.0;
            var weight = parseFloat(slider.getAttribute('data-weight')) || 0.0;
            totalWeighted += (score * weight);
            totalWeight += weight;
        });

        var finalScore = (totalWeight > 0) ? (totalWeighted / totalWeight) : 0.0;
        var displayScore = finalScore.toFixed(1);
        document.getElementById('totalWeightedScore').innerText = displayScore;

        var verdict = document.getElementById('verdictLabel');
        if (finalScore >= 8.0) {
            verdict.innerText = '⭐ Xuất sắc — Đạt điều kiện phê duyệt trực tiếp';
            verdict.style.color = '#5de0a5';
        } else if (finalScore >= 6.5) {
            verdict.innerText = '👍 Khá — Đạt chuẩn chất lượng tòa soạn';
            verdict.style.color = '#72d572';
        } else if (finalScore >= 5.0) {
            verdict.innerText = '⚠️ Trung bình — Đạt chuẩn tối thiểu, nên cân nhắc yêu cầu sửa';
            verdict.style.color = '#fbc02d';
        } else {
            verdict.innerText = '✖ Không đạt (< 5.0) — Bắt buộc Bác bỏ hoặc Yêu cầu sửa đổi';
            verdict.style.color = '#ff8090';
        }

        updateApprovalGuard();
    }

    // 4. Handle Legal Risk Changes
    function handleLegalRiskChange(val) {
        var score = parseFloat(val) || 0.0;
        document.getElementById('legalScoreDisplay').innerText = score.toFixed(1);
        document.getElementById('legalBadge').innerText = 'Rủi ro: ' + score.toFixed(1) + ' / 10';
        updateApprovalGuard();
    }

    // 4b. Unified Approval Guard Engine (Defense-in-Depth UI Guard)
    function updateApprovalGuard() {
        var finalScore = parseFloat(document.getElementById('totalWeightedScore').innerText) || 0.0;
        var legalScore = parseFloat(document.getElementById('legalRiskSlider').value) || 0.0;

        var btnApprove = document.getElementById('btnApprove');
        var btnEscalate = document.getElementById('btnEscalate');
        var container = document.getElementById('legalRiskContainer');
        var legalAlertBox = document.getElementById('guardAlertBox');
        var scoreAlertBox = document.getElementById('scoreThresholdAlertBox');

        var isScoreFailed = (finalScore < 5.0);
        var isHighLegalRisk = (legalScore >= 8.0);

        // Score threshold alert
        if (scoreAlertBox) {
            if (isScoreFailed) {
                scoreAlertBox.style.display = 'block';
                var alertVal = document.getElementById('scoreAlertVal');
                if (alertVal) alertVal.innerText = finalScore.toFixed(1);
            } else {
                scoreAlertBox.style.display = 'none';
            }
        }

        // Legal risk alert
        if (isHighLegalRisk) {
            container.classList.add('high-risk');
            legalAlertBox.style.display = 'block';
            btnEscalate.style.boxShadow = '0 0 0 3px rgba(178, 106, 0, 0.4)';
        } else {
            container.classList.remove('high-risk');
            legalAlertBox.style.display = 'none';
            btnEscalate.style.boxShadow = 'none';
        }

        // Approval button state
        if (isScoreFailed || isHighLegalRisk) {
            btnApprove.disabled = true;
            if (isScoreFailed && isHighLegalRisk) {
                btnApprove.title = 'Điểm < 5.0 và Rủi ro >= 8.0: Không đủ điều kiện phê duyệt Cấp 1.';
            } else if (isScoreFailed) {
                btnApprove.title = 'Điểm tổng hợp (' + finalScore.toFixed(1) + '/10) < 5.0: Chưa đạt ngưỡng tối thiểu phê duyệt.';
            } else {
                btnApprove.title = 'Rủi ro pháp lý >= 8.0: Bắt buộc chuyển lên TBT duyệt vượt cấp.';
            }
        } else {
            btnApprove.disabled = false;
            btnApprove.title = 'Đủ điều kiện phê duyệt Cấp 1 trực tiếp.';
        }
    }

    // 5. Submit Form with Chosen Decision Action
    function submitDecision(decision) {
        var finalScore = parseFloat(document.getElementById('totalWeightedScore').innerText) || 0.0;
        var legalScore = parseFloat(document.getElementById('legalRiskSlider').value) || 0.0;

        if (decision === 'APPROVED') {
            if (finalScore < 5.0) {
                alert('CẢNH BÁO BẢO MẬT: Điểm tổng hợp (' + finalScore.toFixed(1) + '/10) chưa đạt ngưỡng tối thiểu 5.0/10! Bạn chỉ có thể chọn "Yêu cầu chỉnh sửa đề cương" hoặc "Bác bỏ đề tài".');
                return;
            }
            if (legalScore >= 8.0) {
                alert('CẢNH BÁO BẢO MẬT: Đề tài có rủi ro pháp lý >= 8.0! Bạn phải chọn "Chuyển lên Tổng Biên Tập".');
                return;
            }
        }

        var comments = document.getElementById('editorialComments').value.trim();
        if ((decision === 'REVISION_REQUESTED' || decision === 'REJECTED') && comments.length === 0) {
            alert('Vui lòng nhập lý do và nhận xét cụ thể khi Yêu cầu chỉnh sửa hoặc Bác bỏ đề tài.');
            document.getElementById('editorialComments').focus();
            return;
        }

        var confirmMsg = '';
        if (decision === 'APPROVED') {
            confirmMsg = 'Xác nhận PHÊ DUYỆT đề tài #' + '${topic.topicId}' + ' và sinh mã Topic_Code chính thức?';
        } else if (decision === 'ESCALATED') {
            confirmMsg = 'Xác nhận CHUYỂN đề tài #' + '${topic.topicId}' + ' lên Tổng Biên Tập phê duyệt vượt cấp?';
        } else if (decision === 'REVISION_REQUESTED') {
            confirmMsg = 'Xác nhận gửi YÊU CẦU CHỈNH SỬA đề cương đến Phóng viên?';
        } else if (decision === 'REJECTED') {
            confirmMsg = 'Xác nhận BÁC BỎ đề tài #' + '${topic.topicId}' + ' và chuyển vào Kho lưu trữ ý tưởng?';
        }

        if (confirm(confirmMsg)) {
            document.getElementById('decisionInput').value = decision;
            document.getElementById('evalForm').submit();
        }
    }

    // Initial calculation on load
    document.addEventListener('DOMContentLoaded', function() {
        recalculateTotal();
        handleLegalRiskChange(document.getElementById('legalRiskSlider').value);
    });
</script>
</body>
</html>
