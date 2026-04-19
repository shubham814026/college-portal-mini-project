package com.college.dao;

import com.college.utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatGroupDAO {

    public int createGroup(String name, String description, int createdBy) throws SQLException {
        String sql = "INSERT INTO chat_groups(group_name, description, created_by) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, createdBy);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public void addMember(int groupId, int userId, String role) throws SQLException {
        String sql = "INSERT INTO chat_group_members(group_id, user_id, role) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE role = VALUES(role)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            ps.setString(3, role);
            ps.executeUpdate();
        }
    }

    public boolean isMember(int groupId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM chat_group_members WHERE group_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public boolean isOwner(int groupId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM chat_group_members WHERE group_id = ? AND user_id = ? AND role = 'OWNER'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public List<Map<String, Object>> getGroupsForUser(int userId) throws SQLException {
        String sql = "SELECT g.group_id, g.group_name, g.description, g.created_by, g.created_at, " +
                "m.role, u.full_name AS creator_name " +
                "FROM chat_groups g " +
                "JOIN chat_group_members m ON g.group_id = m.group_id " +
                "JOIN users u ON g.created_by = u.user_id " +
                "WHERE m.user_id = ? ORDER BY g.created_at DESC";
        List<Map<String, Object>> groups = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> g = new HashMap<>();
                    g.put("groupId", rs.getInt("group_id"));
                    g.put("groupName", rs.getString("group_name"));
                    g.put("description", rs.getString("description"));
                    g.put("createdBy", rs.getInt("created_by"));
                    g.put("creatorName", rs.getString("creator_name"));
                    g.put("myRole", rs.getString("role"));
                    g.put("createdAt", rs.getTimestamp("created_at").toLocalDateTime());
                    groups.add(g);
                }
            }
        }
        return groups;
    }

    public List<Map<String, Object>> getMembers(int groupId) throws SQLException {
        String sql = "SELECT m.user_id, m.role, m.joined_at, u.username, u.full_name " +
                "FROM chat_group_members m JOIN users u ON m.user_id = u.user_id " +
                "WHERE m.group_id = ? ORDER BY m.role, u.full_name";
        List<Map<String, Object>> members = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("userId", rs.getInt("user_id"));
                    m.put("username", rs.getString("username"));
                    m.put("fullName", rs.getString("full_name"));
                    m.put("role", rs.getString("role"));
                    m.put("joinedAt", rs.getTimestamp("joined_at").toLocalDateTime());
                    members.add(m);
                }
            }
        }
        return members;
    }

    public void saveGroupMessage(int groupId, int senderId, String content) throws SQLException {
        String sql = "INSERT INTO chat_group_messages(group_id, sender_id, content) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, senderId);
            ps.setString(3, content);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> getGroupMessages(int groupId, int limit) throws SQLException {
        String sql = "SELECT m.message_id, m.sender_id, m.content, m.sent_at, u.username, u.full_name " +
                "FROM chat_group_messages m JOIN users u ON m.sender_id = u.user_id " +
                "WHERE m.group_id = ? ORDER BY m.sent_at DESC LIMIT ?";
        List<Map<String, Object>> messages = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> msg = new HashMap<>();
                    msg.put("messageId", rs.getInt("message_id"));
                    msg.put("senderId", rs.getInt("sender_id"));
                    msg.put("senderUsername", rs.getString("username"));
                    msg.put("senderName", rs.getString("full_name"));
                    msg.put("content", rs.getString("content"));
                    msg.put("sentAt", rs.getTimestamp("sent_at").toLocalDateTime());
                    messages.add(msg);
                }
            }
        }
        return messages;
    }
}
