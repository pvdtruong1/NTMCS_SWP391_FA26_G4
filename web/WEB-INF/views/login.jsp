<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Đăng Nhập — NTMCS</title>
        <link rel="stylesheet" href="${base}/css/style.css">
    </head>
    <body style="display: flex; align-items: center; justify-content: center; min-height: 100vh; background: var(--bg); padding: 20px;">
        <div style="background: var(--surface); border: 1px solid var(--line); border-radius: var(--radius); box-shadow: var(--shadow); padding: 36px 32px; max-width: 480px; width: 100%;">
            <div style="width: 52px; height: 52px; border-radius: 14px; background: linear-gradient(135deg, #5574ef, #2b49b9); color: #fff; font-size: 26px; font-weight: 900; display: grid; place-items: center; margin: 0 auto 16px;">N</div>
            <h2 style="text-align: center; margin: 0 0 6px; font-size: 22px;">Đăng Nhập NTMCS</h2>
            <p style="text-align: center; color: var(--muted); font-size: 13px; margin: 0 0 20px;">Tòa Soạn Hội Tụ — Quản Lý &amp; Điều Phối Đề Tài</p>

            <c:if test="${not empty error}">
                <div style="background: #fff0f2; color: var(--danger); border: 1px solid #f0c8ce; border-radius: 8px; padding: 10px 14px; margin-bottom: 16px; font-size: 13px;">
                    <c:out value="${error}" />
                </div>
            </c:if>

            <form method="post" action="${base}/login">
                <div style="margin-bottom: 14px;">
                    <label style="display: block; font-weight: 600; margin-bottom: 6px; font-size: 13px;">Email Đăng Nhập</label>
                    <input type="email" name="email" id="emailInput" required style="width: 100%; padding: 10px 12px; border: 1px solid var(--line); border-radius: 8px; font-size: 14px; box-sizing: border-box;" placeholder="name@newsroom.vn">
                </div>
                <div style="margin-bottom: 20px;">
                    <label style="display: block; font-weight: 600; margin-bottom: 6px; font-size: 13px;">Mật Khẩu</label>
                    <input type="password" name="password" id="passInput" required style="width: 100%; padding: 10px 12px; border: 1px solid var(--line); border-radius: 8px; font-size: 14px; box-sizing: border-box;" placeholder="••••••">
                </div>
                <button type="submit" class="btn primary full" style="padding: 11px; font-size: 15px; font-weight: 700;">Đăng Nhập &rarr;</button>
            </form>

            <div style="margin-top: 24px; padding-top: 18px; border-top: 1px solid var(--line); font-size: 12px; color: var(--muted);">
                <b>Tài khoản thử nghiệm nhanh (Mật khẩu: <code>123</code>):</b>
                <div style="display: flex; flex-direction: column; gap: 6px; margin-top: 8px;">
                    <a href="javascript:void(0)" onclick="fill('cuong.admin@newsroom.vn','123')" style="color: var(--primary); text-decoration: none;">
                        🛡️ <b>Cường</b> (Quản trị hệ thống - WF-05): <code>cuong.admin@newsroom.vn</code>
                    </a>
                    <a href="javascript:void(0)" onclick="fill('tbt.exec@newsroom.vn','123')" style="color: var(--primary); text-decoration: none;">
                        👑 <b>Tổng Biên Tập</b> (Thẩm định tối cao): <code>tbt.exec@newsroom.vn</code>
                    </a>
                    <a href="javascript:void(0)" onclick="fill('truong.editor@newsroom.vn','123')" style="color: var(--primary); text-decoration: none;">
                        🏛️ <b>Trường</b> (Trưởng ban Biên tập - WF-02): <code>truong.editor@newsroom.vn</code>
                    </a>
                    <a href="javascript:void(0)" onclick="fill('hung.reporter@newsroom.vn','123')" style="color: var(--primary); text-decoration: none;">
                        ✍️ <b>Hưng</b> (Phóng viên - WF-01): <code>hung.reporter@newsroom.vn</code>
                    </a>
                    <a href="javascript:void(0)" onclick="fill('loc.logistics@newsroom.vn','123')" style="color: var(--primary); text-decoration: none;">
                        📦 <b>Lộc</b> (Điều phối Hậu cần - WF-03): <code>loc.logistics@newsroom.vn</code>
                    </a>
                    <a href="javascript:void(0)" onclick="fill('dat.fieldwork@newsroom.vn','123')" style="color: var(--primary); text-decoration: none;">
                        📌 <b>Đạt</b> (Điều phối Hiện trường - WF-04): <code>dat.fieldwork@newsroom.vn</code>
                    </a>
                </div>
            </div>
        </div>
        <script>
            function fill(e, p) {
                document.getElementById('emailInput').value = e;
                document.getElementById('passInput').value = p;
            }
        </script>
    </body>
</html>
