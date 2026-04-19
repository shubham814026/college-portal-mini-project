package com.college.servlets;

import com.college.service.AlertService;
import com.college.utils.AppConstants;
import com.college.utils.InputSanitizer;
import com.college.utils.JsonUtil;
import com.college.utils.ValidationUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/alerts")
public class AdminAlertServlet extends BaseServlet {
    private final AlertService alertService = new AlertService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!AppConstants.ROLE_ADMIN.equals(currentRole(req))) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        String body = JsonUtil.readBody(req);
        String alertMessage = InputSanitizer.normalizeText(JsonUtil.extractString(body, "message"));
        if (ValidationUtil.isBlank(alertMessage)) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Alert message is required");
            return;
        }

        try {
            alertService.sendAlert(alertMessage, currentUserId(req));
            JsonUtil.sendCreated(resp, "{\"status\":\"ok\"}");
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to send alert.");
        }
    }
}
