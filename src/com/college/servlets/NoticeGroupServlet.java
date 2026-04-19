package com.college.servlets;

import com.college.dao.NoticeGroupDAO;
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

@WebServlet("/api/notice-groups/*")
public class NoticeGroupServlet extends BaseServlet {
    private final NoticeGroupDAO noticeGroupDAO = new NoticeGroupDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        int userId = currentUserId(req);

        if (pathInfo == null || "/".equals(pathInfo)) {
            try {
                List<Map<String, Object>> groups = noticeGroupDAO.getGroupsForUser(userId);
                JsonUtil.sendSuccess(resp, groupListToJson(groups));
            } catch (Exception e) {
                JsonUtil.sendError(resp, 500, "Could not load groups.");
            }
            return;
        }

        if ("/discover".equals(pathInfo)) {
            try {
                List<Map<String, Object>> groups = noticeGroupDAO.getDiscoverableGroups(userId);
                JsonUtil.sendSuccess(resp, groupListToJson(groups));
            } catch (Exception e) {
                JsonUtil.sendError(resp, 500, "Could not load discoverable groups.");
            }
            return;
        }

        // /api/notice-groups/{id}/notices
        if (pathInfo.matches("/\\d+/notices")) {
            int groupId = Integer.parseInt(pathInfo.split("/")[1]);
            try {
                if (!noticeGroupDAO.isMember(groupId, userId)) {
                    JsonUtil.sendError(resp, 403, "Not a member of this group");
                    return;
                }
                List<Map<String, Object>> notices = noticeGroupDAO.getGroupNotices(groupId);
                List<String> items = new ArrayList<>();
                for (Map<String, Object> n : notices) {
                    items.add(JsonUtil.object(
                            "id", JsonUtil.num((Integer) n.get("id")),
                            "title", JsonUtil.str((String) n.get("title")),
                            "body", JsonUtil.str((String) n.get("body")),
                            "postedByName", JsonUtil.str((String) n.get("postedByName")),
                            "createdAt", JsonUtil.date((LocalDateTime) n.get("createdAt"))
                    ));
                }
                JsonUtil.sendSuccess(resp, JsonUtil.array(items));
            } catch (Exception e) {
                JsonUtil.sendError(resp, 500, "Could not load notices.");
            }
            return;
        }

