package com.college.servlets;

import com.college.service.ChatService;
import com.college.models.Message;
import com.college.utils.InputSanitizer;
import com.college.utils.ServletResponseUtil;
import com.college.utils.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/chat")
public class ChatServlet extends BaseServlet {
    private final ChatService chatService = new ChatService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String action = req.getParameter("action");
        if ("history".equals(action)) {
            String with = InputSanitizer.normalizeText(req.getParameter("with"));
            if (ValidationUtil.isBlank(with)) {
                ServletResponseUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "{\"status\":\"invalid-request\",\"message\":\"Missing 'with' username.\"}");
                return;
            }

            try {
                int currentUserId = currentUserId(req);
                java.util.List<Message> history = chatService.getDirectHistory(currentUserId, with);
                StringBuilder sb = new StringBuilder();
                sb.append("{\"status\":\"ok\",\"messages\":[");

                for (int i = history.size() - 1; i >= 0; i--) {
                    Message msg = history.get(i);
                    if (i < history.size() - 1) {
                        sb.append(',');
                    }
                    sb.append("{\"fromSelf\":")
                            .append(msg.getSenderId() == currentUserId ? "true" : "false")
                            .append(",\"content\":\"")
                            .append(ServletResponseUtil.escapeJson(msg.getContent()))
                            .append("\"}");
                }
                sb.append("]}");
                ServletResponseUtil.sendJson(resp, HttpServletResponse.SC_OK, sb.toString());
            } catch (Exception e) {
                ServletResponseUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "{\"status\":\"history-error\"}");
            }
            return;
        }

        req.getRequestDispatcher("/student/chat.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String to = InputSanitizer.normalizeText(req.getParameter("to"));
        String message = InputSanitizer.normalizeText(req.getParameter("message"));

        if (ValidationUtil.isBlank(to) || ValidationUtil.isBlank(message)) {
            ServletResponseUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "{\"status\":\"invalid-request\",\"message\":\"Recipient and message are required.\"}");
            return;
        }

        String from = (String) req.getSession(false).getAttribute("username");

        try {
            chatService.sendDirectMessage(from, to, message, currentUserId(req));

            ServletResponseUtil.sendJson(resp, HttpServletResponse.SC_OK, "{\"status\":\"sent\"}");
        } catch (IllegalArgumentException e) {
            ServletResponseUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "{\"status\":\"invalid-recipient\"}");
        } catch (Exception e) {
            System.err.println("Chat send failed: " + e.getMessage());
            ServletResponseUtil.sendJson(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "{\"status\":\"chat-offline\"}");
        }
    }
}
