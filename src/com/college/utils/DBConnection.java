package com.college.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private static final String URL = resolveConfig(AppConstants.DB_URL_ENV, "college.db.url",
            "jdbc:mysql://localhost:3306/college_db?useSSL=false&serverTimezone=UTC");
    private static final String USER = resolveConfig(AppConstants.DB_USER_ENV, "college.db.user", "root");
    private static final String PASS = resolveConfig(AppConstants.DB_PASS_ENV, "college.db.pass", "");

    private DBConnection() {
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException firstError) {
            // Local MariaDB installs often use root without a password.
            if (PASS != null && !PASS.isEmpty()) {
                try {
                    return DriverManager.getConnection(URL, USER, "");
                } catch (SQLException ignored) {
                    // Throw original exception to preserve the primary root cause.
                }
            }
            throw firstError;
        }
    }

    private static String resolveConfig(String envKey, String propKey, String defaultValue) {
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }

        String propValue = System.getProperty(propKey);
        if (propValue != null && !propValue.trim().isEmpty()) {
            return propValue.trim();
        }

        return defaultValue;
    }
}
