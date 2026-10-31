package controller;

import dal.AuditLogDAO;
import dal.CategoryDAO;
import dal.DeskDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import model.Category;
import model.Desk;
import model.User;
import util.ApiUtil;
import util.CategoryValidator;
import util.FormUtil;
import util.Json;

/**
 * JSON version of CategoryServlet (UC-04, SCR-5.4). The rules are the same (CategoryValidator).
 *   POST /api/categories  action=createDesk | updateDesk | deleteDesk
 *                                createCategory | updateCategory | deleteCategory
 *     createDesk     : desk_name
 *     updateDesk     : id, desk_name
 *     createCategory : category_name, desk_id, parent_category_id (empty = top level)
 *     updateCategory : id, category_name, desk_id, parent_category_id
 *     deleteDesk / deleteCategory : id
 * The lists themselves come from GET /api/data.
 */
@WebServlet(name = "ApiCategoryServlet", urlPatterns = {"/api/categories"})
public class ApiCategoryServlet extends HttpServlet {

    private static final String PERMISSION = "QUOTA_MANAGE";

    private final DeskDAO deskDAO = new DeskDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();
    private final CategoryValidator validator = new CategoryValidator();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User admin = ApiUtil.requirePermission(request, response, PERMISSION);
        if (admin == null) {
            return;
        }
        String action = request.getParameter("action");

