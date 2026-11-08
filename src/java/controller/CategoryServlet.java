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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import model.Category;
import model.Desk;
import model.User;
import util.AuthUtil;
import util.CategoryValidator;
import util.FormUtil;

/**
 * UC-04 Manage Category Tree (SCR-5.4): categories and desks on one screen.
 *   GET  /categories                       -> desks table + category tree
 *   GET  /categories?action=newDesk        -> desk form      (editDesk&id=  to edit)
 *   GET  /categories?action=newCategory    -> category form  (editCategory&id=  to edit)
 *   POST /categories  action=createDesk | updateDesk | deleteDesk
 *                            createCategory | updateCategory | deleteCategory
 */
@WebServlet(name = "CategoryServlet", urlPatterns = {"/categories"})
public class CategoryServlet extends HttpServlet {

    private static final String PERMISSION = "QUOTA_MANAGE";

    private final DeskDAO deskDAO = new DeskDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();
    private final CategoryValidator validator = new CategoryValidator();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.check(request, response, PERMISSION)) {
            return;
        }
        String action = request.getParameter("action");

        if ("newDesk".equals(action)) {
            showDeskForm(request, response, new Desk());
        } else if ("editDesk".equals(action)) {
            Desk desk = deskDAO.getById(FormUtil.toInt(request.getParameter("id")));
            if (desk == null) {
                response.sendRedirect(request.getContextPath() + "/categories");
                return;
            }
            showDeskForm(request, response, desk);
        } else if ("newCategory".equals(action)) {
            showCategoryForm(request, response, new Category());
        } else if ("editCategory".equals(action)) {
            Category category = categoryDAO.getById(FormUtil.toInt(request.getParameter("id")));
            if (category == null) {
                response.sendRedirect(request.getContextPath() + "/categories");
                return;
            }
            showCategoryForm(request, response, category);
        } else {
            showList(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!AuthUtil.check(request, response, PERMISSION)) {
            return;
        }
        String action = request.getParameter("action");

        if ("createDesk".equals(action)) {
            createDesk(request, response);
        } else if ("updateDesk".equals(action)) {
            updateDesk(request, response);
        } else if ("deleteDesk".equals(action)) {
            deleteDesk(request, response);
        } else if ("createCategory".equals(action)) {
            createCategory(request, response);
        } else if ("updateCategory".equals(action)) {
            updateCategory(request, response);
        } else if ("deleteCategory".equals(action)) {
            deleteCategory(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/categories");
        }
    }

    // =====================================================================
    //  DESKS
    // =====================================================================
    private void createDesk(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = AuthUtil.getLoggedUser(request);
        Desk desk = new Desk();
        desk.setDeskName(FormUtil.text(request, "deskName"));

        String error = validator.validateDeskName(desk.getDeskName(), 0);
        if (error != null) {
            request.setAttribute("error", error);
            showDeskForm(request, response, desk);
            return;
        }
        int newId = deskDAO.insert(desk.getDeskName());
        if (newId == 0) {
            request.setAttribute("error", "Could not save the desk. Please try again.");
            showDeskForm(request, response, desk);
            return;
        }
        auditDAO.log(admin.getUserId(), "CREATE_DESK", "DESK", newId,
                "Created desk " + desk.getDeskName());
        response.sendRedirect(request.getContextPath() + "/categories?msg=deskCreated");
    }

    private void updateDesk(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = AuthUtil.getLoggedUser(request);
        int id = FormUtil.toInt(request.getParameter("id"));
        Desk old = deskDAO.getById(id);
        if (old == null) {
            response.sendRedirect(request.getContextPath() + "/categories");
            return;
        }
        Desk form = new Desk(id, FormUtil.text(request, "deskName"));

        String error = validator.validateDeskName(form.getDeskName(), id);
        if (error != null) {
            request.setAttribute("error", error);
            showDeskForm(request, response, form);
            return;
        }
        if (form.getDeskName().equals(old.getDeskName())) {
            response.sendRedirect(request.getContextPath() + "/categories?msg=nochange");
            return;
        }
        if (!deskDAO.updateName(id, form.getDeskName())) {
            request.setAttribute("error", "Could not save the desk. Please try again.");
            showDeskForm(request, response, form);
            return;
        }
        auditDAO.log(admin.getUserId(), "UPDATE_DESK", "DESK", id,
                "Renamed desk \"" + old.getDeskName() + "\" to \"" + form.getDeskName() + "\"");
        response.sendRedirect(request.getContextPath() + "/categories?msg=deskUpdated");
    }

    private void deleteDesk(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = AuthUtil.getLoggedUser(request);
        int id = FormUtil.toInt(request.getParameter("id"));
        Desk desk = deskDAO.getById(id);
        if (desk == null) {
            response.sendRedirect(request.getContextPath() + "/categories");
            return;
        }

        // a desk that is still in use cannot be deleted
        String error = validator.checkDeskCanBeDeleted(desk);
        if (error != null) {
            request.setAttribute("error", error);
            showList(request, response);
            return;
        }
        if (!deskDAO.delete(id)) {
            request.setAttribute("error", "Could not delete the desk. Please try again.");
            showList(request, response);
            return;
        }
        auditDAO.log(admin.getUserId(), "DELETE_DESK", "DESK", id,
                "Deleted desk " + desk.getDeskName());
        response.sendRedirect(request.getContextPath() + "/categories?msg=deskDeleted");
    }

    // =====================================================================
    //  CATEGORIES
    // =====================================================================
    private void createCategory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = AuthUtil.getLoggedUser(request);
        Category category = readCategoryForm(request);

        String error = validator.validateCategory(category);
        if (error != null) {
            request.setAttribute("error", error);
            showCategoryForm(request, response, category);
            return;
        }
        int newId = categoryDAO.insert(category);
        if (newId == 0) {
            request.setAttribute("error", "Could not save the category. Please try again.");
            showCategoryForm(request, response, category);
            return;
        }
        auditDAO.log(admin.getUserId(), "CREATE_CATEGORY", "CATEGORY", newId,
                "Created category " + category.getCategoryName());
        response.sendRedirect(request.getContextPath() + "/categories?msg=categoryCreated");
    }

    private void updateCategory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = AuthUtil.getLoggedUser(request);
        Category form = readCategoryForm(request);
        Category old = categoryDAO.getById(form.getCategoryId());
        if (old == null) {
            response.sendRedirect(request.getContextPath() + "/categories");
            return;
        }

        String error = validator.validateCategory(form);
        if (error != null) {
            request.setAttribute("error", error);
            showCategoryForm(request, response, form);
            return;
        }
        boolean same = old.getCategoryName().equals(form.getCategoryName())
                && Objects.equals(old.getParentCategoryId(), form.getParentCategoryId())
                && old.getDeskId() == form.getDeskId();
        if (same) {
            response.sendRedirect(request.getContextPath() + "/categories?msg=nochange");
            return;
        }
        if (!categoryDAO.update(form)) {
            request.setAttribute("error", "Could not save the category. Please try again.");
            showCategoryForm(request, response, form);
            return;
        }
        auditDAO.log(admin.getUserId(), "UPDATE_CATEGORY", "CATEGORY", form.getCategoryId(),
                "Updated category " + form.getCategoryName());
        response.sendRedirect(request.getContextPath() + "/categories?msg=categoryUpdated");
    }

    private void deleteCategory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = AuthUtil.getLoggedUser(request);
        int id = FormUtil.toInt(request.getParameter("id"));
        Category category = categoryDAO.getById(id);
        if (category == null) {
            response.sendRedirect(request.getContextPath() + "/categories");
            return;
        }

        // a category that is still in use cannot be deleted
        String error = validator.checkCategoryCanBeDeleted(category);
        if (error != null) {
            request.setAttribute("error", error);
            showList(request, response);
            return;
        }
        if (!categoryDAO.delete(id)) {
            request.setAttribute("error", "Could not delete the category. Please try again.");
            showList(request, response);
            return;
        }
        auditDAO.log(admin.getUserId(), "DELETE_CATEGORY", "CATEGORY", id,
                "Deleted category " + category.getCategoryName());
        response.sendRedirect(request.getContextPath() + "/categories?msg=categoryDeleted");
    }

    /** Reads id, name, parent (optional) and desk from the form. */
    private Category readCategoryForm(HttpServletRequest request) {
        Category c = new Category();
        c.setCategoryId(FormUtil.toInt(request.getParameter("id")));
        c.setCategoryName(FormUtil.text(request, "categoryName"));
        String parent = FormUtil.text(request, "parentId");
        if (!parent.isEmpty()) {
            c.setParentCategoryId(FormUtil.toInt(parent));
        }
        c.setDeskId(FormUtil.toInt(request.getParameter("deskId")));
        return c;
    }

    // =====================================================================
    //  SHOWING PAGES
    // =====================================================================
    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Category> all = categoryDAO.getAll();
        Map<Integer, String> indents = new HashMap<>();
        List<Category> tree = buildTree(all, CategoryValidator.buildChildrenMap(all), indents);

        List<Desk> desks = deskDAO.getAll();
        Map<Integer, String> deskNames = new HashMap<>();
        for (Desk d : desks) {
            deskNames.put(d.getDeskId(), d.getDeskName());
        }

        request.setAttribute("deskList", desks);
        request.setAttribute("categoryTree", tree);
        request.setAttribute("indents", indents);
        request.setAttribute("deskNames", deskNames);
        request.getRequestDispatcher("/WEB-INF/views/categories.jsp").forward(request, response);
    }

    private void showDeskForm(HttpServletRequest request, HttpServletResponse response, Desk desk)
            throws ServletException, IOException {
        request.setAttribute("desk", desk);
        request.getRequestDispatcher("/WEB-INF/views/desk-form.jsp").forward(request, response);
    }

    private void showCategoryForm(HttpServletRequest request, HttpServletResponse response,
            Category category) throws ServletException, IOException {
        List<Category> all = categoryDAO.getAll();
        Map<Integer, List<Category>> children = CategoryValidator.buildChildrenMap(all);
        Map<Integer, String> indents = new HashMap<>();
        List<Category> tree = buildTree(all, children, indents);

        // when editing, the category itself and everything below it cannot be chosen as parent
        Set<Integer> excluded = new HashSet<>();
        if (category.getCategoryId() > 0) {
            excluded.add(category.getCategoryId());
            CategoryValidator.collectDescendants(category.getCategoryId(), children, excluded);
        }

        request.setAttribute("category", category);
        request.setAttribute("treeList", tree);
        request.setAttribute("indents", indents);
        request.setAttribute("excluded", excluded);
        request.setAttribute("deskList", deskDAO.getAll());
        request.getRequestDispatcher("/WEB-INF/views/category-form.jsp").forward(request, response);
    }

    // =====================================================================
    //  TREE HELPERS
    // =====================================================================
    /** The categories in tree order (each parent followed by its sub-categories). Fills indents. */
    private List<Category> buildTree(List<Category> all, Map<Integer, List<Category>> children,
            Map<Integer, String> indents) {
        List<Category> ordered = new ArrayList<>();
        for (Category c : all) {
            if (c.getParentCategoryId() == null) {
                addWithChildren(c, 0, children, ordered, indents);
            }
        }
        return ordered;
    }

    private void addWithChildren(Category c, int depth, Map<Integer, List<Category>> children,
            List<Category> ordered, Map<Integer, String> indents) {
        ordered.add(c);
        indents.put(c.getCategoryId(), makeIndent(depth));
        List<Category> kids = children.get(c.getCategoryId());
        if (kids != null) {
            for (Category kid : kids) {
                addWithChildren(kid, depth + 1, children, ordered, indents);
            }
        }
    }

    /** Spaces and a corner sign in front of a sub-category's name (HTML text). */
    private String makeIndent(int depth) {
        if (depth == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < depth; i++) {
            sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
        }
        sb.append("&#9492; ");
        return sb.toString();
    }
}
