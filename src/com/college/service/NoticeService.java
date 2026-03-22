package com.college.service;

import com.college.dao.NoticeDAO;
import com.college.models.Notice;
import com.college.network.MulticastSender;
import com.college.utils.RequestContext;
import com.college.utils.RmiClientUtil;

import java.sql.SQLException;
import java.util.List;

public class NoticeService {
    private final NoticeDAO noticeDAO = new NoticeDAO();

    public List<Notice> getAllActiveNotices() throws SQLException {
        return noticeDAO.getAllActiveNotices();
    }

    public void postNotice(String title, String body, int adminUserId) throws SQLException {
        long startedAt = System.currentTimeMillis();
        int noticeId = noticeDAO.insertNotice(title, body, adminUserId);
        MulticastSender.broadcast("NEW_NOTICE:" + noticeId);
        RmiClientUtil.safeLogEvent(adminUserId, "POST_NOTICE");
        System.out.println("[" + RequestContext.getRequestId() + "] NoticeService.postNotice success in "
                + (System.currentTimeMillis() - startedAt) + "ms");
    }
}
