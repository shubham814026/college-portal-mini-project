package com.college.servlets;

import com.college.models.FileMetadata;
import com.college.service.FileService;
import com.college.utils.FileStorageUtil;
import com.college.utils.InputSanitizer;
import com.college.utils.ServletResponseUtil;
import com.college.utils.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;

@WebServlet("/files")
@MultipartConfig
public class FileServlet extends BaseServlet {
    private final FileService fileService = new FileService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String action = req.getParameter("action");
        if ("download".equals(action)) {
            handleDownload(req, resp);
            return;
        }

        try {
            req.setAttribute("files", fileService.getAllFiles());
            req.getRequestDispatcher("/student/resources.jsp").forward(req, resp);
        } catch (Exception e) {
            ServletResponseUtil.forwardError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load resources.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Part filePart = req.getPart("file");
        String subjectTag = InputSanitizer.normalizeText(req.getParameter("subjectTag"));

        if (filePart == null || filePart.getSize() == 0) {
            resp.sendRedirect(req.getContextPath() + "/student/upload.jsp?error=nofile");
            return;
        }

        String originalName = extractFileName(filePart);
        if (!ValidationUtil.isAllowedFileExtension(originalName)) {
            resp.sendRedirect(req.getContextPath() + "/student/upload.jsp?error=type");
            return;
        }

        if (filePart.getSize() > FileStorageUtil.getMaxFileSize()) {
            resp.sendRedirect(req.getContextPath() + "/student/upload.jsp?error=size");
            return;
        }

        String uploadPath = getServletContext().getRealPath("/uploads");
        if (uploadPath == null) {
            uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
        }

        try {
            fileService.uploadFile(filePart, originalName, subjectTag, currentUserId(req), uploadPath);

            resp.sendRedirect(req.getContextPath() + "/files?success=uploaded");
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/student/upload.jsp?error=storage");
        }
    }

    private void handleDownload(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String fileIdParam = req.getParameter("fileId");
        if (ValidationUtil.isBlank(fileIdParam)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing fileId");
            return;
        }

        try {
            int fileId = Integer.parseInt(fileIdParam);
            FileMetadata metadata = fileService.findById(fileId);
            if (metadata == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not available.");
                return;
            }

            String uploadPath = getServletContext().getRealPath("/uploads");
            if (uploadPath == null) {
                uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
            }

            File file = fileService.resolveStoredFile(uploadPath, metadata);
            if (!file.exists()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not available.");
                return;
            }

            FileStorageUtil.streamFile(file, metadata.getOriginalName(), resp);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid fileId");
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Download failed");
        }
    }

    private String extractFileName(Part part) {
        return InputSanitizer.safeFileName(part.getSubmittedFileName());
    }
}
