package com.college.models;

import java.time.LocalDateTime;

public class FileMetadata {
    private static final long KB = 1024L;
    private static final long MB = KB * 1024L;

    private int fileId;
    private String originalName;
    private String storedName;
    private long fileSize;
    private String fileType;
    private String subjectTag;
    private int uploadedBy;
    private String uploadedByName;
    private LocalDateTime uploadedAt;

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getStoredName() {
        return storedName;
    }

    public void setStoredName(String storedName) {
        this.storedName = storedName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileSizeDisplay() {
        if (fileSize < KB) {
            return fileSize + " B";
        }
        if (fileSize < MB) {
            long value = Math.max(1L, fileSize / KB);
            return value + " KB";
        }
        double value = (double) fileSize / MB;
        return String.format("%.1f MB", value);
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getSubjectTag() {
        return subjectTag;
    }

    public void setSubjectTag(String subjectTag) {
        this.subjectTag = subjectTag;
    }

    public int getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(int uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getUploadedByName() {
        return uploadedByName;
    }

    public void setUploadedByName(String uploadedByName) {
        this.uploadedByName = uploadedByName;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
