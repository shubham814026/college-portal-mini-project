package com.college.service;

import com.college.dao.AlertDAO;
import com.college.network.MulticastSender;
import com.college.utils.RequestContext;
import com.college.utils.RmiClientUtil;

public class AlertService {
    private final AlertDAO alertDAO = new AlertDAO();

    public void sendAlert(String alertMessage, int adminUserId) throws Exception {
        long startedAt = System.currentTimeMillis();
        try {
            MulticastSender.broadcast("ALERT:" + alertMessage);
            alertDAO.insertAlert(alertMessage, adminUserId);
            RmiClientUtil.safeLogEvent(adminUserId, "SEND_ALERT");
            System.out.println("[" + RequestContext.getRequestId() + "] AlertService.sendAlert success in "
                    + (System.currentTimeMillis() - startedAt) + "ms");
        } catch (Exception e) {
            System.err.println("[" + RequestContext.getRequestId() + "] AlertService.sendAlert failed: "
                    + e.getMessage());
            throw e;
        }
    }
}
