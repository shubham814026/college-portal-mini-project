package com.college.servlets;

import com.college.dao.LogDAO;
import com.college.models.LogEntry;
import com.college.utils.AppConstants;
import com.college.utils.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/logs")
public class LogServlet extends BaseServlet {
    private final LogDAO logDAO = new LogDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!AppConstants.ROLE_ADMIN.equals(currentRole(req))) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        try {
            List<LogEntry> logs = logDAO.getAllLogs();
            List<String> items = new ArrayList<>();
            for (LogEntry l : logs) {
                items.add(JsonUtil.object(
                        "logId", JsonUtil.num(l.getLogId()),
                        "userId", JsonUtil.num(l.getUserId()),
                        "username", JsonUtil.str(l.getUsername()),
                        "action", JsonUtil.str(l.getAction()),
                        "ipAddress", JsonUtil.str(l.getIpAddress()),
                        "loggedAt", JsonUtil.date(l.getLoggedAt())
                ));
            }
            JsonUtil.sendSuccess(resp, JsonUtil.array(items));
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not load logs.");
        }
    }
}
