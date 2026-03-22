package com.college.service;

import com.college.dao.FileDAO;
import com.college.models.FileMetadata;
import com.college.utils.FileStorageUtil;
import com.college.utils.RequestContext;
import com.college.utils.RmiClientUtil;

import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class FileService {
    private final FileDAO fileDAO = new FileDAO();

    public List<FileMetadata> getAllFiles() throws SQLException {
        return fileDAO.getAllFiles();
    }

    public FileMetadata findById(int fileId) throws SQLException {
        return fileDAO.findById(fileId);
    }

    public void uploadFile(Part filePart, String originalName, String subjectTag, int userId, String uploadPath)
            throws IOException, SQLException {
        long startedAt = System.currentTimeMillis();
        String storedName = FileStorageUtil.saveFile(filePart, originalName, uploadPath);
        String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        fileDAO.insertMetadata(originalName, storedName, filePart.getSize(), extension, userId, subjectTag);
        RmiClientUtil.safeLogEvent(userId, "FILE_UPLOAD");
        System.out.println("[" + RequestContext.getRequestId() + "] FileService.uploadFile success in "
            + (System.currentTimeMillis() - startedAt) + "ms");
    }

    public File resolveStoredFile(String uploadPath, FileMetadata metadata) {
        return new File(uploadPath, metadata.getStoredName());
    }
}
