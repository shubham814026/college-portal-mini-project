package com.college.utils;

import com.college.rmi.CollegeService;

import java.rmi.Naming;
import java.sql.Timestamp;

public final class RmiClientUtil {
    private RmiClientUtil() {
    }

    public static CollegeService getService() throws Exception {
        return (CollegeService) Naming.lookup(AppConstants.RMI_URL);
    }

    public static void safeLogEvent(Integer userId, String action) {
        if (userId == null || action == null || action.trim().isEmpty()) {
            return;
        }

        try {
            CollegeService service = getService();
            service.logEvent(userId, action, new Timestamp(System.currentTimeMillis()));
        } catch (Exception e) {
            System.err.println("[" + RequestContext.getRequestId() + "] RMI logEvent skipped for action "
                    + action + ": " + e.getMessage());
        }
    }
}
