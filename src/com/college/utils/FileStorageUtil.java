package com.college.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

public final class FileStorageUtil {
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private FileStorageUtil() {
    }

    public static long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }

    public static String saveFile(Part filePart, String originalName, String uploadDir) throws IOException {
        String safeName = InputSanitizer.safeFileName(originalName);
        if (safeName == null || !safeName.contains(".")) {
            throw new IOException("Invalid file name");
        }

        originalName = safeName;
        String extension = originalName.substring(originalName.lastIndexOf('.'));
        String baseName = originalName.substring(0, originalName.lastIndexOf('.'));
        String storedName = baseName + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID() + extension;

        File directory = new File(uploadDir);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create upload directory");
        }

        File outputFile = new File(directory, storedName);
        try (InputStream in = filePart.getInputStream();
             OutputStream out = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
        return storedName;
    }

    public static void streamFile(File file, String downloadName, HttpServletResponse response, boolean inline) throws IOException {
        String safeDownloadName = InputSanitizer.safeFileName(downloadName);
        if (safeDownloadName == null) {
            safeDownloadName = "download.bin";
        }

        String encodedName = URLEncoder.encode(safeDownloadName, StandardCharsets.UTF_8.name())
                .replace("+", "%20");

        String contentType = "application/octet-stream";
        if (inline) {
            try {
                contentType = java.nio.file.Files.probeContentType(file.toPath());
                if (contentType == null) {
                    // Fallback for some unknown types, though browser will decide
                    contentType = "application/octet-stream";
                }
            } catch (IOException e) {
                contentType = "application/octet-stream";
            }
        }

        response.setContentType(contentType);
        response.setHeader("X-Content-Type-Options", "nosniff");
        String disposition = inline ? "inline" : "attachment";
        response.setHeader("Content-Disposition", disposition + "; filename=\"" + safeDownloadName + "\"; filename*=UTF-8''" + encodedName);
        response.setContentLengthLong(file.length());

        try (InputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }
}
