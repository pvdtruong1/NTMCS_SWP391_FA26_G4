package controller;

import dal.PermissionDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import model.User;
import util.AuthUtil;

/**
 * Home screen. Every logged-in user lands here after login.
 * It shows a link to each screen that the user's role is allowed to open.
 * A role with no screens yet sees a short message instead of an error.
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {

    private final PermissionDAO permissionDAO = new PermissionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = AuthUtil.getLoggedUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // read the role's permissions again, so a change in the RBAC matrix shows up here
        Set<String> codes = permissionDAO.getCodesByRole(user.getRoleId());
        request.getSession().setAttribute("permissions", codes);

        // each entry is { link text, url }
        List<String[]> screens = new ArrayList<>();
        if (codes.contains("USER_MANAGE")) {
            screens.add(new String[]{"User accounts", "/users"});
        }
        if (codes.contains("RBAC_MANAGE")) {
            screens.add(new String[]{"Roles and permissions (RBAC)", "/rbac"});
        }
        if (codes.contains("AUDIT_VIEW")) {
            screens.add(new String[]{"System audit log", "/audit-log"});
        }
        if (codes.contains("TOPIC_EVALUATE")) {
            screens.add(new String[]{"Hàng chờ thẩm định đề tài (Pending Queue)", "/editorial/pending-queue"});
            screens.add(new String[]{"Kho lưu trữ ý tưởng & Đề tài đình chỉ (Idea Archive)", "/editorial/idea-archive"});
            screens.add(new String[]{"Đặt hàng & Giao quyền đề tài chỉ đạo (Directive Topic)", "/editorial/directive-creation"});
        }
        if (codes.contains("QUOTA_MANAGE")) {
            screens.add(new String[]{"Executive dashboard", "/dashboard"});
            screens.add(new String[]{"Performance report builder", "/report"});
            screens.add(new String[]{"Desk quotas", "/quota"});
            screens.add(new String[]{"Evaluation criteria", "/criteria"});
            screens.add(new String[]{"Categories and desks", "/categories"});
        }
        // When another workflow adds a screen, add ONE more "if" here, for example:
        // if (codes.contains("TOPIC_PITCH")) { screens.add(new String[]{"Pitch a topic", "/topic-pitch"}); }

        request.setAttribute("screens", screens);
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
