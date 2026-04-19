package com.college.servlets;

import com.college.dao.MessageDAO;
import com.college.dao.UserDAO;
import com.college.models.Message;
import com.college.models.User;
import com.college.service.ChatService;
import com.college.utils.InputSanitizer;
import com.college.utils.JsonUtil;
import com.college.utils.ValidationUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/chat/*")
public class ChatServlet extends BaseServlet {
    private final ChatService chatService = new ChatService();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if ("/history".equals(pathInfo)) {
            String with = InputSanitizer.normalizeText(req.getParameter("with"));
            if (ValidationUtil.isBlank(with)) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing 'with' username");
                return;
            }
            try {
                int currentUserId = currentUserId(req);
                List<Message> history = chatService.getDirectHistory(currentUserId, with);
                List<String> items = new ArrayList<>();
                for (int i = history.size() - 1; i >= 0; i--) {
                    Message msg = history.get(i);
                    items.add(JsonUtil.object(
                            "messageId", JsonUtil.num(msg.getMessageId()),
                            "fromSelf", JsonUtil.bool(msg.getSenderId() == currentUserId),
                            "senderId", JsonUtil.num(msg.getSenderId()),
                            "content", JsonUtil.str(msg.getContent()),
                            "sentAt", JsonUtil.date(msg.getSentAt())
                    ));
                }
                JsonUtil.sendSuccess(resp, JsonUtil.object(
                        "status", JsonUtil.str("ok"),
                        "messages", JsonUtil.array(items)
                ));
            } catch (Exception e) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Could not load chat history.");
            }
            return;
        }

        if ("/contacts".equals(pathInfo)) {
            try {
                List<User> users = userDAO.findAll();
                int currentId = currentUserId(req);
                List<String> items = new ArrayList<>();
                for (User u : users) {
                    if (u.getUserId() != currentId) {
                        items.add(JsonUtil.object(
                                "userId", JsonUtil.num(u.getUserId()),
                                "username", JsonUtil.str(u.getUsername()),
                                "fullName", JsonUtil.str(u.getFullName()),
                                "role", JsonUtil.str(u.getRole())
                        ));
                    }
                }
                JsonUtil.sendSuccess(resp, JsonUtil.array(items));
            } catch (Exception e) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Could not load contacts.");
            }
            return;
        }

        JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown chat endpoint");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if ("/send".equals(pathInfo)) {
            String body = JsonUtil.readBody(req);
            String to = InputSanitizer.normalizeText(JsonUtil.extractString(body, "to"));
            String message = InputSanitizer.normalizeText(JsonUtil.extractString(body, "message"));

            if (ValidationUtil.isBlank(to) || ValidationUtil.isBlank(message)) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Recipient and message are required.");
                return;
            }

            String from = (String) req.getSession(false).getAttribute("username");

            try {
                chatService.sendDirectMessage(from, to, message, currentUserId(req));
                JsonUtil.sendSuccess(resp, "{\"status\":\"sent\"}");
            } catch (IllegalArgumentException e) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Recipient not found");
            } catch (Exception e) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Chat service offline");
            }
            return;
        }

        JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown chat endpoint");
    }
}
