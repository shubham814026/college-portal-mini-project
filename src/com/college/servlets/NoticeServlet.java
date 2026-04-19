package com.college.servlets;

import com.college.models.Notice;
import com.college.service.NoticeService;
import com.college.utils.AppConstants;
import com.college.utils.InputSanitizer;
import com.college.utils.JsonUtil;
import com.college.utils.ValidationUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/notices/*")
public class NoticeServlet extends BaseServlet {
    private final NoticeService noticeService = new NoticeService();
    private final com.college.dao.NoticeDAO noticeDAO = new com.college.dao.NoticeDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<Notice> notices = noticeService.getAllActiveNotices();
            List<String> items = new ArrayList<>();
            for (Notice n : notices) {
                items.add(JsonUtil.object(
                        "noticeId", JsonUtil.num(n.getNoticeId()),
                        "title", JsonUtil.str(n.getTitle()),
                        "body", JsonUtil.str(n.getBody()),
                        "postedBy", JsonUtil.num(n.getPostedBy()),
                        "postedByName", JsonUtil.str(n.getPostedByName()),
                        "createdAt", JsonUtil.date(n.getCreatedAt())
                ));
            }
            JsonUtil.sendSuccess(resp, JsonUtil.array(items));
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load notices.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = currentRole(req);
        boolean canPost = AppConstants.ROLE_ADMIN.equals(role) || AppConstants.ROLE_FACULTY.equals(role);
        if (!canPost) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        String body = JsonUtil.readBody(req);
        String title = InputSanitizer.normalizeText(JsonUtil.extractString(body, "title"));
        String noticeBody = InputSanitizer.normalizeText(JsonUtil.extractString(body, "body"));

        if (ValidationUtil.isBlank(title) || ValidationUtil.isBlank(noticeBody)) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Title and body are required");
            return;
        }

        try {
            noticeService.postNotice(title, noticeBody, currentUserId(req));
            JsonUtil.sendCreated(resp, "{\"status\":\"ok\"}");
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to post notice.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = currentRole(req);
        if (!AppConstants.ROLE_ADMIN.equals(role) && !AppConstants.ROLE_FACULTY.equals(role)) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing notice ID");
            return;
        }

        int noticeId = Integer.parseInt(pathInfo.substring(1));
        String body = JsonUtil.readBody(req);
        String title = InputSanitizer.normalizeText(JsonUtil.extractString(body, "title"));
        String noticeBody = InputSanitizer.normalizeText(JsonUtil.extractString(body, "body"));

        if (ValidationUtil.isBlank(title) || ValidationUtil.isBlank(noticeBody)) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Title and body are required");
            return;
        }

        try {
            noticeDAO.updateNotice(noticeId, title, noticeBody);
            JsonUtil.sendSuccess(resp, "{\"status\":\"ok\"}");
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Update failed");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = currentRole(req);
        if (!AppConstants.ROLE_ADMIN.equals(role) && !AppConstants.ROLE_FACULTY.equals(role)) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing notice ID");
            return;
        }

        int noticeId = Integer.parseInt(pathInfo.substring(1));
        try {
            noticeDAO.deleteNotice(noticeId);
            JsonUtil.sendSuccess(resp, "{\"status\":\"deleted\"}");
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Delete failed");
        }
    }
}
