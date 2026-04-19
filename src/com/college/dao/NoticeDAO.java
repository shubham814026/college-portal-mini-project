package com.college.dao;

import com.college.models.Notice;
import com.college.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class NoticeDAO {

    public List<Notice> getRecentActiveNotices(int limit) throws SQLException {
        String sql = "SELECT n.notice_id, n.title, n.body, n.posted_by, n.created_at, u.full_name " +
                "FROM notices n JOIN users u ON n.posted_by = u.user_id " +
                "WHERE n.is_active = TRUE ORDER BY n.created_at DESC LIMIT ?";
        List<Notice> notices = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notice notice = new Notice();
                    notice.setNoticeId(rs.getInt("notice_id"));
                    notice.setTitle(rs.getString("title"));
                    notice.setBody(rs.getString("body"));
                    notice.setPostedBy(rs.getInt("posted_by"));
                    notice.setPostedByName(rs.getString("full_name"));
                    notice.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    notices.add(notice);
                }
            }
        }

        return notices;
    }

    public List<Notice> getAllActiveNotices() throws SQLException {
        String sql = "SELECT n.notice_id, n.title, n.body, n.posted_by, n.created_at, u.full_name " +
                "FROM notices n JOIN users u ON n.posted_by = u.user_id WHERE n.is_active = TRUE ORDER BY n.created_at DESC";
        List<Notice> notices = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Notice notice = new Notice();
                notice.setNoticeId(rs.getInt("notice_id"));
                notice.setTitle(rs.getString("title"));
                notice.setBody(rs.getString("body"));
                notice.setPostedBy(rs.getInt("posted_by"));
                notice.setPostedByName(rs.getString("full_name"));
                notice.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                notices.add(notice);
            }
        }

        return notices;
    }

    public int insertNotice(String title, String body, int adminId) throws SQLException {
        String sql = "INSERT INTO notices(title, body, posted_by) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
               PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, body);
            ps.setInt(3, adminId);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public void updateNotice(int noticeId, String title, String body) throws SQLException {
        String sql = "UPDATE notices SET title = ?, body = ? WHERE notice_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, body);
            ps.setInt(3, noticeId);
            ps.executeUpdate();
        }
    }

    public void deleteNotice(int noticeId) throws SQLException {
        String sql = "DELETE FROM notices WHERE notice_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, noticeId);
            ps.executeUpdate();
        }
    }
}
