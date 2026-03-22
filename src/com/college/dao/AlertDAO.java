package com.college.dao;

import com.college.models.Alert;
import com.college.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlertDAO {

    public void insertAlert(String message, int adminId) throws SQLException {
        String sql = "INSERT INTO alerts(message, sent_by) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, message);
            ps.setInt(2, adminId);
            ps.executeUpdate();
        }
    }

    public List<Alert> getRecentAlerts(int limit) throws SQLException {
        String sql = "SELECT a.alert_id, a.message, a.sent_by, a.sent_at, u.username " +
                "FROM alerts a JOIN users u ON a.sent_by = u.user_id ORDER BY a.sent_at DESC LIMIT ?";

        List<Alert> alerts = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Alert alert = new Alert();
                    alert.setAlertId(rs.getInt("alert_id"));
                    alert.setMessage(rs.getString("message"));
                    alert.setSentBy(rs.getInt("sent_by"));
                    alert.setSentByName(rs.getString("username"));
                    alert.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
                    alerts.add(alert);
                }
            }
        }

        return alerts;
    }
}
