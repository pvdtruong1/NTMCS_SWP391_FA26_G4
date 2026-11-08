document.addEventListener("DOMContentLoaded", function () {

    // Xác nhận xóa tài sản
    document.querySelectorAll(".delete-form").forEach(function (form) {
        form.addEventListener("submit", function (event) {
            const confirmed = confirm(
                "Bạn có chắc chắn muốn xóa tài sản này khỏi hệ thống không?\nThao tác này chỉ thành công khi tài sản chưa có lịch sử mượn trả."
            );

            if (!confirmed) {
                event.preventDefault();
            }
        });
    });

    // Tự động ẩn thông báo sau 6 giây nếu có
    const notices = document.querySelectorAll(".notice");
    if (notices.length > 0) {
        setTimeout(function () {
            notices.forEach(function (el) {
                el.style.transition = "opacity 0.6s ease";
                el.style.opacity = "0.7";
            });
        }, 6000);
    }
});
