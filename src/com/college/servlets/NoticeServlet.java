package com.college.servlets;

import com.college.service.NoticeService;
import com.college.utils.AppConstants;
import com.college.utils.InputSanitizer;
import com.college.utils.ServletResponseUtil;
import com.college.utils.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/notices", "/admin/notices/new", "/faculty/notices/new"})
public class NoticeServlet extends BaseServlet {
    private final NoticeService noticeService = new NoticeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/admin/notices/new".equals(req.getServletPath()) || "/faculty/notices/new".equals(req.getServletPath())) {
            String role = currentRole(req);
            boolean canPost = AppConstants.ROLE_ADMIN.equals(role) || AppConstants.ROLE_FACULTY.equals(role);
            if (!canPost) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }
            req.getRequestDispatcher("/admin/post_notice.jsp").forward(req, resp);
            return;
        }

        try {
            req.setAttribute("notices", noticeService.getAllActiveNotices());
            req.getRequestDispatcher("/student/notices.jsp").forward(req, resp);
        } catch (Exception e) {
            ServletResponseUtil.forwardError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load notices.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String role = currentRole(req);
        boolean canPost = AppConstants.ROLE_ADMIN.equals(role) || AppConstants.ROLE_FACULTY.equals(role);
        if (!canPost) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        String title = InputSanitizer.normalizeText(req.getParameter("title"));
        String body = InputSanitizer.normalizeText(req.getParameter("body"));
        if (ValidationUtil.isBlank(title) || ValidationUtil.isBlank(body)) {
            resp.sendRedirect(req.getContextPath() + resolveComposerPath(role) + "?error=validation");
            return;
        }

        try {
            noticeService.postNotice(title, body, currentUserId(req));
            resp.sendRedirect(req.getContextPath() + resolveComposerPath(role) + "?success=posted");
        } catch (Exception e) {
            ServletResponseUtil.forwardError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to post notice.");
        }
    }

    private String resolveComposerPath(String role) {
        if (AppConstants.ROLE_FACULTY.equals(role)) {
            return "/faculty/notices/new";
        }
        return "/admin/notices/new";
    }
}
