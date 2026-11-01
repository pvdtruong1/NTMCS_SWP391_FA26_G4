package controller;

import dal.CategoryDAO;
import dal.CriteriaDAO;
import dal.DeskDAO;
import dal.DeskQuotaDAO;
import dal.PermissionDAO;
import dal.RoleDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import model.Category;
import model.CriteriaConfig;
import model.Desk;
import model.DeskQuota;
import model.Permission;
import model.Role;
import model.User;
import util.ApiUtil;
import util.Json;

/**
 * GET /api/data : the WF-05 data the React front end shows, in one call.
 * Any logged-in user gets the lists every screen needs (roles, permissions, desks, categories,
 * criteria, quotas). The list of user accounts is only sent to roles with USER_MANAGE.
 * Field names are snake_case, the same as the database columns and the front end types.
 */
@WebServlet(name = "ApiDataServlet", urlPatterns = {"/api/data"})
public class ApiDataServlet extends HttpServlet {

    private final RoleDAO roleDAO = new RoleDAO();
    private final PermissionDAO permissionDAO = new PermissionDAO();
    private final DeskDAO deskDAO = new DeskDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final CriteriaDAO criteriaDAO = new CriteriaDAO();
    private final DeskQuotaDAO quotaDAO = new DeskQuotaDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = ApiUtil.requireLogin(request, response);
        if (admin == null) {
            return;
        }

        // roles
        List<Object> roles = new ArrayList<>();
        for (Role r : roleDAO.getAll()) {
            roles.add(Json.obj("role_id", r.getRoleId(), "role_name", r.getRoleName()));
        }

        // permissions
        List<Object> permissions = new ArrayList<>();
        for (Permission p : permissionDAO.getAll()) {
            permissions.add(Json.obj("permission_id", p.getPermissionId(),
                    "permission_code", p.getPermissionCode(),
                    "description", p.getDescription()));
        }

        // ticked boxes of the RBAC matrix: "roleId-permissionId" -> object
        List<Object> rolePermissions = new ArrayList<>();
        for (String pair : permissionDAO.getAllPairs()) {
            String[] parts = pair.split("-");
            rolePermissions.add(Json.obj("role_id", Integer.parseInt(parts[0]),
                    "permission_id", Integer.parseInt(parts[1])));
        }

        // desks (in id order)
        List<Desk> deskList = deskDAO.getAll();
        Collections.sort(deskList, (a, b) -> a.getDeskId() - b.getDeskId());
        List<Object> desks = new ArrayList<>();
        for (Desk d : deskList) {
            desks.add(Json.obj("desk_id", d.getDeskId(), "desk_name", d.getDeskName()));
        }

        // categories (in id order)
        List<Category> categoryList = categoryDAO.getAll();
        Collections.sort(categoryList, (a, b) -> a.getCategoryId() - b.getCategoryId());
        List<Object> categories = new ArrayList<>();
        for (Category c : categoryList) {
            categories.add(Json.obj("category_id", c.getCategoryId(),
                    "category_name", c.getCategoryName(),
                    "parent_category_id", c.getParentCategoryId(),
                    "desk_id", c.getDeskId()));
        }

        // evaluation criteria
        List<Object> criteria = new ArrayList<>();
        for (CriteriaConfig c : criteriaDAO.getAll()) {
            criteria.add(Json.obj("criteria_id", c.getCriteriaId(),
                    "criteria_name", c.getCriteriaName(),
                    "weight_percent", c.getWeightPercent()));
        }

        // desk quotas (in id order)
        List<DeskQuota> quotaList = quotaDAO.getAll();
        Collections.sort(quotaList, (a, b) -> a.getQuotaId() - b.getQuotaId());
        List<Object> quotas = new ArrayList<>();
        for (DeskQuota q : quotaList) {
            quotas.add(Json.obj("quota_id", q.getQuotaId(),
                    "desk_id", q.getDeskId(),
                    "period", q.getPeriod(),
                    "quota_count", q.getQuotaCount()));
        }

        Map<String, Object> data = Json.obj(
                "roles", roles,
                "permissions", permissions,
                "role_permissions", rolePermissions,
                "desks", desks,
                "categories", categories,
                "criteria_configs", criteria,
                "desk_quotas", quotas);

        // the account list is only for roles that may manage users
        if (permissionDAO.roleHasPermission(admin.getRoleId(), "USER_MANAGE")) {
            List<Object> users = new ArrayList<>();
            for (User u : userDAO.getAll()) {
                users.add(ApiUtil.userJson(u));
            }
            data.put("users", users);
        }

        ApiUtil.send(response, 200, data);
    }
}
