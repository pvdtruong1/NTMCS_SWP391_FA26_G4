package util;

import dal.CategoryDAO;
import dal.DeskDAO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Category;
import model.Desk;

/**
 * UC-04 Manage Category Tree: the rules a desk or a category must pass before it is
 * saved or deleted. Shared by CategoryServlet (JSP) and ApiCategoryServlet (React),
 * so both sides always apply the same rules.
 */
public class CategoryValidator {

    private final DeskDAO deskDAO = new DeskDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    // =====================================================================
    //  DESKS
    // =====================================================================
    /** Returns an error message, or null if the desk name is fine. */
    public String validateDeskName(String name, int excludeId) {
        if (name.isEmpty()) {
            return "Desk name is required.";
        }
        if (name.length() > 100) {
            return "Desk name is too long (maximum 100 characters).";
        }
        if (deskDAO.nameExists(name, excludeId)) {
            return "A desk with this name already exists.";
        }
        return null;
    }

    /** Returns an error message if the desk is still in use, or null if it can be deleted. */
    public String checkDeskCanBeDeleted(Desk desk) {
        int categories = deskDAO.countCategories(desk.getDeskId());
        int users = deskDAO.countUsers(desk.getDeskId());
        int quotas = deskDAO.countQuotas(desk.getDeskId());
        if (categories > 0 || users > 0 || quotas > 0) {
            return "Desk \"" + desk.getDeskName() + "\" cannot be deleted. It still has "
                    + categories + " categories, " + users + " users and " + quotas
                    + " quotas. Move or remove them first.";
        }
        return null;
    }

    // =====================================================================
    //  CATEGORIES
    // =====================================================================
    /** Returns an error message, or null if the category can be saved. */
    public String validateCategory(Category c) {
        if (c.getCategoryName().isEmpty()) {
            return "Category name is required.";
        }
        if (c.getCategoryName().length() > 100) {
            return "Category name is too long (maximum 100 characters).";
        }
        if (deskDAO.getById(c.getDeskId()) == null) {
            return "Please choose a desk.";
        }
        Integer parentId = c.getParentCategoryId();
        if (parentId != null) {
            if (categoryDAO.getById(parentId) == null) {
                return "The parent category does not exist.";
            }
            if (c.getCategoryId() > 0) {
                // editing: the parent must not be the category itself or anything below it
                Set<Integer> forbidden = new HashSet<>();
                forbidden.add(c.getCategoryId());
                collectDescendants(c.getCategoryId(), buildChildrenMap(categoryDAO.getAll()), forbidden);
                if (forbidden.contains(parentId)) {
                    return "A category cannot be placed under itself or under one of its own sub-categories.";
                }
            }
        }
        if (categoryDAO.siblingNameExists(c.getCategoryName(), parentId, c.getCategoryId())) {
            return "A category with this name already exists under the same parent.";
        }
        return null;
    }

    /** Returns an error message if the category is still in use, or null if it can be deleted. */
    public String checkCategoryCanBeDeleted(Category category) {
        List<Category> subCategories = buildChildrenMap(categoryDAO.getAll()).get(category.getCategoryId());
        if (subCategories != null && !subCategories.isEmpty()) {
            return "Category \"" + category.getCategoryName()
                    + "\" cannot be deleted. It still has " + subCategories.size()
                    + " sub-categories. Move or delete them first.";
        }
        int topics = categoryDAO.countTopics(category.getCategoryId());
        if (topics > 0) {
            return "Category \"" + category.getCategoryName()
                    + "\" cannot be deleted. " + topics + " topics use it.";
        }
        return null;
    }

    // =====================================================================
    //  TREE HELPERS
    // =====================================================================
    /** parent id -> list of its direct sub-categories. */
    public static Map<Integer, List<Category>> buildChildrenMap(List<Category> all) {
        Map<Integer, List<Category>> map = new HashMap<>();
        for (Category c : all) {
            Integer parentId = c.getParentCategoryId();
            if (parentId != null) {
                List<Category> list = map.get(parentId);
                if (list == null) {
                    list = new ArrayList<>();
                    map.put(parentId, list);
                }
                list.add(c);
            }
        }
        return map;
    }

    /** Adds the ids of everything below a category (children, grandchildren, ...) to result. */
    public static void collectDescendants(int id, Map<Integer, List<Category>> children, Set<Integer> result) {
        List<Category> kids = children.get(id);
        if (kids == null) {
            return;
        }
        for (Category kid : kids) {
            result.add(kid.getCategoryId());
            collectDescendants(kid.getCategoryId(), children, result);
        }
    }
}
