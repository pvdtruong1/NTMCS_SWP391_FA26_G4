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
import util.AuthUtil;
import util.CriteriaValidator;
import util.FormUtil;

/**
 * UC-03 Configure Evaluation Criteria Matrix (SCR-5.3), with UC-10 (weights = 100%).
 * The whole matrix is one form: edit the names and weights, tick "Remove", or fill a
 * blank row to add a criterion. Everything is checked together and saved together.
 *   GET  /criteria -> the matrix
 *   POST /criteria -> check and save the matrix
 */
@WebServlet(name = "CriteriaServlet", urlPatterns = {"/criteria"})
public class CriteriaServlet extends HttpServlet {

    private static final String PERMISSION = "QUOTA_MANAGE";
    private static final int BLANK_ROWS = 2;   // empty rows shown for adding new criteria

    private final CriteriaDAO criteriaDAO = new CriteriaDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.check(request, response, PERMISSION)) {
            return;
        }
        // each row is { id, name, weight text, removed flag }
        List<String[]> rows = new ArrayList<>();
        List<BigDecimal> weights = new ArrayList<>();
        for (CriteriaConfig c : criteriaDAO.getAll()) {
            rows.add(new String[]{String.valueOf(c.getCriteriaId()), c.getCriteriaName(),
                c.getWeightPercent().toPlainString(), ""});
            weights.add(c.getWeightPercent());
        }
        for (int i = 0; i < BLANK_ROWS; i++) {
            rows.add(new String[]{"0", "", "", ""});
        }
        request.setAttribute("rows", rows);
        request.setAttribute("total", CriteriaValidator.total(weights));
        request.getRequestDispatcher("/WEB-INF/views/criteria.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!AuthUtil.check(request, response, PERMISSION)) {
            return;
        }
        User admin = AuthUtil.getLoggedUser(request);

        String[] ids = request.getParameterValues("criteriaId");
        String[] names = request.getParameterValues("criteriaName");
        String[] weightTexts = request.getParameterValues("weight");
        if (ids == null || names == null || weightTexts == null
                || ids.length != names.length || ids.length != weightTexts.length) {
            response.sendRedirect(request.getContextPath() + "/criteria");
            return;
        }
        Set<Integer> removeSet = new HashSet<>();
        String[] removeIds = request.getParameterValues("remove");
        if (removeIds != null) {
            for (String r : removeIds) {
                removeSet.add(FormUtil.toInt(r));
            }
        }

        List<String[]> rows = new ArrayList<>();             // what the user typed (to show again on error)
        List<CriteriaConfig> keptExisting = new ArrayList<>();
        List<CriteriaConfig> keptNew = new ArrayList<>();
        List<Integer> toDelete = new ArrayList<>();
        List<String> keptNames = new ArrayList<>();
        List<BigDecimal> keptWeights = new ArrayList<>();
        String error = null;

        // 1. read every row of the form
        for (int i = 0; i < ids.length; i++) {
            int id = FormUtil.toInt(ids[i]);
            String name = names[i].trim();
            String weightText = weightTexts[i].trim();
            boolean removed = id > 0 && removeSet.contains(id);
            rows.add(new String[]{String.valueOf(id), name, weightText, removed ? "1" : ""});

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
            request.setAttribute("error", error);
            request.setAttribute("rows", rows);
            request.getRequestDispatcher("/WEB-INF/views/criteria.jsp").forward(request, response);
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
            response.sendRedirect(request.getContextPath() + "/criteria?msg=nochange");
            return;
        }

        // 5. save everything in one transaction
        if (!criteriaDAO.saveAll(keptExisting, keptNew, toDelete)) {
            request.setAttribute("error", "Could not save the matrix. Nothing was changed.");
            request.setAttribute("rows", rows);
            request.getRequestDispatcher("/WEB-INF/views/criteria.jsp").forward(request, response);
            return;
        }

        auditDAO.log(admin.getUserId(), "UPDATE_CRITERIA", "CRITERIA_CONFIG", null,
                "Saved criteria matrix: " + keptNames.size() + " criteria, total 100%. Added "
                + keptNew.size() + ", changed " + changed + ", removed " + toDelete.size());
        response.sendRedirect(request.getContextPath() + "/criteria?msg=saved");
    }
}
