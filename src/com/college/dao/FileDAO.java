package com.college.dao;

import com.college.models.FileMetadata;
import com.college.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class FileDAO {

    private static final String SELECT_COLS =
            "f.file_id, f.original_name, f.stored_name, f.file_size, f.file_type, f.subject_tag, " +
            "f.uploaded_by, f.uploaded_at, u.username, " +
            "f.branch, f.year_of_study, f.semester, f.approval_status, f.reviewed_by, f.reviewed_at";

    private FileMetadata mapRow(ResultSet rs) throws SQLException {
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
        file.setBranch(rs.getString("branch"));
        int year = rs.getInt("year_of_study");
        file.setYearOfStudy(rs.wasNull() ? null : year);
        int sem = rs.getInt("semester");
        file.setSemester(rs.wasNull() ? null : sem);
        file.setApprovalStatus(rs.getString("approval_status"));
        int reviewedBy = rs.getInt("reviewed_by");
        file.setReviewedBy(rs.wasNull() ? null : reviewedBy);
        Timestamp reviewedAt = rs.getTimestamp("reviewed_at");
        file.setReviewedAt(reviewedAt == null ? null : reviewedAt.toLocalDateTime());
        return file;
    }

    public List<FileMetadata> getRecentFiles(int limit) throws SQLException {
        String sql = "SELECT " + SELECT_COLS +
                " FROM files f JOIN users u ON f.uploaded_by = u.user_id ORDER BY f.uploaded_at DESC LIMIT ?";
        List<FileMetadata> files = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) files.add(mapRow(rs));
            }
        }
        return files;
    }

    public List<FileMetadata> getAllFiles() throws SQLException {
        String sql = "SELECT " + SELECT_COLS +
                " FROM files f JOIN users u ON f.uploaded_by = u.user_id ORDER BY f.uploaded_at DESC";
        List<FileMetadata> files = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) files.add(mapRow(rs));
        }
        return files;
    }

    public List<FileMetadata> getApprovedFiles() throws SQLException {
        String sql = "SELECT " + SELECT_COLS +
                " FROM files f JOIN users u ON f.uploaded_by = u.user_id " +
                "WHERE f.approval_status = 'APPROVED' ORDER BY f.uploaded_at DESC";
        List<FileMetadata> files = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) files.add(mapRow(rs));
        }
        return files;
    }

    public List<FileMetadata> getApprovedFilesByFolder(String branch, Integer year, Integer semester, String subject)
            throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT " + SELECT_COLS +
                " FROM files f JOIN users u ON f.uploaded_by = u.user_id WHERE f.approval_status = 'APPROVED'");
        List<Object> params = new ArrayList<>();

        if (branch != null) { sql.append(" AND f.branch = ?"); params.add(branch); }
        if (year != null) { sql.append(" AND f.year_of_study = ?"); params.add(year); }
        if (semester != null) { sql.append(" AND f.semester = ?"); params.add(semester); }
        if (subject != null) { sql.append(" AND f.subject_tag = ?"); params.add(subject); }

        sql.append(" ORDER BY f.uploaded_at DESC");

        List<FileMetadata> files = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Integer) ps.setInt(i + 1, (Integer) p);
                else ps.setString(i + 1, (String) p);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) files.add(mapRow(rs));
            }
        }
        return files;
    }

    public List<FileMetadata> getFilesByApprovalStatus(String status) throws SQLException {
        String sql = "SELECT " + SELECT_COLS +
                " FROM files f JOIN users u ON f.uploaded_by = u.user_id " +
                "WHERE f.approval_status = ? ORDER BY f.uploaded_at DESC";
        List<FileMetadata> files = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) files.add(mapRow(rs));
            }
        }
        return files;
    }

    public FileMetadata findById(int fileId) throws SQLException {
        String sql = "SELECT " + SELECT_COLS +
                " FROM files f JOIN users u ON f.uploaded_by = u.user_id WHERE f.file_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapRow(rs);
            }
        }
    }

    public void insertMetadata(String originalName, String storedName, long size, String type,
                               int userId, String subjectTag) throws SQLException {
        insertMetadataFull(originalName, storedName, size, type, userId, subjectTag,
                null, null, null, "APPROVED");
    }

    public void insertMetadataFull(String originalName, String storedName, long size, String type,
                                   int userId, String subjectTag, String branch,
                                   Integer year, Integer semester, String approvalStatus) throws SQLException {
        String sql = "INSERT INTO files(original_name, stored_name, file_size, file_type, uploaded_by, " +
                "subject_tag, branch, year_of_study, semester, approval_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, originalName);
            ps.setString(2, storedName);
            ps.setLong(3, size);
            ps.setString(4, type);
            ps.setInt(5, userId);
            ps.setString(6, subjectTag);
            ps.setString(7, branch);
            if (year != null) ps.setInt(8, year); else ps.setNull(8, java.sql.Types.INTEGER);
            if (semester != null) ps.setInt(9, semester); else ps.setNull(9, java.sql.Types.INTEGER);
            ps.setString(10, approvalStatus != null ? approvalStatus : "APPROVED");
            ps.executeUpdate();
        }
    }

    public void updateApprovalStatus(int fileId, String status, int reviewerId) throws SQLException {
        String sql = "UPDATE files SET approval_status = ?, reviewed_by = ?, reviewed_at = NOW() WHERE file_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, reviewerId);
            ps.setInt(3, fileId);
            ps.executeUpdate();
        }
    }

    public void deleteFile(int fileId) throws SQLException {
        String sql = "DELETE FROM files WHERE file_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fileId);
            ps.executeUpdate();
        }
    }
}
