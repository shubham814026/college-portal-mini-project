package com.college.dao;

import com.college.models.Message;
import com.college.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public void saveMessage(int senderId, Integer receiverId, String room, String content) throws SQLException {
        String sql = "INSERT INTO messages(sender_id, receiver_id, room, content) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            if (receiverId == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, receiverId);
            }
            ps.setString(3, room);
            ps.setString(4, content);
            ps.executeUpdate();
        }
    }

    public List<Message> getRecentMessagesForRoom(String room, int limit) throws SQLException {
        String sql = "SELECT message_id, sender_id, receiver_id, room, content, sent_at " +
                "FROM messages WHERE room = ? ORDER BY sent_at DESC LIMIT ?";

        List<Message> messages = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message m = new Message();
                    m.setMessageId(rs.getInt("message_id"));
                    m.setSenderId(rs.getInt("sender_id"));
                    int receiver = rs.getInt("receiver_id");
                    m.setReceiverId(rs.wasNull() ? null : receiver);
                    m.setRoom(rs.getString("room"));
                    m.setContent(rs.getString("content"));
                    m.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
                    messages.add(m);
                }
            }
        }

        return messages;
    }

    public List<Message> getRecentDirectMessagesBetweenUsers(int userA, int userB, int limit) throws SQLException {
        String sql = "SELECT message_id, sender_id, receiver_id, room, content, sent_at " +
                "FROM messages " +
                "WHERE room = 'direct' AND ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) " +
                "ORDER BY sent_at DESC LIMIT ?";

        List<Message> messages = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userA);
            ps.setInt(2, userB);
            ps.setInt(3, userB);
            ps.setInt(4, userA);
            ps.setInt(5, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message m = new Message();
                    m.setMessageId(rs.getInt("message_id"));
                    m.setSenderId(rs.getInt("sender_id"));
                    int receiver = rs.getInt("receiver_id");
                    m.setReceiverId(rs.wasNull() ? null : receiver);
                    m.setRoom(rs.getString("room"));
                    m.setContent(rs.getString("content"));
                    m.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
                    messages.add(m);
                }
            }
        }

        return messages;
    }
}
