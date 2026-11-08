package controller;

import dal.AuditLogDAO;
import dal.PermissionDAO;
import dal.RoleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import model.Role;
import model.User;
import util.AuthUtil;

/**
 * UC-07 Configure RBAC Permission (SCR-5.7).
 * Shows a matrix: rows = permissions, columns = roles, checkbox = allowed.
 * The SYSTEM_ADMINISTRATOR column is locked so nobody can lock themselves out.
 */
@WebServlet(name = "RbacServlet", urlPatterns = {"/rbac"})
public class RbacServlet extends HttpServlet {

    private static final String PERMISSION = "RBAC_MANAGE";
    private static final String LOCKED_ROLE = "SYSTEM_ADMINISTRATOR";

    private final RoleDAO roleDAO = new RoleDAO();
    private final PermissionDAO permissionDAO = new PermissionDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.check(request, response, PERMISSION)) {
            return;
        }
        request.setAttribute("roleList", roleDAO.getAll());
        request.setAttribute("permissionList", permissionDAO.getAll());
        request.setAttribute("pairs", permissionDAO.getAllPairs());
        request.getRequestDispatcher("/WEB-INF/views/rbac.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.check(request, response, PERMISSION)) {
            return;
        }
        User admin = AuthUtil.getLoggedUser(request);

        // find the id of the locked role
        int lockedRoleId = 0;
        List<Role> roles = roleDAO.getAll();
        for (Role r : roles) {
            if (LOCKED_ROLE.equals(r.getRoleName())) {
                lockedRoleId = r.getRoleId();
            }
        }

        String[] pairs = request.getParameterValues("rp");   // e.g. "2-1", "2-3"
        boolean ok = permissionDAO.saveMatrix(pairs, lockedRoleId);

        if (ok) {
            int count = (pairs == null) ? 0 : pairs.length;
            auditDAO.log(admin.getUserId(), "UPDATE_RBAC", "RBAC", null,
                    "Saved permission matrix (" + count + " permissions granted)");
        }
        response.sendRedirect(request.getContextPath() + "/rbac?msg=" + (ok ? "saved" : "failed"));
    }
}
