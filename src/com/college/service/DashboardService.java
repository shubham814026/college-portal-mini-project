package com.college.service;

import com.college.dao.FileDAO;
import com.college.dao.LogDAO;
import com.college.dao.NoticeDAO;
import com.college.models.FileMetadata;
import com.college.models.LogEntry;
import com.college.models.Notice;
import com.college.utils.RequestContext;

import java.sql.SQLException;
import java.util.List;

public class DashboardService {
    public static final class DashboardData {
        private final List<Notice> recentNotices;
        private final List<FileMetadata> recentFiles;
        private final List<LogEntry> recentLogs;

        public DashboardData(List<Notice> recentNotices, List<FileMetadata> recentFiles, List<LogEntry> recentLogs) {
            this.recentNotices = recentNotices;
            this.recentFiles = recentFiles;
            this.recentLogs = recentLogs;
        }

        public List<Notice> getRecentNotices() {
            return recentNotices;
        }

        public List<FileMetadata> getRecentFiles() {
            return recentFiles;
        }

        public List<LogEntry> getRecentLogs() {
            return recentLogs;
        }
    }

    private static final int DASHBOARD_LIMIT = 10;

    private final NoticeDAO noticeDAO = new NoticeDAO();
    private final FileDAO fileDAO = new FileDAO();
    private final LogDAO logDAO = new LogDAO();

    public DashboardData loadData() throws SQLException {
        long startedAt = System.currentTimeMillis();
        DashboardData data = new DashboardData(
                noticeDAO.getRecentActiveNotices(DASHBOARD_LIMIT),
                fileDAO.getRecentFiles(DASHBOARD_LIMIT),
                logDAO.getRecentLogs(DASHBOARD_LIMIT)
        );
        System.out.println("[" + RequestContext.getRequestId() + "] DashboardService.loadData success in "
            + (System.currentTimeMillis() - startedAt) + "ms");
        return data;
    }
}
