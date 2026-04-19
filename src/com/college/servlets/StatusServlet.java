package com.college.servlets;

import com.college.dao.AlertDAO;
import com.college.dao.NoticeDAO;
import com.college.models.Alert;
import com.college.models.Notice;
import com.college.network.NotificationQueue;
import com.college.utils.ChatStatusClient;
import com.college.utils.JsonUtil;
import com.college.utils.RequestContext;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/api/status")
public class StatusServlet extends BaseServlet {
    private static final String LAST_ALERT_DB_ID = "lastAlertDbId";
    private static final String LAST_NOTICE_DB_ID = "lastNoticeDbId";
    private static final long CHAT_ONLINE_WINDOW_MS = 15_000L;
    private static final Map<String, Long> CHAT_PRESENCE = new ConcurrentHashMap<>();

    private final AlertDAO alertDAO = new AlertDAO();
    private final NoticeDAO noticeDAO = new NoticeDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        String sessionId = session.getId();
        NotificationQueue.registerSession(sessionId);

        String type = req.getParameter("type");

        if ("alert".equals(type)) {
            NotificationQueue.Event event = NotificationQueue.pollAlert(sessionId);
            String alert = event == null ? fetchAlertFallback(session) : event.getPayload();
            String payload = alert == null
                    ? "{\"alert\":null}"
                    : JsonUtil.object("alert", JsonUtil.str(alert));
            JsonUtil.sendSuccess(resp, payload);
            return;
        }

        if ("notice".equals(type)) {
            NotificationQueue.Event event = NotificationQueue.pollNotice(sessionId);
            String notice = event == null ? fetchNoticeFallback(session) : event.getPayload();
            String payload = notice == null
                    ? "{\"notice\":null}"
                    : JsonUtil.object("notice", JsonUtil.str(notice));
            JsonUtil.sendSuccess(resp, payload);
            return;
        }

        // Online users
        String username = (String) session.getAttribute("username");
        if (username != null && !username.trim().isEmpty()) {
            CHAT_PRESENCE.put(username, System.currentTimeMillis());
        }

        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : CHAT_PRESENCE.entrySet()) {
            if (now - entry.getValue() > CHAT_ONLINE_WINDOW_MS) {
                CHAT_PRESENCE.remove(entry.getKey());
            }
        }

        List<String> users = ChatStatusClient.fetchOnlineUsers();
        for (String u : CHAT_PRESENCE.keySet()) {
            if (!users.contains(u)) {
                users.add(u);
            }
        }

        List<String> userStrings = new ArrayList<>();
        for (String user : users) {
            userStrings.add(JsonUtil.str(user));
        }

        JsonUtil.sendSuccess(resp, JsonUtil.object("users", JsonUtil.array(userStrings)));
    }

    private String fetchAlertFallback(HttpSession session) {
        try {
            List<Alert> recent = alertDAO.getRecentAlerts(1);
            if (recent.isEmpty()) return null;
            Alert latest = recent.get(0);
            Integer lastSeen = (Integer) session.getAttribute(LAST_ALERT_DB_ID);
            if (lastSeen != null && latest.getAlertId() <= lastSeen) return null;
            session.setAttribute(LAST_ALERT_DB_ID, latest.getAlertId());
            return latest.getMessage();
        } catch (Exception e) {
            System.err.println("[" + RequestContext.getRequestId() + "] Alert DB fallback failed: " + e.getMessage());
            return null;
        }
    }

    private String fetchNoticeFallback(HttpSession session) {
        try {
            List<Notice> recent = noticeDAO.getRecentActiveNotices(1);
            if (recent.isEmpty()) return null;
            Notice latest = recent.get(0);
            Integer lastSeen = (Integer) session.getAttribute(LAST_NOTICE_DB_ID);
            if (lastSeen != null && latest.getNoticeId() <= lastSeen) return null;
            session.setAttribute(LAST_NOTICE_DB_ID, latest.getNoticeId());
            return String.valueOf(latest.getNoticeId());
        } catch (Exception e) {
            System.err.println("[" + RequestContext.getRequestId() + "] Notice DB fallback failed: " + e.getMessage());
            return null;
        }
    }
}
