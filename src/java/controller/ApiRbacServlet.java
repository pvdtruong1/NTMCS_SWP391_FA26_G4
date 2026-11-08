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
import util.ApiUtil;
import util.Json;

/**
 * JSON version of RbacServlet (UC-07).
 *   POST /api/rbac   rp = "roleId-permissionId" (repeat for every ticked box)
 * The whole matrix is saved in one transaction. The SYSTEM_ADMINISTRATOR column is never changed.
 */
@WebServlet(name = "ApiRbacServlet", urlPatterns = {"/api/rbac"})
public class ApiRbacServlet extends HttpServlet {

    private static final String PERMISSION = "RBAC_MANAGE";
    private static final String LOCKED_ROLE = "SYSTEM_ADMINISTRATOR";

    private final RoleDAO roleDAO = new RoleDAO();
    private final PermissionDAO permissionDAO = new PermissionDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User admin = ApiUtil.requirePermission(request, response, PERMISSION);
        if (admin == null) {
            return;
        }

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
        if (!ok) {
            ApiUtil.error(response, 500, "Could not save. Nothing was changed.");
            return;
        }

        int count = (pairs == null) ? 0 : pairs.length;
        auditDAO.log(admin.getUserId(), "UPDATE_RBAC", "RBAC", null,
                "Saved permission matrix (" + count + " permissions granted)");
        ApiUtil.send(response, 200, Json.obj("ok", true));
    }
}
