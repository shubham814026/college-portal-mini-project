package com.college.servlets;

import com.college.dao.LogDAO;
import com.college.utils.AppConstants;
import com.college.utils.ServletResponseUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/logs")
public class LogServlet extends BaseServlet {
    private final LogDAO logDAO = new LogDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        if (!AppConstants.ROLE_ADMIN.equals(currentRole(req))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            req.setAttribute("logs", logDAO.getAllLogs());
            req.getRequestDispatcher("/admin/view_logs.jsp").forward(req, resp);
        } catch (Exception e) {
            ServletResponseUtil.forwardError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load logs.");
        }
    }
}
