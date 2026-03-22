package com.college.utils;

public final class AppConstants {
    private AppConstants() {
    }

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_STUDENT = "STUDENT";

    public static final int SESSION_TIMEOUT_SECONDS = 30 * 60;
    public static final int TCP_CONNECT_TIMEOUT_MS = 2000;
    public static final int TCP_READ_TIMEOUT_MS = 1500;

    public static final String RMI_URL = "rmi://localhost/CollegeService";
    public static final int TCP_CHAT_PORT = 9100;
    public static final int TCP_CHAT_STATUS_PORT = 9101;
    public static final String TCP_CHAT_HOST = "localhost";

    public static final String MULTICAST_GROUP = "230.0.0.1";
    public static final int MULTICAST_PORT = 8888;

    public static final String DB_URL_ENV = "COLLEGE_DB_URL";
    public static final String DB_USER_ENV = "COLLEGE_DB_USER";
    public static final String DB_PASS_ENV = "COLLEGE_DB_PASS";
}