        if ("createDesk".equals(action)) {
            createDesk(request, response, admin);
        } else if ("updateDesk".equals(action)) {
            updateDesk(request, response, admin);
        } else if ("deleteDesk".equals(action)) {
            deleteDesk(request, response, admin);
        } else if ("createCategory".equals(action)) {
            createCategory(request, response, admin);
        } else if ("updateCategory".equals(action)) {
            updateCategory(request, response, admin);
        } else if ("deleteCategory".equals(action)) {
            deleteCategory(request, response, admin);
        } else {
            ApiUtil.error(response, 400, "Unknown action.");
        }
    }

    // =====================================================================
    //  DESKS
    // =====================================================================
    private void createDesk(HttpServletRequest request, HttpServletResponse response, User admin)
            throws IOException {
        String name = FormUtil.text(request, "desk_name");
        String error = validator.validateDeskName(name, 0);
        if (error != null) {
            ApiUtil.error(response, 400, error);
            return;
        }
        int newId = deskDAO.insert(name);
        if (newId == 0) {
            ApiUtil.error(response, 500, "Could not save the desk. Please try again.");
            return;
        }
        auditDAO.log(admin.getUserId(), "CREATE_DESK", "DESK", newId, "Created desk " + name);
        ApiUtil.send(response, 200, Json.obj("ok", true, "desk_id", newId));
    }

    private void updateDesk(HttpServletRequest request, HttpServletResponse response, User admin)
            throws IOException {
        int id = FormUtil.toInt(request.getParameter("id"));
        Desk old = deskDAO.getById(id);
        if (old == null) {
            ApiUtil.error(response, 404, "Desk not found.");
            return;
        }
        String name = FormUtil.text(request, "desk_name");
        String error = validator.validateDeskName(name, id);
        if (error != null) {
            ApiUtil.error(response, 400, error);
            return;
        }
        if (name.equals(old.getDeskName())) {
            ApiUtil.send(response, 200, Json.obj("ok", true, "changed", false));
            return;
        }
        if (!deskDAO.updateName(id, name)) {
            ApiUtil.error(response, 500, "Could not save the desk. Please try again.");
            return;
        }
        auditDAO.log(admin.getUserId(), "UPDATE_DESK", "DESK", id,
                "Renamed desk \"" + old.getDeskName() + "\" to \"" + name + "\"");
        ApiUtil.send(response, 200, Json.obj("ok", true, "changed", true));
    }

    private void deleteDesk(HttpServletRequest request, HttpServletResponse response, User admin)
            throws IOException {
        Desk desk = deskDAO.getById(FormUtil.toInt(request.getParameter("id")));
        if (desk == null) {
            ApiUtil.error(response, 404, "Desk not found.");
            return;
        }
        String error = validator.checkDeskCanBeDeleted(desk);   // still in use?
        if (error != null) {
            ApiUtil.error(response, 400, error);
            return;
        }
        if (!deskDAO.delete(desk.getDeskId())) {
            ApiUtil.error(response, 500, "Could not delete the desk. Please try again.");
            return;
        }
        auditDAO.log(admin.getUserId(), "DELETE_DESK", "DESK", desk.getDeskId(),
                "Deleted desk " + desk.getDeskName());
        ApiUtil.send(response, 200, Json.obj("ok", true));
    }

    // =====================================================================
    //  CATEGORIES
    // =====================================================================
    private void createCategory(HttpServletRequest request, HttpServletResponse response, User admin)
            throws IOException {
        Category category = readCategory(request);
        category.setCategoryId(0);
        String error = validator.validateCategory(category);
        if (error != null) {
            ApiUtil.error(response, 400, error);
            return;
        }
        int newId = categoryDAO.insert(category);
        if (newId == 0) {
            ApiUtil.error(response, 500, "Could not save the category. Please try again.");
            return;
        }
        auditDAO.log(admin.getUserId(), "CREATE_CATEGORY", "CATEGORY", newId,
                "Created category " + category.getCategoryName());
        ApiUtil.send(response, 200, Json.obj("ok", true, "category_id", newId));
    }

    private void updateCategory(HttpServletRequest request, HttpServletResponse response, User admin)
            throws IOException {
        Category form = readCategory(request);
        Category old = categoryDAO.getById(form.getCategoryId());
        if (old == null) {
            ApiUtil.error(response, 404, "Category not found.");
            return;
        }
        String error = validator.validateCategory(form);
        if (error != null) {
            ApiUtil.error(response, 400, error);
            return;
        }
        boolean same = old.getCategoryName().equals(form.getCategoryName())
                && Objects.equals(old.getParentCategoryId(), form.getParentCategoryId())
                && old.getDeskId() == form.getDeskId();
        if (same) {
            ApiUtil.send(response, 200, Json.obj("ok", true, "changed", false));
            return;
        }
        if (!categoryDAO.update(form)) {
            ApiUtil.error(response, 500, "Could not save the category. Please try again.");
            return;
        }
        auditDAO.log(admin.getUserId(), "UPDATE_CATEGORY", "CATEGORY", form.getCategoryId(),
                "Updated category " + form.getCategoryName());
        ApiUtil.send(response, 200, Json.obj("ok", true, "changed", true));
    }

    private void deleteCategory(HttpServletRequest request, HttpServletResponse response, User admin)
            throws IOException {
        Category category = categoryDAO.getById(FormUtil.toInt(request.getParameter("id")));
        if (category == null) {
            ApiUtil.error(response, 404, "Category not found.");
            return;
        }
        String error = validator.checkCategoryCanBeDeleted(category);   // still in use?
        if (error != null) {
            ApiUtil.error(response, 400, error);
            return;
        }
        if (!categoryDAO.delete(category.getCategoryId())) {
            ApiUtil.error(response, 500, "Could not delete the category. Please try again.");
            return;
        }
        auditDAO.log(admin.getUserId(), "DELETE_CATEGORY", "CATEGORY", category.getCategoryId(),
                "Deleted category " + category.getCategoryName());
        ApiUtil.send(response, 200, Json.obj("ok", true));
    }

    /** Reads id, name, parent (optional) and desk from the request. */
    private Category readCategory(HttpServletRequest request) {
        Category c = new Category();
        c.setCategoryId(FormUtil.toInt(request.getParameter("id")));
        c.setCategoryName(FormUtil.text(request, "category_name"));
        String parent = FormUtil.text(request, "parent_category_id");
        if (!parent.isEmpty()) {
            c.setParentCategoryId(FormUtil.toInt(parent));
        }
        c.setDeskId(FormUtil.toInt(request.getParameter("desk_id")));
        return c;
    }
}
