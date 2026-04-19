package com.college.dao;

import com.college.utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoticeGroupDAO {

    public int createGroup(String name, String description, String joinPolicy, int createdBy) throws SQLException {
        String sql = "INSERT INTO notice_groups(group_name, description, join_policy, created_by) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, joinPolicy);
            ps.setInt(4, createdBy);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { return rs.next() ? rs.getInt(1) : -1; }
        }
    }

    public void addMember(int groupId, int userId, String role, String status) throws SQLException {
        String sql = "INSERT INTO notice_group_members(group_id, user_id, role, status) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE role = VALUES(role), status = VALUES(status)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            ps.setString(3, role);
            ps.setString(4, status);
            ps.executeUpdate();
        }
    }

    public boolean isMember(int groupId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM notice_group_members WHERE group_id = ? AND user_id = ? AND status = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public boolean isOwnerOrAdmin(int groupId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM notice_group_members WHERE group_id = ? AND user_id = ? AND status = 'ACTIVE' AND role IN ('OWNER','ADMIN')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public Map<String, Object> getGroupInfo(int groupId) throws SQLException {
        String sql = "SELECT g.group_id, g.group_name, g.description, g.join_policy, g.created_by, g.created_at, u.full_name AS creator_name " +
                "FROM notice_groups g JOIN users u ON g.created_by = u.user_id WHERE g.group_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Map<String, Object> g = new HashMap<>();
                g.put("groupId", rs.getInt("group_id"));
                g.put("groupName", rs.getString("group_name"));
                g.put("description", rs.getString("description"));
                g.put("joinPolicy", rs.getString("join_policy"));
                g.put("createdBy", rs.getInt("created_by"));
                g.put("creatorName", rs.getString("creator_name"));
                g.put("createdAt", rs.getTimestamp("created_at").toLocalDateTime());
                return g;
            }
        }
    }

    public List<Map<String, Object>> getGroupsForUser(int userId) throws SQLException {
        String sql = "SELECT g.group_id, g.group_name, g.description, g.join_policy, g.created_by, g.created_at, " +
                "m.role, m.status, u.full_name AS creator_name " +
                "FROM notice_groups g " +
                "JOIN notice_group_members m ON g.group_id = m.group_id " +
                "JOIN users u ON g.created_by = u.user_id " +
                "WHERE m.user_id = ? AND m.status = 'ACTIVE' ORDER BY g.created_at DESC";
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
                    g.put("joinPolicy", rs.getString("join_policy"));
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

    public List<Map<String, Object>> getDiscoverableGroups(int userId) throws SQLException {
        String sql = "SELECT g.group_id, g.group_name, g.description, g.join_policy, g.created_by, g.created_at, " +
                "u.full_name AS creator_name " +
                "FROM notice_groups g JOIN users u ON g.created_by = u.user_id " +
                "WHERE g.join_policy = 'REQUEST_JOIN' AND g.group_id NOT IN " +
                "(SELECT group_id FROM notice_group_members WHERE user_id = ?) " +
                "ORDER BY g.created_at DESC";
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
                    g.put("joinPolicy", rs.getString("join_policy"));
                    g.put("creatorName", rs.getString("creator_name"));
                    g.put("createdAt", rs.getTimestamp("created_at").toLocalDateTime());
                    groups.add(g);
                }
            }
        }
        return groups;
    }

    public List<Map<String, Object>> getPendingRequests(int groupId) throws SQLException {
        String sql = "SELECT m.id, m.user_id, m.joined_at, u.username, u.full_name " +
                "FROM notice_group_members m JOIN users u ON m.user_id = u.user_id " +
                "WHERE m.group_id = ? AND m.status = 'PENDING'";
        List<Map<String, Object>> requests = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> r = new HashMap<>();
                    r.put("userId", rs.getInt("user_id"));
                    r.put("username", rs.getString("username"));
                    r.put("fullName", rs.getString("full_name"));
                    r.put("requestedAt", rs.getTimestamp("joined_at").toLocalDateTime());
                    requests.add(r);
                }
            }
        }
        return requests;
    }

    public List<Map<String, Object>> getMembers(int groupId) throws SQLException {
        String sql = "SELECT m.user_id, m.role, m.joined_at, u.username, u.full_name " +
                "FROM notice_group_members m JOIN users u ON m.user_id = u.user_id " +
                "WHERE m.group_id = ? AND m.status = 'ACTIVE' ORDER BY m.role, u.full_name";
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

    public void postNotice(int groupId, String title, String body, int postedBy) throws SQLException {
        String sql = "INSERT INTO group_notices(group_id, title, body, posted_by) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setString(2, title);
            ps.setString(3, body);
            ps.setInt(4, postedBy);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> getGroupNotices(int groupId) throws SQLException {
        String sql = "SELECT gn.id, gn.title, gn.body, gn.posted_by, gn.created_at, u.full_name " +
                "FROM group_notices gn JOIN users u ON gn.posted_by = u.user_id " +
                "WHERE gn.group_id = ? ORDER BY gn.created_at DESC";
        List<Map<String, Object>> notices = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> n = new HashMap<>();
                    n.put("id", rs.getInt("id"));
                    n.put("title", rs.getString("title"));
                    n.put("body", rs.getString("body"));
                    n.put("postedBy", rs.getInt("posted_by"));
                    n.put("postedByName", rs.getString("full_name"));
                    n.put("createdAt", rs.getTimestamp("created_at").toLocalDateTime());
                    notices.add(n);
                }
            }
        }
        return notices;
    }

    public boolean hasPendingRequest(int groupId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM notice_group_members WHERE group_id = ? AND user_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public void approveMember(int groupId, int userId) throws SQLException {
        String sql = "UPDATE notice_group_members SET status = 'ACTIVE' WHERE group_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }
}
