<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thẩm Định Nghiệm Thu Bản Thảo #${deliverable.topicId} | NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
    <style>
        .review-layout {
            display: grid;
            grid-template-columns: 1.6fr 1fr;
            gap: 22px;
            align-items: start;
        }
        @media (max-width: 1080px) {
            .review-layout {
                grid-template-columns: 1fr;
            }
        }
        .meta-pill {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 4px 10px;
            border-radius: 99px;
            font-size: 11px;
            font-weight: 700;
            background: var(--surface2);
            border: 1px solid var(--line);
        }
        .file-box {
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 16px 18px;
            margin-top: 12px;
        }
        .rule01-box-pass {
            background: #e8f7ef;
            border: 1px solid #ccebdd;
            color: #15734f;
            border-radius: 12px;
            padding: 14px 16px;
            margin-bottom: 18px;
        }
        .rule01-box-block {
            background: #fef2f2;
            border: 1px solid #fecaca;
            color: #991b1b;
            border-radius: 12px;
            padding: 14px 16px;
            margin-bottom: 18px;
        }
        .rubric-mini-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 13px;
        }
        .rubric-mini-table th, .rubric-mini-table td {
            padding: 8px 10px;
            border-bottom: 1px solid var(--line);
        }
        .rubric-mini-table th {
            font-size: 11px;
            text-transform: uppercase;
            color: var(--muted);
            background: var(--surface2);
        }
        .action-card {
            border: 1px solid var(--line);
            border-radius: var(--radius);
            padding: 18px;
            background: var(--surface);
            box-shadow: var(--shadow);
            margin-bottom: 18px;
        }
        .form-control-text {
            width: 100%;
            border: 1px solid var(--line);
            border-radius: 8px;
            padding: 10px 12px;
            font-size: 13px;
            box-sizing: border-box;
            font-family: inherit;
            resize: vertical;
        }
        .form-control-text:focus {
            outline: none;
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(52, 88, 212, 0.15);
        }
    </style>
