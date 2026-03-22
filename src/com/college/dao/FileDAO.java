package com.college.dao;

import com.college.models.FileMetadata;
import com.college.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FileDAO {

    public List<FileMetadata> getRecentFiles(int limit) throws SQLException {
        String sql = "SELECT f.file_id, f.original_name, f.stored_name, f.file_size, f.file_type, f.subject_tag, " +
                "f.uploaded_by, f.uploaded_at, u.username " +
                "FROM files f JOIN users u ON f.uploaded_by = u.user_id ORDER BY f.uploaded_at DESC LIMIT ?";

        List<FileMetadata> files = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FileMetadata file = new FileMetadata();
                    file.setFileId(rs.getInt("file_id"));
                    file.setOriginalName(rs.getString("original_name"));
                    file.setStoredName(rs.getString("stored_name"));
                    file.setFileSize(rs.getLong("file_size"));
                    file.setFileType(rs.getString("file_type"));
                    file.setSubjectTag(rs.getString("subject_tag"));
                    file.setUploadedBy(rs.getInt("uploaded_by"));
                    file.setUploadedByName(rs.getString("username"));
                    file.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
                    files.add(file);
                }
            }
        }

        return files;
    }

    public List<FileMetadata> getAllFiles() throws SQLException {
        String sql = "SELECT f.file_id, f.original_name, f.stored_name, f.file_size, f.file_type, f.subject_tag, " +
                "f.uploaded_by, f.uploaded_at, u.username " +
                "FROM files f JOIN users u ON f.uploaded_by = u.user_id ORDER BY f.uploaded_at DESC";

        List<FileMetadata> files = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                FileMetadata file = new FileMetadata();
                file.setFileId(rs.getInt("file_id"));
                file.setOriginalName(rs.getString("original_name"));
                file.setStoredName(rs.getString("stored_name"));
                file.setFileSize(rs.getLong("file_size"));
                file.setFileType(rs.getString("file_type"));
                file.setSubjectTag(rs.getString("subject_tag"));
                file.setUploadedBy(rs.getInt("uploaded_by"));
                file.setUploadedByName(rs.getString("username"));
                file.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
                files.add(file);
            }
        }

        return files;
    }

    public FileMetadata findById(int fileId) throws SQLException {
        String sql = "SELECT file_id, original_name, stored_name, file_size, file_type, subject_tag, uploaded_by, uploaded_at " +
                "FROM files WHERE file_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fileId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                FileMetadata file = new FileMetadata();
                file.setFileId(rs.getInt("file_id"));
                file.setOriginalName(rs.getString("original_name"));
                file.setStoredName(rs.getString("stored_name"));
                file.setFileSize(rs.getLong("file_size"));
                file.setFileType(rs.getString("file_type"));
                file.setSubjectTag(rs.getString("subject_tag"));
                file.setUploadedBy(rs.getInt("uploaded_by"));
                file.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
                return file;
            }
        }
    }

    public void insertMetadata(String originalName, String storedName, long size, String type, int userId, String subjectTag)
            throws SQLException {
        String sql = "INSERT INTO files(original_name, stored_name, file_size, file_type, uploaded_by, subject_tag) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, originalName);
            ps.setString(2, storedName);
            ps.setLong(3, size);
            ps.setString(4, type);
            ps.setInt(5, userId);
            ps.setString(6, subjectTag);
            ps.executeUpdate();
        }
    }
}
