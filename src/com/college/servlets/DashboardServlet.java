package com.college.servlets;

import com.college.service.DashboardService;
import com.college.service.DashboardService.DashboardData;
import com.college.utils.AppConstants;
import com.college.utils.ServletResponseUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet {
    private final DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            DashboardData data = dashboardService.loadData();
            req.setAttribute("recentNotices", data.getRecentNotices());
            req.setAttribute("recentFiles", data.getRecentFiles());
            req.setAttribute("recentLogs", data.getRecentLogs());
        } catch (Exception e) {
            ServletResponseUtil.forwardError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load dashboard data.");
            return;
        }

        String role = currentRole(req);
        if (AppConstants.ROLE_ADMIN.equals(role)) {
            req.getRequestDispatcher("/admin/dashboard.jsp").forward(req, resp);
            return;
        }

        req.getRequestDispatcher("/student/dashboard.jsp").forward(req, resp);
    }
}
