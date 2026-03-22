package com.college.servlets;

import com.college.dao.AlertDAO;
import com.college.dao.NoticeDAO;
import com.college.models.Alert;
import com.college.models.Notice;
import com.college.network.NotificationQueue;
import com.college.utils.ChatStatusClient;
import com.college.utils.RequestContext;
import com.college.utils.ServletResponseUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/status")
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
            ServletResponseUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "{\"error\":\"Unauthorized\"}");
            return;
        }

        String sessionId = session.getId();
        NotificationQueue.registerSession(sessionId);

        String type = req.getParameter("type");

        if ("alert".equals(type)) {
            NotificationQueue.Event event = NotificationQueue.pollAlert(sessionId);
            String alert = event == null ? fetchAlertFallback(session) : event.getPayload();
            String payload = alert == null ? "{\"alert\":null}" : "{\"alert\":\"" + ServletResponseUtil.escapeJson(alert) + "\"}";
            ServletResponseUtil.sendJson(resp, HttpServletResponse.SC_OK, payload);
            return;
        }

        if ("notice".equals(type)) {
            NotificationQueue.Event event = NotificationQueue.pollNotice(sessionId);
            String notice = event == null ? fetchNoticeFallback(session) : event.getPayload();
            String payload = notice == null ? "{\"notice\":null}" : "{\"notice\":\"" + ServletResponseUtil.escapeJson(notice) + "\"}";
            ServletResponseUtil.sendJson(resp, HttpServletResponse.SC_OK, payload);
            return;
        }

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
        
        StringBuilder sb = new StringBuilder();
        sb.append("{\"users\":[");
        int i = 0;
        for (String user : users) {
            if (i++ > 0) {
                sb.append(',');
            }
            sb.append('"').append(ServletResponseUtil.escapeJson(user)).append('"');
        }
        sb.append("]}");
        ServletResponseUtil.sendJson(resp, HttpServletResponse.SC_OK, sb.toString());
    }

    private String fetchAlertFallback(HttpSession session) {
        try {
            List<Alert> recent = alertDAO.getRecentAlerts(1);
            if (recent.isEmpty()) {
                return null;
            }

            Alert latest = recent.get(0);
            Integer lastSeen = (Integer) session.getAttribute(LAST_ALERT_DB_ID);
            if (lastSeen != null && latest.getAlertId() <= lastSeen) {
                return null;
            }

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
            if (recent.isEmpty()) {
                return null;
            }

            Notice latest = recent.get(0);
            Integer lastSeen = (Integer) session.getAttribute(LAST_NOTICE_DB_ID);
            if (lastSeen != null && latest.getNoticeId() <= lastSeen) {
                return null;
            }

            session.setAttribute(LAST_NOTICE_DB_ID, latest.getNoticeId());
            // Status notice payload remains notice-id-like to preserve existing polling contract.
            return String.valueOf(latest.getNoticeId());
        } catch (Exception e) {
            System.err.println("[" + RequestContext.getRequestId() + "] Notice DB fallback failed: " + e.getMessage());
            return null;
        }
    }
}
