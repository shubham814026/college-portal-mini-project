package com.college.dao;

import com.college.models.User;
import com.college.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class UserDAO {

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, role, full_name, email, failed_attempts, locked_until FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setFailedAttempts(rs.getInt("failed_attempts"));
                Timestamp lockedUntilTs = rs.getTimestamp("locked_until");
                user.setLockedUntil(lockedUntilTs == null ? null : lockedUntilTs.toLocalDateTime());
                return user;
            }
        }
    }

    public User findByUsernameAndPasswordHash(String username, String passwordHash) throws SQLException {
        String sql = "SELECT user_id, username, role, full_name, email FROM users " +
                "WHERE username = ? AND password_hash = ? AND (locked_until IS NULL OR locked_until < NOW())";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                return user;
            }
        }
    }

    public void incrementFailedAttempts(String username) throws SQLException {
        String sql = "UPDATE users SET failed_attempts = failed_attempts + 1, " +
                "locked_until = CASE WHEN failed_attempts >= 2 THEN DATE_ADD(NOW(), INTERVAL 15 MINUTE) ELSE locked_until END " +
                "WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }

    public void resetFailedAttempts(String username) throws SQLException {
        String sql = "UPDATE users SET failed_attempts = 0, locked_until = NULL WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }

    public LocalDateTime getLockedUntil(String username) throws SQLException {
        String sql = "SELECT locked_until FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("locked_until");
                    return ts == null ? null : ts.toLocalDateTime();
                }
                return null;
            }
        }
    }
}
