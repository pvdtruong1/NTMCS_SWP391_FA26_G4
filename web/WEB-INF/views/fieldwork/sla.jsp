<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cấu Hình Hạn Mức SLA Hiện Trường — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
    <style>
        .sla-card {
            background: var(--surface);
            border: 1px solid var(--line);
            border-radius: 12px;
            padding: 24px;
            margin-bottom: 24px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.04);
        }
        .preview-box {
            background: #f8fafc;
            border: 1px solid var(--line);
            border-radius: 8px;
            padding: 12px 16px;
            font-size: 12px;
            color: #334155;
            margin-top: 6px;
            line-height: 1.5;
        }
    </style>
</head>
<body>
<div class="app">
    <jsp:include page="../sidebar.jsp">
        <jsp:param name="active" value="fieldwork-sla"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="../topbar.jsp">
            <jsp:param name="title" value="Cấu Hình Hạn Mức SLA Hiện Trường (SCR-4.6)"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">QUẢN TRỊ VẬN HÀNH &amp; CHÍNH SÁCH / WF-04</span>
                    <h1>Cấu Hình Hạn Mức SLA Hiện Trường (SCR-4.6)</h1>
                    <p>Thiết lập các mốc thời gian cảnh báo hạn chót tác nghiệp và cấu hình các mẫu thông điệp phát thanh tự động.</p>
                </div>
            </div>

            <div style="max-width: 860px; margin: 0 auto;">
                <form id="slaForm" onsubmit="saveSLA(event)">
                    <!-- Section 1: Activation & Thresholds -->
                    <div class="sla-card">
                        <h3 style="margin-top:0; margin-bottom:16px; font-size:16px; display:flex; align-items:center; gap:8px;">
                            ⏱️ Mốc Giờ Giám Sát Hạn Chót (SLA Deadlines)
                        </h3>

                        <div style="margin-bottom:20px; display:flex; align-items:center; gap:10px;">
                            <input type="checkbox" id="inAppEnabled" name="inAppEnabled" ${slaConfig.inAppEnabled ? 'checked' : ''} style="width:18px; height:18px; cursor:pointer;"/>
                            <label for="inAppEnabled" style="font-weight:600; font-size:13px; cursor:pointer;">
                                Kích hoạt cảnh báo tự động trong hệ thống ứng dụng (In-App Notification)
                            </label>
                        </div>

                        <div style="display:grid; grid-template-columns:repeat(3, 1fr); gap:16px;">
                            <div>
                                <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px; color:#2563eb;">
                                    Mốc 1: Cảnh báo sớm (giờ)
                                </label>
                                <input type="number" id="th1" name="thresholdHours1" value="${slaConfig.thresholdHours1}" min="1" max="168" required style="width:100%; padding:9px 12px; border:1px solid var(--line); border-radius:8px; font-size:14px; font-weight:600; box-sizing:border-box;" onchange="updatePreviews()"/>
                                <small style="color:var(--muted); font-size:11px;">Mặc định: 24 giờ trước hạn</small>
                            </div>

                            <div>
                                <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px; color:#d97706;">
                                    Mốc 2: Cảnh báo vàng (giờ)
                                </label>
                                <input type="number" id="th2" name="thresholdHours2" value="${slaConfig.thresholdHours2}" min="1" max="72" required style="width:100%; padding:9px 12px; border:1px solid var(--line); border-radius:8px; font-size:14px; font-weight:600; box-sizing:border-box;" onchange="updatePreviews()"/>
                                <small style="color:var(--muted); font-size:11px;">Mặc định: 8 giờ trước hạn</small>
                            </div>

                            <div>
                                <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px; color:#dc2626;">
                                    Mốc 3: Cảnh báo đỏ (giờ)
                                </label>
                                <input type="number" id="th3" name="thresholdHours3" value="${slaConfig.thresholdHours3}" min="1" max="24" required style="width:100%; padding:9px 12px; border:1px solid var(--line); border-radius:8px; font-size:14px; font-weight:600; box-sizing:border-box;" onchange="updatePreviews()"/>
                                <small style="color:var(--muted); font-size:11px;">Mặc định: 2 giờ trước hạn</small>
                            </div>
                        </div>

                        <div style="margin-top:12px; padding:10px 14px; background:#eff6ff; border-radius:8px; font-size:12px; color:#1e40af;">
                            ℹ️ Quy tắc hợp lệ: <b>Mốc cảnh báo sớm &gt; Mốc cảnh báo vàng &gt; Mốc cảnh báo đỏ &gt; 0</b>.
                        </div>
                    </div>

                    <!-- Section 2: Templates -->
                    <div class="sla-card">
                        <h3 style="margin-top:0; margin-bottom:16px; font-size:16px; display:flex; align-items:center; gap:8px;">
                            💬 Mẫu Lời Nhắn &amp; Thông Điệp Phát Thanh (Templates)
                        </h3>
                        <p style="font-size:12px; color:var(--muted); margin-top:0; margin-bottom:18px;">
                            Các biến động được hệ thống điền tự động: <code>{topicCode}</code>, <code>{hours}</code>, <code>{deadline}</code>, <code>{taskDescription}</code>, <code>{reporterName}</code>.
                        </p>

                        <!-- Template 1 -->
                        <div style="margin-bottom:20px;">
                            <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px; color:#2563eb;">
                                1. Mẫu cảnh báo sớm ({hours}h)
                            </label>
                            <textarea id="tpl1" name="earlyWarningTemplate" rows="2" required style="width:100%; padding:10px 12px; border:1px solid var(--line); border-radius:8px; font-size:13px; box-sizing:border-box;" onkeyup="updatePreviews()"><c:out value="${slaConfig.earlyWarningTemplate}" /></textarea>
                            <div class="preview-box" id="prev1"></div>
                        </div>

                        <!-- Template 2 -->
                        <div style="margin-bottom:20px;">
                            <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px; color:#d97706;">
                                2. Mẫu cảnh báo vàng ({hours}h)
                            </label>
                            <textarea id="tpl2" name="warningTemplate" rows="2" required style="width:100%; padding:10px 12px; border:1px solid var(--line); border-radius:8px; font-size:13px; box-sizing:border-box;" onkeyup="updatePreviews()"><c:out value="${slaConfig.warningTemplate}" /></textarea>
                            <div class="preview-box" id="prev2"></div>
                        </div>

                        <!-- Template 3 -->
                        <div style="margin-bottom:20px;">
                            <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px; color:#dc2626;">
                                3. Mẫu cảnh báo đỏ khẩn cấp ({hours}h)
                            </label>
                            <textarea id="tpl3" name="urgentTemplate" rows="2" required style="width:100%; padding:10px 12px; border:1px solid var(--line); border-radius:8px; font-size:13px; box-sizing:border-box;" onkeyup="updatePreviews()"><c:out value="${slaConfig.urgentTemplate}" /></textarea>
                            <div class="preview-box" id="prev3"></div>
                        </div>

                        <!-- Template 4 -->
                        <div style="margin-bottom:20px;">
                            <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px; color:#7c3aed;">
                                4. Mẫu đôn đốc tác nghiệp chủ động (Nudge Desk)
                            </label>
                            <textarea id="tpl4" name="nudgeTemplate" rows="2" required style="width:100%; padding:10px 12px; border:1px solid var(--line); border-radius:8px; font-size:13px; box-sizing:border-box;" onkeyup="updatePreviews()"><c:out value="${slaConfig.nudgeTemplate}" /></textarea>
                            <div class="preview-box" id="prev4"></div>
                        </div>

                        <div style="display:flex; justify-content:flex-end; gap:10px; margin-top:24px;">
                            <button type="submit" class="btn primary" id="btnSaveSLA" style="padding:10px 24px; font-size:14px; font-weight:700;">
                                💾 Lưu Cấu Hình SLA
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </main>
    </div>
