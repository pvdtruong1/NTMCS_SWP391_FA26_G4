package controller;

import dal.AuditLogDAO;
import dal.CriteriaDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.CriteriaConfig;
import model.User;
import util.ApiUtil;
import util.CriteriaValidator;
import util.FormUtil;
import util.Json;

/**
 * JSON version of CriteriaServlet (UC-03, with UC-10). The rules are the same as CriteriaServlet:
 * the whole matrix is checked together (weights add up to exactly 100%) and saved together.
 *   POST /api/criteria
 *     criteria_id   (repeat per row, 0 = a new criterion)
 *     criteria_name (repeat per row)
 *     weight        (repeat per row)
 *     remove        (repeat per criterion id to remove)
 */
@WebServlet(name = "ApiCriteriaServlet", urlPatterns = {"/api/criteria"})
public class ApiCriteriaServlet extends HttpServlet {

    private static final String PERMISSION = "QUOTA_MANAGE";

    private final CriteriaDAO criteriaDAO = new CriteriaDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User admin = ApiUtil.requirePermission(request, response, PERMISSION);
        if (admin == null) {
            return;
        }

        String[] ids = request.getParameterValues("criteria_id");
        String[] names = request.getParameterValues("criteria_name");
        String[] weightTexts = request.getParameterValues("weight");
        if (ids == null || names == null || weightTexts == null
                || ids.length != names.length || ids.length != weightTexts.length) {
            ApiUtil.error(response, 400, "The criteria form is incomplete.");
            return;
        }
        Set<Integer> removeSet = new HashSet<>();
        String[] removeIds = request.getParameterValues("remove");
        if (removeIds != null) {
            for (String r : removeIds) {
                removeSet.add(FormUtil.toInt(r));
            }
        }

        List<CriteriaConfig> keptExisting = new ArrayList<>();
        List<CriteriaConfig> keptNew = new ArrayList<>();
        List<Integer> toDelete = new ArrayList<>();
        List<String> keptNames = new ArrayList<>();
        List<BigDecimal> keptWeights = new ArrayList<>();
        String error = null;

        // 1. read every row
        for (int i = 0; i < ids.length; i++) {
            int id = FormUtil.toInt(ids[i]);
            String name = names[i].trim();
            String weightText = weightTexts[i].trim();
            boolean removed = id > 0 && removeSet.contains(id);

            if (removed) {
                toDelete.add(id);
                continue;
            }
            if (id == 0 && name.isEmpty() && weightText.isEmpty()) {
                continue;   // a blank row for a new criterion that was left empty
            }
            if (name.isEmpty()) {
                if (error == null) {
                    error = "Every criterion needs a name.";
                }
                continue;
            }
            if (name.length() > 150) {
                if (error == null) {
                    error = "A criterion name is too long (maximum 150 characters).";
                }
                continue;
            }
            BigDecimal weight = CriteriaValidator.parseWeight(weightText);
            if (weight == null) {
                if (error == null) {
                    error = "The weight of \"" + name + "\" must be a number from 0 to 100 (at most 2 decimals).";
                }
                continue;
            }
            CriteriaConfig c = new CriteriaConfig(id, name, weight);
            if (id > 0) {
                keptExisting.add(c);
            } else {
                keptNew.add(c);
            }
            keptNames.add(name);
            keptWeights.add(weight);
        }

        // 2. UC-10: the weights of the criteria that stay must add up to exactly 100%
        if (error == null) {
            error = CriteriaValidator.validateWeights(keptNames, keptWeights);
        }
        // 3. a criterion that was already used to score a topic cannot be removed
        if (error == null) {
            for (int id : toDelete) {
                if (criteriaDAO.isUsed(id)) {
                    error = "A criterion that was already used in evaluations cannot be removed. "
                            + "Set its weight to 0 instead.";
                    break;
                }
            }
        }
        if (error != null) {
            ApiUtil.error(response, 400, error);
            return;
        }

        // 4. count what really changed (nothing changed = nothing to save or log)
        Map<Integer, CriteriaConfig> current = new HashMap<>();
        for (CriteriaConfig c : criteriaDAO.getAll()) {
            current.put(c.getCriteriaId(), c);
        }
        int changed = 0;
        for (CriteriaConfig c : keptExisting) {
            CriteriaConfig old = current.get(c.getCriteriaId());
            if (old != null && (!old.getCriteriaName().equals(c.getCriteriaName())
                    || old.getWeightPercent().compareTo(c.getWeightPercent()) != 0)) {
                changed++;
            }
        }
        if (changed == 0 && keptNew.isEmpty() && toDelete.isEmpty()) {
            ApiUtil.send(response, 200, Json.obj("ok", true, "changed", false));
            return;
        }

        // 5. save everything in one transaction
        if (!criteriaDAO.saveAll(keptExisting, keptNew, toDelete)) {
            ApiUtil.error(response, 500, "Could not save the matrix. Nothing was changed.");
            return;
        }

        auditDAO.log(admin.getUserId(), "UPDATE_CRITERIA", "CRITERIA_CONFIG", null,
                "Saved criteria matrix: " + keptNames.size() + " criteria, total 100%. Added "
                + keptNew.size() + ", changed " + changed + ", removed " + toDelete.size());
        ApiUtil.send(response, 200, Json.obj("ok", true, "changed", true));
    }
}
