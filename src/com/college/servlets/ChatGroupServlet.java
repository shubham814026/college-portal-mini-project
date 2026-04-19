package com.college.servlets;

import com.college.dao.ChatGroupDAO;
import com.college.dao.UserDAO;
import com.college.models.User;
import com.college.utils.InputSanitizer;
import com.college.utils.JsonUtil;
import com.college.utils.ValidationUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/api/chat-groups/*")
public class ChatGroupServlet extends BaseServlet {
    private final ChatGroupDAO chatGroupDAO = new ChatGroupDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        int userId = currentUserId(req);

        // List my groups
        if (pathInfo == null || "/".equals(pathInfo)) {
            try {
                List<Map<String, Object>> groups = chatGroupDAO.getGroupsForUser(userId);
                List<String> items = new ArrayList<>();
                for (Map<String, Object> g : groups) {
                    items.add(JsonUtil.object(
                            "groupId", JsonUtil.num((Integer) g.get("groupId")),
                            "groupName", JsonUtil.str((String) g.get("groupName")),
                            "description", JsonUtil.str((String) g.get("description")),
                            "creatorName", JsonUtil.str((String) g.get("creatorName")),
                            "myRole", JsonUtil.str((String) g.get("myRole")),
                            "createdAt", JsonUtil.date((LocalDateTime) g.get("createdAt"))
                    ));
                }
                JsonUtil.sendSuccess(resp, JsonUtil.array(items));
            } catch (Exception e) {
                JsonUtil.sendError(resp, 500, "Could not load groups.");
            }
            return;
        }

        // Get group messages: /api/chat-groups/{id}/messages
        if (pathInfo.matches("/\\d+/messages")) {
            int groupId = Integer.parseInt(pathInfo.split("/")[1]);
            try {
                if (!chatGroupDAO.isMember(groupId, userId)) {
                    JsonUtil.sendError(resp, 403, "Not a member of this group");
                    return;
                }
                List<Map<String, Object>> messages = chatGroupDAO.getGroupMessages(groupId, 100);
                List<String> items = new ArrayList<>();
                for (int i = messages.size() - 1; i >= 0; i--) {
                    Map<String, Object> msg = messages.get(i);
                    items.add(JsonUtil.object(
                            "messageId", JsonUtil.num((Integer) msg.get("messageId")),
                            "senderId", JsonUtil.num((Integer) msg.get("senderId")),
                            "senderUsername", JsonUtil.str((String) msg.get("senderUsername")),
                            "senderName", JsonUtil.str((String) msg.get("senderName")),
                            "content", JsonUtil.str((String) msg.get("content")),
                            "fromSelf", JsonUtil.bool((Integer) msg.get("senderId") == userId),
                            "sentAt", JsonUtil.date((LocalDateTime) msg.get("sentAt"))
                    ));
                }
                JsonUtil.sendSuccess(resp, JsonUtil.array(items));
            } catch (Exception e) {
                JsonUtil.sendError(resp, 500, "Could not load messages.");
            }
            return;
        }

        // Get group members: /api/chat-groups/{id}/members
        if (pathInfo.matches("/\\d+/members")) {
            int groupId = Integer.parseInt(pathInfo.split("/")[1]);
            try {
                if (!chatGroupDAO.isMember(groupId, userId)) {
                    JsonUtil.sendError(resp, 403, "Not a member");
                    return;
                }
                List<Map<String, Object>> members = chatGroupDAO.getMembers(groupId);
                List<String> items = new ArrayList<>();
                for (Map<String, Object> m : members) {
                    items.add(JsonUtil.object(
                            "userId", JsonUtil.num((Integer) m.get("userId")),
                            "username", JsonUtil.str((String) m.get("username")),
                            "fullName", JsonUtil.str((String) m.get("fullName")),
                            "role", JsonUtil.str((String) m.get("role"))
                    ));
                }
                JsonUtil.sendSuccess(resp, JsonUtil.array(items));
            } catch (Exception e) {
                JsonUtil.sendError(resp, 500, "Could not load members.");
            }
            return;
        }

        JsonUtil.sendError(resp, 404, "Unknown endpoint");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        int userId = currentUserId(req);

        // Create group
        if (pathInfo == null || "/".equals(pathInfo)) {
            String body = JsonUtil.readBody(req);
            String name = InputSanitizer.normalizeText(JsonUtil.extractString(body, "name"));
            String desc = JsonUtil.extractString(body, "description");

            if (ValidationUtil.isBlank(name)) {
                JsonUtil.sendError(resp, 400, "Group name is required");
                return;
            }
            try {
                int groupId = chatGroupDAO.createGroup(name, desc, userId);
                chatGroupDAO.addMember(groupId, userId, "OWNER");
                JsonUtil.sendCreated(resp, JsonUtil.object(
                        "status", JsonUtil.str("ok"),
                        "groupId", JsonUtil.num(groupId)
                ));
            } catch (Exception e) {
                JsonUtil.sendError(resp, 500, "Failed to create group.");
            }
            return;
        }

        // Send message: /api/chat-groups/{id}/send
        if (pathInfo.matches("/\\d+/send")) {
            int groupId = Integer.parseInt(pathInfo.split("/")[1]);
            String body = JsonUtil.readBody(req);
            String message = InputSanitizer.normalizeText(JsonUtil.extractString(body, "message"));

            if (ValidationUtil.isBlank(message)) {
                JsonUtil.sendError(resp, 400, "Message is required");
                return;
            }
            try {
                if (!chatGroupDAO.isMember(groupId, userId)) {
                    JsonUtil.sendError(resp, 403, "Not a member");
                    return;
                }
                chatGroupDAO.saveGroupMessage(groupId, userId, message);
                JsonUtil.sendSuccess(resp, "{\"status\":\"sent\"}");
            } catch (Exception e) {
                JsonUtil.sendError(resp, 500, "Failed to send message.");
            }
            return;
        }

        // Invite user: /api/chat-groups/{id}/invite
        if (pathInfo.matches("/\\d+/invite")) {
            int groupId = Integer.parseInt(pathInfo.split("/")[1]);
            String body = JsonUtil.readBody(req);
            String username = JsonUtil.extractString(body, "username");

            if (ValidationUtil.isBlank(username)) {
                JsonUtil.sendError(resp, 400, "Username is required");
                return;
            }
            try {
                if (!chatGroupDAO.isMember(groupId, userId)) {
                    JsonUtil.sendError(resp, 403, "Not a member");
                    return;
                }
                User invitee = userDAO.findByUsername(username);
                if (invitee == null) {
                    JsonUtil.sendError(resp, 404, "User not found");
                    return;
                }
                chatGroupDAO.addMember(groupId, invitee.getUserId(), "MEMBER");
                JsonUtil.sendSuccess(resp, "{\"status\":\"ok\"}");
            } catch (Exception e) {
                JsonUtil.sendError(resp, 500, "Failed to invite user.");
            }
            return;
        }

        JsonUtil.sendError(resp, 404, "Unknown endpoint");
    }
}
