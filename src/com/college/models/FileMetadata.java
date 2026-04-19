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
    // V2 fields
    private String branch;
    private Integer yearOfStudy;
    private Integer semester;
    private String approvalStatus;
    private Integer reviewedBy;
    private LocalDateTime reviewedAt;

    public int getFileId() { return fileId; }
    public void setFileId(int fileId) { this.fileId = fileId; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getFileSizeDisplay() {
        if (fileSize < KB) return fileSize + " B";
        if (fileSize < MB) return Math.max(1L, fileSize / KB) + " KB";
        return String.format("%.1f MB", (double) fileSize / MB);
    }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getSubjectTag() { return subjectTag; }
    public void setSubjectTag(String subjectTag) { this.subjectTag = subjectTag; }

    public int getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(int uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getUploadedByName() { return uploadedByName; }
    public void setUploadedByName(String uploadedByName) { this.uploadedByName = uploadedByName; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public Integer getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(Integer yearOfStudy) { this.yearOfStudy = yearOfStudy; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public Integer getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Integer reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