</head>
<body>
<div class="app">
    <jsp:include page="/WEB-INF/views/sidebar.jsp">
        <jsp:param name="active" value="editorial-dashboard"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="/WEB-INF/views/topbar.jsp">
            <jsp:param name="title" value="🏛️ ${deliverable.deskName != null ? deliverable.deskName : 'Ban Thời sự - Chính trị'}"/>
        </jsp:include>

        <main>
            <!-- Page Header -->
            <div class="page-head" style="margin-bottom: 22px;">
                <div>
                    <span class="eyebrow">EDITORIAL WORKFLOW / SCR-2.6 &amp; RULE-02</span>
                    <h1>Thẩm Định Nghiệm Thu Bản Thảo Bài Viết</h1>
                    <p>Đối soát chốt chặn an toàn tài sản (RULE-01), thẩm định chất lượng bản thảo và cấp mã HMAC-SHA256 Token chuyển giao CMS.</p>
                </div>
                <div class="page-actions">
                    <a href="${base}/editorial/dashboard" class="btn" style="text-decoration:none;">
                        &larr; Bàn Nghiệm Thu Dashboard
                    </a>
                </div>
            </div>

            <!-- Flash Error Alert -->
            <c:if test="${not empty error}">
                <div class="alert alert-danger" style="margin-bottom: 20px; background:#fef2f2; border:1px solid #fecaca; color:#991b1b; padding:12px 16px; border-radius:10px;">
                    <strong>Lỗi:</strong> <c:out value="${error}" />
                </div>
            </c:if>

            <div class="review-layout">
                <!-- ================= LEFT COLUMN: HỒ SƠ BẢN THẢO & TÁC PHẨM ================= -->
                <div>
                    <!-- Card 1: Thông tin đề tài đã duyệt -->
                    <div class="card" style="margin-bottom: 20px;">
                        <div class="card-head" style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom: 12px;">
                            <div>
                                <span class="meta-pill" style="color:var(--primary); background:#eef2ff; border-color:#d1dcff; margin-bottom:6px;">
                                    ${not empty deliverable.topicCode ? deliverable.topicCode : ('#TOPIC-' + deliverable.topicId)}
                                </span>
                                <h2 style="font-size: 20px; margin: 4px 0 8px; line-height: 1.3;">
                                    <c:out value="${deliverable.title}" />
                                </h2>
                            </div>
                            <span class="badge b-delivered" style="font-size: 11px; padding: 6px 12px;">
                                DELIVERED (Đã Nộp Bài)
                            </span>
                        </div>

                        <div style="font-size: 14px; color: #334155; line-height: 1.6; margin-bottom: 16px; background: var(--surface2); padding: 12px 14px; border-radius: 10px; border-left: 4px solid var(--primary);">
                            <b>Góc tiếp cận đã duyệt:</b><br>
                            <c:out value="${deliverable.angle}" />
                        </div>

                        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 12px; font-size: 13px;">
                            <div>
                                <small style="color: var(--muted); display: block;">Chuyên mục</small>
                                <b><c:out value="${deliverable.categoryName}" /></b>
                            </div>
                            <div>
                                <small style="color: var(--muted); display: block;">Phòng ban chuyên môn</small>
                                <b><c:out value="${deliverable.deskName}" /></b>
                            </div>
                            <div>
                                <small style="color: var(--muted); display: block;">Trưởng nhóm / Tác giả</small>
                                <b><c:out value="${deliverable.leadReporterName}" /></b>
                            </div>
                            <div>
                                <small style="color: var(--muted); display: block;">Hạn chót ban đầu</small>
                                <b><c:out value="${deliverable.deadline}" /></b>
                            </div>
                        </div>
                    </div>

                    <!-- Card 2: Sản phẩm bản thảo nộp từ phóng viên -->
                    <div class="card" style="margin-bottom: 20px;">
                        <h2 style="font-size: 16px; margin: 0 0 14px; display:flex; align-items:center; gap:8px;">
                            <span>📄</span> Sản Phẩm Tác Nghiệp &amp; Bản Thảo Đính Kèm
                        </h2>

                        <c:choose>
                            <c:when test="${not empty submissions}">
                                <c:forEach var="sub" items="${submissions}">
                                    <div class="file-box">
                                        <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom: 10px;">
                                            <div>
                                                <span class="meta-pill" style="background:#e0f2fe; color:#0369a1; border-color:#bae6fd;">
                                                    ${sub.submissionType == 'FILE' ? '📎 FILE VĂN BẢN' : '🌐 LIÊN KẾT TRỰC TUYẾN'}
                                                </span>
                                                <small style="color: var(--muted); margin-left: 8px;">
                                                    Nộp lúc: <fmt:formatDate value="${sub.submittedAt}" pattern="dd/MM/yyyy HH:mm" />
                                                    &bull; Bởi: <b><c:out value="${sub.submitterName}" /></b>
                                                </small>
                                            </div>
                                        </div>

                                        <c:if test="${not empty sub.deliverableNote}">
                                            <div style="font-size: 13px; color: #1e293b; margin-bottom: 12px; background: #fff; padding: 10px 12px; border-radius: 8px; border: 1px solid var(--line);">
                                                <b>Ghi chú từ phóng viên:</b><br>
                                                <c:out value="${sub.deliverableNote}" />
                                            </div>
                                        </c:if>

                                        <!-- Preview / Download Section -->
                                        <c:choose>
                                            <c:when test="${sub.submissionType == 'LINK'}">
                                                <div style="display:flex; align-items:center; gap:12px; margin-top: 8px;">
                                                    <a href="<c:out value='${sub.externalUrl}' />" target="_blank" rel="noopener noreferrer"
                                                       class="btn primary" style="text-decoration:none; display:inline-flex; align-items:center; gap:8px; font-size:13px;">
                                                        <span>🌐</span> Mở Bản Thảo Trên Trình Duyệt (Tab Mới) &rarr;
                                                    </a>
                                                    <span style="font-size:12px; color:var(--muted); word-break:break-all;">
                                                        <c:out value="${sub.externalUrl}" />
                                                    </span>
                                                </div>
                                            </c:when>
                                            <c:when test="${sub.submissionType == 'FILE'}">
                                                <div style="display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:12px;">
                                                    <div>
                                                        <div style="font-weight: 700; font-size: 14px; color: var(--text);">
                                                            📄 <c:out value="${sub.originalFileName}" />
                                                        </div>
                                                        <small style="color: var(--muted);">
                                                            Định dạng: <code><c:out value="${sub.contentType}" /></code> &bull;
                                                            Dung lượng: <fmt:formatNumber value="${sub.fileSizeBytes / 1024}" maxFractionDigits="1" /> KB
                                                        </small>
                                                    </div>
                                                    <div>
                                                        <a href="${base}/${sub.storedFilePath}" download="<c:out value='${sub.originalFileName}' />"
                                                           class="btn primary" style="text-decoration:none; display:inline-flex; align-items:center; gap:6px; font-size:13px;">
                                                            <span>📥</span> Tải Bản Thảo Về Máy
                                                        </a>
                                                    </div>
                                                </div>

                                                <!-- PDF Native Embed Preview if PDF -->
                                                <c:if test="${fn:endsWith(fn:toLowerCase(sub.originalFileName), '.pdf')}">
                                                    <div style="margin-top: 14px; border: 1px solid var(--line); border-radius: 8px; overflow: hidden;">
                                                        <iframe src="${base}/${sub.storedFilePath}" width="100%" height="520px" style="border:none;"></iframe>
                                                    </div>
                                                </c:if>
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div style="text-align:center; padding: 24px; color: var(--muted); background: var(--surface2); border-radius: 10px;">
                                    Phóng viên chưa đính kèm file hoặc liên kết bài viết cụ thể.
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Card 3: Thành viên nhóm tác nghiệp hiện trường -->
                    <div class="card">
                        <h2 style="font-size: 16px; margin: 0 0 12px; display:flex; align-items:center; gap:8px;">
                            <span>👥</span> Đội Ngũ Tác Nghiệp Hiện Trường
                        </h2>
                        <c:choose>
                            <c:when test="${not empty fieldMembers}">
                                <div style="display:flex; flex-wrap:wrap; gap:10px;">
                                    <c:forEach var="m" items="${fieldMembers}">
                                        <div style="display:flex; align-items:center; gap:8px; background:var(--surface2); border:1px solid var(--line); border-radius:10px; padding:8px 12px;">
                                            <div style="width:28px; height:28px; border-radius:50%; background:var(--primary); color:#fff; display:grid; place-items:center; font-weight:700; font-size:11px;">
                                                ${m.userName.substring(0, 1)}
                                            </div>
                                            <div>
                                                <div style="font-size:13px; font-weight:600;"><c:out value="${m.userName}" /></div>
                                                <small style="color:var(--muted);"><c:out value="${m.roleInTeam}" /></small>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div style="color:var(--muted); font-size:13px;">Chưa phân công nhân sự bổ sung ngoài tác giả chính.</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- ================= RIGHT COLUMN: CHỐT CHẶN & THAO TÁC NGHIỆM THU ================= -->
                <div>
                    <!-- Box 1: RULE-01 Asset Interlock Guard -->
                    <c:choose>
                        <c:when test="${deliverable.rule01Passed}">
                            <div class="rule01-box-pass">
                                <div style="display:flex; align-items:center; gap:8px; font-weight:700; font-size:14px; margin-bottom:4px;">
                                    <span>✓</span> CHỐT CHẶN RULE-01: ĐẠT TIÊU CHUẨN
                                </div>
                                <div style="font-size: 12px; line-height: 1.4;">
                                    100% thiết bị, phương tiện tác nghiệp mượn đã được hoàn trả về kho an toàn. Đề tài đủ điều kiện pháp lý để nghiệm thu CMS.
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="rule01-box-block">
                                <div style="display:flex; align-items:center; gap:8px; font-weight:700; font-size:14px; margin-bottom:6px;">
                                    <span>🔒</span> KHÓA CHẶN RULE-01: VI PHẠM TÀI SẢN
                                </div>
                                <div style="font-size: 12px; line-height: 1.4; margin-bottom: 8px;">
                                    Còn <b>${deliverable.unreturnedAssetCount}</b> tài sản/xe tác nghiệp chưa hoàn trả kho:
                                </div>
                                <ul style="margin: 0; padding-left: 18px; font-size: 12px;">
                                    <c:forEach var="asset" items="${deliverable.unreturnedAssetDetails}">
                                        <li><b><c:out value="${asset}" /></b></li>
                                    </c:forEach>
                                </ul>
                                <div style="margin-top: 8px; font-size: 11px; color: #7f1d1d; font-style: italic;">
                                    Nút Nghiệm Thu CMS bị khóa cứng theo quy chế tòa soạn cho đến khi thủ kho xác nhận hoàn trả.
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <!-- Box 2: Đối chiếu Tiêu chí Rubric Vòng 1 -->
                    <c:if test="${not empty stage1Dossier and not empty stage1Dossier.criteriaList}">
                        <div class="card" style="margin-bottom: 20px;">
                            <h2 style="font-size: 14px; margin: 0 0 10px; display:flex; align-items:center; justify-content:space-between;">
                                <span>⚖️ Điểm Rubric Vòng 1 (Tham Chiếu)</span>
                                <span class="badge badge-primary">
                                    <fmt:formatNumber value="${stage1Dossier.compositeScore}" maxFractionDigits="1" /> / 10.0
                                </span>
                            </h2>
                            <table class="rubric-mini-table">
                                <thead>
                                    <tr>
                                        <th>Tiêu chí</th>
                                        <th style="width: 50px; text-align: center;">Tỉ trọng</th>
                                        <th style="width: 50px; text-align: right;">Điểm</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="c" items="${stage1Dossier.criteriaList}">
                                        <tr>
                                            <td><c:out value="${c.criteriaName}" /></td>
                                            <td style="text-align: center;"><fmt:formatNumber value="${c.weightPercent}" maxFractionDigits="0" />%</td>
                                            <td style="text-align: right; font-weight: 700;">
                                                <c:set var="sc" value="${stage1Dossier.evaluatedScores[c.criteriaId]}" />
                                                <c:choose>
                                                    <c:when test="${not empty sc}"><fmt:formatNumber value="${sc}" maxFractionDigits="1" /></c:when>
                                                    <c:otherwise>&mdash;</c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:if>

                    <!-- Box 3: Form Nghiệm Thu CMS (Hành động 1 - Zero Popups) -->
                    <div class="action-card" style="border-top: 4px solid var(--success, #16845b);">
                        <h3 style="font-size: 15px; margin: 0 0 6px; color: #16845b; display:flex; align-items:center; gap:8px;">
                            <span>🚀</span> Phê Duyệt Nghiệm Thu Bàn Giao CMS
                        </h3>
                        <p style="font-size: 12px; color: var(--muted); margin: 0 0 12px;">
                            Phát hành mã bảo mật HMAC-SHA256 Token và chuyển trạng thái đề tài sang <b>ACCEPTED_READY_FOR_CMS</b>.
                        </p>

                        <form method="post" action="${base}/editorial/deliverable-review">
                            <input type="hidden" name="action" value="accept_cms">
                            <input type="hidden" name="topicId" value="${deliverable.topicId}">

                            <div style="margin-bottom: 12px;">
                                <label style="font-size: 12px; font-weight: 600; display: block; margin-bottom: 4px;">Ghi chú nghiệm thu xuất bản:</label>
                                <textarea name="acceptanceNote" rows="3" class="form-control-text" placeholder="Nhập nhận xét chất lượng bài viết hoặc chỉ đạo xuất bản..."></textarea>
                            </div>

                            <c:choose>
                                <c:when test="${deliverable.rule01Passed}">
                                    <button type="submit" class="btn primary full" style="background:#16845b; border-color:#16845b; padding: 11px; font-size: 14px; font-weight: 700;">
                                        🚀 Xác Nhận Nghiệm Thu &amp; Cấp Token CMS
                                    </button>
                                </c:when>
                                <c:otherwise>
                                    <button type="button" class="btn full" disabled
                                            style="background:#cbd5e1; color:#64748b; cursor:not-allowed; padding:11px; font-size:14px; font-weight:700;"
                                            title="Khóa theo RULE-01: Phóng viên phải hoàn trả thiết bị trước khi nghiệm thu!">
                                        🔒 Khóa Nghiệm Thu (Chưa Hoàn Kho Tài Sản)
                                    </button>
                                </c:otherwise>
                            </c:choose>
                        </form>
                    </div>

                    <!-- Box 4: Form Yêu Cầu Chỉnh Sửa (Hành động 2 - Zero Popups) -->
                    <div class="action-card" style="border-top: 4px solid var(--warning, #b26a00);">
                        <h3 style="font-size: 15px; margin: 0 0 6px; color: #b26a00; display:flex; align-items:center; gap:8px;">
                            <span>↩️</span> Yêu Cầu Chỉnh Sửa Hoàn Thiện
                        </h3>
                        <p style="font-size: 12px; color: var(--muted); margin: 0 0 12px;">
                            Chuyển đề tài về trạng thái <b>REVISION_REQUESTED</b> và gửi thông báo yêu cầu phóng viên sửa lại bài viết.
                        </p>

                        <form method="post" action="${base}/editorial/deliverable-review">
                            <input type="hidden" name="action" value="request_revision">
                            <input type="hidden" name="topicId" value="${deliverable.topicId}">

                            <div style="margin-bottom: 12px;">
                                <label style="font-size: 12px; font-weight: 600; display: block; margin-bottom: 4px;">Ý kiến chỉ đạo hoàn thiện lại bản thảo (Bắt buộc):</label>
                                <textarea name="revisionFeedback" rows="3" required class="form-control-text" placeholder="Chỉ rõ các đoạn cần bổ sung chứng cứ, số liệu, biên tập văn phong..."></textarea>
                            </div>

                            <button type="submit" class="btn full" style="background:#fff4df; color:#b26a00; border-color:#f4ddb2; padding: 10px; font-size: 13px; font-weight: 700;">
                                ↩️ Gửi Yêu Cầu Chỉnh Sửa Bản Thảo
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>
</body>
</html>
