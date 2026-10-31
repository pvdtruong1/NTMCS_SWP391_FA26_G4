package controller;

import dal.AuditLogDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import util.AuthUtil;

/** UC-08 View System Audit Log (SCR-5.8). Read-only. */
@WebServlet(name = "AuditLogServlet", urlPatterns = {"/audit-log"})
public class AuditLogServlet extends HttpServlet {

    private final AuditLogDAO auditDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.check(request, response, "AUDIT_VIEW")) {
            return;
        }
        String keyword = request.getParameter("keyword");
        if (keyword == null) {
            keyword = "";
        }
        keyword = keyword.trim();

        request.setAttribute("keyword", keyword);
        request.setAttribute("logList", auditDAO.getRecent(keyword));
        request.getRequestDispatcher("/WEB-INF/views/audit-log.jsp").forward(request, response);
    }
}