</div>

<script>
function renderPreview(text, hours) {
    if (!text) return '—';
    return text
        .replaceAll('{topicCode}', 'TOPIC-202611-0001')
        .replaceAll('{hours}', hours)
        .replaceAll('{deadline}', '03/11/2026 17:00')
        .replaceAll('{taskDescription}', 'Thu thập tư liệu hiện trường')
        .replaceAll('{reporterName}', 'Nguyễn Văn A');
}

function updatePreviews() {
    const h1 = document.getElementById('th1').value;
    const h2 = document.getElementById('th2').value;
    const h3 = document.getElementById('th3').value;

    const t1 = document.getElementById('tpl1').value;
    const t2 = document.getElementById('tpl2').value;
    const t3 = document.getElementById('tpl3').value;
    const t4 = document.getElementById('tpl4').value;

    document.getElementById('prev1').innerHTML = '<b>Hiển thị mẫu:</b> ' + renderPreview(t1, h1);
    document.getElementById('prev2').innerHTML = '<b>Hiển thị mẫu:</b> ' + renderPreview(t2, h2);
    document.getElementById('prev3').innerHTML = '<b>Hiển thị mẫu:</b> ' + renderPreview(t3, h3);
    document.getElementById('prev4').innerHTML = '<b>Hiển thị mẫu:</b> ' + renderPreview(t4, '—');
}

window.addEventListener('DOMContentLoaded', updatePreviews);

async function saveSLA(e) {
    e.preventDefault();
    const btn = document.getElementById('btnSaveSLA');
    btn.disabled = true;

    try {
        const inAppEnabled = document.getElementById('inAppEnabled').checked;
        const thresholdHours1 = parseInt(document.getElementById('th1').value, 10);
        const thresholdHours2 = parseInt(document.getElementById('th2').value, 10);
        const thresholdHours3 = parseInt(document.getElementById('th3').value, 10);

        if (!(thresholdHours1 > thresholdHours2 && thresholdHours2 > thresholdHours3 && thresholdHours3 > 0)) {
            alert('Lỗi: Thứ tự mốc thời gian phải là Mốc 1 > Mốc 2 > Mốc 3 > 0.');
            btn.disabled = false;
            return;
        }

        const earlyWarningTemplate = document.getElementById('tpl1').value.trim();
        const warningTemplate = document.getElementById('tpl2').value.trim();
        const urgentTemplate = document.getElementById('tpl3').value.trim();
        const nudgeTemplate = document.getElementById('tpl4').value.trim();

        const payload = {
            inAppEnabled,
            thresholdHours1,
            thresholdHours2,
            thresholdHours3,
            earlyWarningTemplate,
            warningTemplate,
            urgentTemplate,
            nudgeTemplate
        };

        const resp = await fetch('${base}/api/sla-configs', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const res = await resp.json();
        if (resp.ok) {
            alert(res.message || 'Lưu cấu hình SLA thành công!');
        } else {
            alert(res.error || 'Lỗi lưu cấu hình.');
        }
    } catch (e) {
        alert('Lỗi kết nối: ' + e.message);
    } finally {
        btn.disabled = false;
    }
}
</script>
</body>
</html>
