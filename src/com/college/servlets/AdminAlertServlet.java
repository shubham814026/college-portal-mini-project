package com.college.servlets;

import com.college.service.AlertService;
import com.college.utils.AppConstants;
import com.college.utils.InputSanitizer;
import com.college.utils.ServletResponseUtil;
import com.college.utils.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/alert", "/admin/alerts/new"})
public class AdminAlertServlet extends BaseServlet {
    private final AlertService alertService = new AlertService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        if (!AppConstants.ROLE_ADMIN.equals(currentRole(req))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        req.getRequestDispatcher("/admin/send_alert.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        if (!AppConstants.ROLE_ADMIN.equals(currentRole(req))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        String alertMessage = InputSanitizer.normalizeText(req.getParameter("alertMessage"));
        if (ValidationUtil.isBlank(alertMessage)) {
            resp.sendRedirect(req.getContextPath() + "/admin/alerts/new?error=validation");
            return;
        }

        try {
            alertService.sendAlert(alertMessage, currentUserId(req));

            resp.sendRedirect(req.getContextPath() + "/admin/alerts/new?success=sent");
        } catch (Exception e) {
            ServletResponseUtil.forwardError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to send alert.");
        }
    }
}
