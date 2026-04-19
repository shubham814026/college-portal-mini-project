package com.college.servlets;

import com.college.dao.FileDAO;
import com.college.models.FileMetadata;
import com.college.service.FileService;
import com.college.utils.AppConstants;
import com.college.utils.FileStorageUtil;
import com.college.utils.InputSanitizer;
import com.college.utils.JsonUtil;
import com.college.utils.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/files/*")
@MultipartConfig(maxFileSize = 10485760, maxRequestSize = 10485760)
public class FileServlet extends BaseServlet {
    private final FileService fileService = new FileService();
    private final FileDAO fileDAO = new FileDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.startsWith("/download")) {
            handleDownload(req, resp);
            return;
        }

        if (pathInfo != null && pathInfo.equals("/pending")) {
            handlePending(req, resp);
            return;
        }

        // List approved files, optionally filtered by folder path
        try {
            String branch = req.getParameter("branch");
            String yearStr = req.getParameter("year");
            String semStr = req.getParameter("semester");
            String subject = req.getParameter("subject");

            List<FileMetadata> files;
            if (branch != null || yearStr != null || semStr != null || subject != null) {
                Integer year = yearStr != null ? Integer.parseInt(yearStr) : null;
                Integer sem = semStr != null ? Integer.parseInt(semStr) : null;
                files = fileDAO.getApprovedFilesByFolder(branch, year, sem, subject);
            } else {
                files = fileDAO.getApprovedFiles();
            }

            List<String> items = new ArrayList<>();
            for (FileMetadata f : files) {
                items.add(fileToJson(f));
            }
            JsonUtil.sendSuccess(resp, JsonUtil.array(items));
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load files.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.matches("/\\d+/approve")) {
            handleApproval(req, resp, true);
            return;
        }
        if (pathInfo != null && pathInfo.matches("/\\d+/reject")) {
            handleApproval(req, resp, false);
            return;
        }

        // Upload file
        Part filePart = req.getPart("file");
        String subjectTag = InputSanitizer.normalizeText(req.getParameter("subjectTag"));
        String branch = InputSanitizer.normalizeText(req.getParameter("branch"));
        String yearStr = req.getParameter("year");
        String semStr = req.getParameter("semester");

        if (ValidationUtil.isBlank(subjectTag)) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Subject is compulsory.");
            return;
        }

        Integer year = null;
        Integer sem = null;
        try {
            if (yearStr != null && !yearStr.trim().isEmpty()) {
                year = Integer.parseInt(yearStr);
            }
            if (semStr != null && !semStr.trim().isEmpty()) {
                sem = Integer.parseInt(semStr);
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid year or semester.");
            return;
        }

        if (year != null && sem != null) {
            int expectedSem1 = (year * 2) - 1;
            int expectedSem2 = year * 2;
            if (sem != expectedSem1 && sem != expectedSem2) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Semester " + sem + " does not match Year " + year + ".");
                return;
            }
        }

        if (filePart == null || filePart.getSize() == 0) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "No file provided");
            return;
        }

        String originalName = InputSanitizer.safeFileName(filePart.getSubmittedFileName());
        if (!ValidationUtil.isAllowedFileExtension(originalName)) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Only .pdf, .docx, .pptx, and .zip files are allowed.");
            return;
        }

        if (filePart.getSize() > FileStorageUtil.getMaxFileSize()) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "File size exceeds the 10 MB limit.");
            return;
        }

        String uploadPath = getServletContext().getRealPath("/uploads");
        if (uploadPath == null) {
            uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
        }

        try {
            String role = currentRole(req);
            // Faculty/Admin uploads are auto-approved; student uploads are pending
            String status = (AppConstants.ROLE_ADMIN.equals(role) || AppConstants.ROLE_FACULTY.equals(role))
                    ? "APPROVED" : "PENDING";

            fileService.uploadFileWithMeta(filePart, originalName, subjectTag, currentUserId(req),
                    uploadPath, branch, year, sem, status);

            JsonUtil.sendCreated(resp, JsonUtil.object(
                    "status", JsonUtil.str("ok"),
                    "approvalStatus", JsonUtil.str(status)
            ));
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Upload failed: " + e.getMessage());
        }
    }

    private void handleDownload(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String fileIdParam = req.getParameter("fileId");
        if (ValidationUtil.isBlank(fileIdParam)) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing fileId");
            return;
        }

        try {
            int fileId = Integer.parseInt(fileIdParam);
            FileMetadata metadata = fileService.findById(fileId);
            if (metadata == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Resource not available.");
                return;
            }

            String uploadPath = getServletContext().getRealPath("/uploads");
            if (uploadPath == null) {
                uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
            }

            File file = fileService.resolveStoredFile(uploadPath, metadata);
            if (!file.exists()) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Resource not available.");
                return;
            }

            boolean inline = "true".equalsIgnoreCase(req.getParameter("inline"));
            FileStorageUtil.streamFile(file, metadata.getOriginalName(), resp, inline);
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid fileId");
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Download failed");
        }
    }

    private void handlePending(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = currentRole(req);
        if (!AppConstants.ROLE_ADMIN.equals(role) && !AppConstants.ROLE_FACULTY.equals(role)) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            List<FileMetadata> files = fileDAO.getFilesByApprovalStatus("PENDING");
            List<String> items = new ArrayList<>();
            for (FileMetadata f : files) {
                items.add(fileToJson(f));
            }
            JsonUtil.sendSuccess(resp, JsonUtil.array(items));
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load pending files.");
        }
    }

    private void handleApproval(HttpServletRequest req, HttpServletResponse resp, boolean approve)
            throws IOException {
        String role = currentRole(req);
        if (!AppConstants.ROLE_ADMIN.equals(role) && !AppConstants.ROLE_FACULTY.equals(role)) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        String pathInfo = req.getPathInfo();
        String[] parts = pathInfo.split("/");
        int fileId = Integer.parseInt(parts[1]);

        try {
            String status = approve ? "APPROVED" : "REJECTED";
            fileDAO.updateApprovalStatus(fileId, status, currentUserId(req));
            JsonUtil.sendSuccess(resp, JsonUtil.object(
                    "status", JsonUtil.str("ok"),
                    "approvalStatus", JsonUtil.str(status)
            ));
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Approval update failed.");
        }
    }

    private String fileToJson(FileMetadata f) {
        return JsonUtil.object(
                "fileId", JsonUtil.num(f.getFileId()),
                "originalName", JsonUtil.str(f.getOriginalName()),
                "storedName", JsonUtil.str(f.getStoredName()),
                "fileSize", JsonUtil.num(f.getFileSize()),
                "fileSizeDisplay", JsonUtil.str(f.getFileSizeDisplay()),
                "fileType", JsonUtil.str(f.getFileType()),
                "subjectTag", JsonUtil.str(f.getSubjectTag()),
                "branch", JsonUtil.str(f.getBranch()),
                "yearOfStudy", f.getYearOfStudy() != null ? JsonUtil.num(f.getYearOfStudy()) : JsonUtil.nullVal(),
                "semester", f.getSemester() != null ? JsonUtil.num(f.getSemester()) : JsonUtil.nullVal(),
                "uploadedBy", JsonUtil.num(f.getUploadedBy()),
                "uploadedByName", JsonUtil.str(f.getUploadedByName()),
                "uploadedAt", JsonUtil.date(f.getUploadedAt()),
                "approvalStatus", JsonUtil.str(f.getApprovalStatus())
        );
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String role = currentRole(req);
        if (!AppConstants.ROLE_ADMIN.equals(role) && !AppConstants.ROLE_FACULTY.equals(role)) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing file ID");
            return;
        }

        try {
            int fileId = Integer.parseInt(pathInfo.substring(1));
            String uploadPath = getServletContext().getRealPath("/uploads");
            if (uploadPath == null) {
                uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
            }
            fileService.deleteFile(fileId, uploadPath);
            JsonUtil.sendSuccess(resp, JsonUtil.object("status", JsonUtil.str("deleted")));
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid file ID");
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Delete failed: " + e.getMessage());
        }
    }
}
