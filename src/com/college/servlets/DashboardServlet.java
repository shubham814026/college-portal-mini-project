package com.college.servlets;

import com.college.models.FileMetadata;
import com.college.models.LogEntry;
import com.college.models.Notice;
import com.college.service.DashboardService;
import com.college.service.DashboardService.DashboardData;
import com.college.utils.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/dashboard")
public class DashboardServlet extends BaseServlet {
    private final DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            DashboardData data = dashboardService.loadData();

            List<String> noticeItems = new ArrayList<>();
            for (Notice n : data.getRecentNotices()) {
                noticeItems.add(JsonUtil.object(
                        "noticeId", JsonUtil.num(n.getNoticeId()),
                        "title", JsonUtil.str(n.getTitle()),
                        "body", JsonUtil.str(n.getBody()),
                        "postedByName", JsonUtil.str(n.getPostedByName()),
                        "createdAt", JsonUtil.date(n.getCreatedAt())
                ));
            }

            List<String> fileItems = new ArrayList<>();
            for (FileMetadata f : data.getRecentFiles()) {
                fileItems.add(JsonUtil.object(
                        "fileId", JsonUtil.num(f.getFileId()),
                        "originalName", JsonUtil.str(f.getOriginalName()),
                        "fileSize", JsonUtil.num(f.getFileSize()),
                        "fileSizeDisplay", JsonUtil.str(f.getFileSizeDisplay()),
                        "fileType", JsonUtil.str(f.getFileType()),
                        "uploadedByName", JsonUtil.str(f.getUploadedByName()),
                        "uploadedAt", JsonUtil.date(f.getUploadedAt())
                ));
            }

            List<String> logItems = new ArrayList<>();
            for (LogEntry l : data.getRecentLogs()) {
                logItems.add(JsonUtil.object(
                        "logId", JsonUtil.num(l.getLogId()),
                        "username", JsonUtil.str(l.getUsername()),
                        "action", JsonUtil.str(l.getAction()),
                        "loggedAt", JsonUtil.date(l.getLoggedAt())
                ));
            }

            JsonUtil.sendSuccess(resp, JsonUtil.object(
                    "notices", JsonUtil.array(noticeItems),
                    "files", JsonUtil.array(fileItems),
                    "logs", JsonUtil.array(logItems)
            ));
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load dashboard data.");
        }
    }
}