        // /api/notice-groups/{id}/members
        if (pathInfo.matches("/\\d+/members")) {
            int groupId = Integer.parseInt(pathInfo.split("/")[1]);
            try {
                if (!noticeGroupDAO.isMember(groupId, userId)) {
                    JsonUtil.sendError(resp, 403, "Not a member");
                    return;
                }
                List<Map<String, Object>> members = noticeGroupDAO.getMembers(groupId);
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

        // /api/notice-groups/{id}/pending
        if (pathInfo.matches("/\\d+/pending")) {
            int groupId = Integer.parseInt(pathInfo.split("/")[1]);
            try {
                if (!noticeGroupDAO.isOwnerOrAdmin(groupId, userId)) {
                    JsonUtil.sendError(resp, 403, "Not an admin of this group");
                    return;
                }
                List<Map<String, Object>> requests = noticeGroupDAO.getPendingRequests(groupId);
                List<String> items = new ArrayList<>();
                for (Map<String, Object> r : requests) {
                    items.add(JsonUtil.object(
                            "userId", JsonUtil.num((Integer) r.get("userId")),
                            "username", JsonUtil.str((String) r.get("username")),
                            "fullName", JsonUtil.str((String) r.get("fullName")),
                            "requestedAt", JsonUtil.date((LocalDateTime) r.get("requestedAt"))
                    ));
                }
                JsonUtil.sendSuccess(resp, JsonUtil.array(items));
            } catch (Exception e) {
                JsonUtil.sendError(resp, 500, "Could not load pending requests.");
            }
            return;
        }

        // /api/notice-groups/{id} — group info
        if (pathInfo.matches("/\\d+")) {
            int groupId = Integer.parseInt(pathInfo.substring(1));
            try {
                Map<String, Object> info = noticeGroupDAO.getGroupInfo(groupId);
                if (info == null) {
                    JsonUtil.sendError(resp, 404, "Group not found");
                    return;
                }
                boolean member = noticeGroupDAO.isMember(groupId, userId);
                boolean pending = noticeGroupDAO.hasPendingRequest(groupId, userId);
                JsonUtil.sendSuccess(resp, JsonUtil.object(
                        "groupId", JsonUtil.num((Integer) info.get("groupId")),
                        "groupName", JsonUtil.str((String) info.get("groupName")),
                        "description", JsonUtil.str((String) info.get("description")),
                        "joinPolicy", JsonUtil.str((String) info.get("joinPolicy")),
                        "creatorName", JsonUtil.str((String) info.get("creatorName")),
                        "isMember", JsonUtil.bool(member),
                        "hasPendingRequest", JsonUtil.bool(pending),
                        "createdAt", JsonUtil.date((LocalDateTime) info.get("createdAt"))
                ));
            } catch (Exception e) {
                JsonUtil.sendError(resp, 500, "Could not load group info.");
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
            String joinPolicy = JsonUtil.extractString(body, "joinPolicy");
            if (joinPolicy == null) joinPolicy = "INVITE_ONLY";

            if (ValidationUtil.isBlank(name)) {
                JsonUtil.sendError(resp, 400, "Group name is required");
                return;
            }
            try {
                int groupId = noticeGroupDAO.createGroup(name, desc, joinPolicy, userId);
                noticeGroupDAO.addMember(groupId, userId, "OWNER", "ACTIVE");
                JsonUtil.sendCreated(resp, JsonUtil.object(
                        "status", JsonUtil.str("ok"),
                        "groupId", JsonUtil.num(groupId)
                ));
            } catch (Exception e) {
                JsonUtil.sendError(resp, 500, "Failed to create group.");
            }
            return;
        }

        // Post notice to group: /api/notice-groups/{id}/notices
        if (pathInfo.matches("/\\d+/notices")) {
            int groupId = Integer.parseInt(pathInfo.split("/")[1]);
            String body = JsonUtil.readBody(req);
            String title = InputSanitizer.normalizeText(JsonUtil.extractString(body, "title"));
            String noticeBody = InputSanitizer.normalizeText(JsonUtil.extractString(body, "body"));

            if (ValidationUtil.isBlank(title) || ValidationUtil.isBlank(noticeBody)) {
                JsonUtil.sendError(resp, 400, "Title and body are required");
                return;
            }
            try {
                if (!noticeGroupDAO.isMember(groupId, userId)) {
                    JsonUtil.sendError(resp, 403, "Not a member");
                    return;
                }
                noticeGroupDAO.postNotice(groupId, title, noticeBody, userId);
                JsonUtil.sendCreated(resp, "{\"status\":\"ok\"}");
            } catch (Exception e) {
                JsonUtil.sendError(resp, 500, "Failed to post notice.");
            }
            return;
        }

        // Invite user: /api/notice-groups/{id}/invite
        if (pathInfo.matches("/\\d+/invite")) {
            int groupId = Integer.parseInt(pathInfo.split("/")[1]);
            String body = JsonUtil.readBody(req);
            String username = JsonUtil.extractString(body, "username");

            if (ValidationUtil.isBlank(username)) {
                JsonUtil.sendError(resp, 400, "Username is required");
                return;
            }
            try {
                if (!noticeGroupDAO.isMember(groupId, userId)) {
                    JsonUtil.sendError(resp, 403, "Not a member");
                    return;
                }
                User invitee = userDAO.findByUsername(username);
                if (invitee == null) {
                    JsonUtil.sendError(resp, 404, "User not found");
                    return;
                }
                noticeGroupDAO.addMember(groupId, invitee.getUserId(), "MEMBER", "ACTIVE");
                JsonUtil.sendSuccess(resp, "{\"status\":\"ok\"}");
            } catch (Exception e) {
                JsonUtil.sendError(resp, 500, "Failed to invite user.");
            }
            return;
        }

        // Request join: /api/notice-groups/{id}/request
        if (pathInfo.matches("/\\d+/request")) {
            int groupId = Integer.parseInt(pathInfo.split("/")[1]);
            try {
                if (noticeGroupDAO.isMember(groupId, userId)) {
                    JsonUtil.sendError(resp, 400, "Already a member");
                    return;
                }
                if (noticeGroupDAO.hasPendingRequest(groupId, userId)) {
                    JsonUtil.sendError(resp, 400, "Request already pending");
                    return;
                }
                noticeGroupDAO.addMember(groupId, userId, "MEMBER", "PENDING");
                JsonUtil.sendSuccess(resp, "{\"status\":\"ok\"}");
            } catch (Exception e) {
                JsonUtil.sendError(resp, 500, "Failed to submit request.");
            }
            return;
        }

        // Approve member: /api/notice-groups/{id}/approve-member
        if (pathInfo.matches("/\\d+/approve-member")) {
            int groupId = Integer.parseInt(pathInfo.split("/")[1]);
            String body = JsonUtil.readBody(req);
            Integer targetUserId = JsonUtil.extractInt(body, "userId");

            if (targetUserId == null) {
                JsonUtil.sendError(resp, 400, "userId is required");
                return;
            }
            try {
                if (!noticeGroupDAO.isOwnerOrAdmin(groupId, userId)) {
                    JsonUtil.sendError(resp, 403, "Not an admin of this group");
                    return;
                }
                noticeGroupDAO.approveMember(groupId, targetUserId);
                JsonUtil.sendSuccess(resp, "{\"status\":\"ok\"}");
            } catch (Exception e) {
                JsonUtil.sendError(resp, 500, "Failed to approve member.");
            }
            return;
        }

        JsonUtil.sendError(resp, 404, "Unknown endpoint");
    }

    private String groupListToJson(List<Map<String, Object>> groups) {
        List<String> items = new ArrayList<>();
        for (Map<String, Object> g : groups) {
            items.add(JsonUtil.object(
                    "groupId", JsonUtil.num((Integer) g.get("groupId")),
                    "groupName", JsonUtil.str((String) g.get("groupName")),
                    "description", JsonUtil.str((String) g.get("description")),
                    "joinPolicy", JsonUtil.str((String) g.getOrDefault("joinPolicy", "")),
                    "creatorName", JsonUtil.str((String) g.get("creatorName")),
                    "myRole", JsonUtil.str((String) g.getOrDefault("myRole", "")),
                    "createdAt", JsonUtil.date((LocalDateTime) g.get("createdAt"))
            ));
        }
        return JsonUtil.array(items);
    }
}
