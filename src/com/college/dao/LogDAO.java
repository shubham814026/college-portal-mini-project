package com.college.dao;

import com.college.models.LogEntry;
import com.college.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class LogDAO {

    public void insertLog(int userId, String action, Timestamp timestamp) throws SQLException {
        String sql = "INSERT INTO logs(user_id, action, logged_at) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, action);
            ps.setTimestamp(3, timestamp);
            ps.executeUpdate();
        }
    }

    public List<LogEntry> getAllLogs() throws SQLException {
        String sql = "SELECT l.log_id, l.user_id, l.action, l.ip_address, l.logged_at, u.username " +
                "FROM logs l JOIN users u ON l.user_id = u.user_id ORDER BY l.logged_at DESC";

        List<LogEntry> logs = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LogEntry log = new LogEntry();
                log.setLogId(rs.getInt("log_id"));
                log.setUserId(rs.getInt("user_id"));
                log.setUsername(rs.getString("username"));
                log.setAction(rs.getString("action"));
                log.setIpAddress(rs.getString("ip_address"));
                log.setLoggedAt(rs.getTimestamp("logged_at").toLocalDateTime());
                logs.add(log);
            }
        }

        return logs;
    }

    public List<LogEntry> getRecentLogs(int limit) throws SQLException {
        String sql = "SELECT l.log_id, l.user_id, l.action, l.ip_address, l.logged_at, u.username " +
                "FROM logs l JOIN users u ON l.user_id = u.user_id ORDER BY l.logged_at DESC LIMIT ?";

        List<LogEntry> logs = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LogEntry log = new LogEntry();
                    log.setLogId(rs.getInt("log_id"));
                    log.setUserId(rs.getInt("user_id"));
                    log.setUsername(rs.getString("username"));
                    log.setAction(rs.getString("action"));
                    log.setIpAddress(rs.getString("ip_address"));
                    log.setLoggedAt(rs.getTimestamp("logged_at").toLocalDateTime());
                    logs.add(log);
                }
            }
        }

        return logs;
    }
}
