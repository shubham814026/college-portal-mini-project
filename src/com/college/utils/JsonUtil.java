package com.college.utils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Lightweight JSON utility — no external dependency.
 */
public final class JsonUtil {
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private JsonUtil() {
    }

    /* ---------- response helpers ---------- */

    public static void sendSuccess(HttpServletResponse resp, String json) throws IOException {
        send(resp, HttpServletResponse.SC_OK, json);
    }

    public static void sendCreated(HttpServletResponse resp, String json) throws IOException {
        send(resp, 201, json);
    }

    public static void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        send(resp, status, "{\"error\":" + str(message) + "}");
    }

    public static void send(HttpServletResponse resp, int status, String json) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json);
    }

    /* ---------- value helpers ---------- */

    public static String str(String v) {
        return v == null ? "null" : "\"" + escape(v) + "\"";
    }

    public static String num(long v) {
        return Long.toString(v);
    }

    public static String num(int v) {
        return Integer.toString(v);
    }

    public static String bool(boolean v) {
        return v ? "true" : "false";
    }

    public static String date(LocalDateTime v) {
        return v == null ? "null" : "\"" + v.format(ISO) + "\"";
    }

    public static String nullVal() {
        return "null";
    }

    /* ---------- object / array builders ---------- */

    public static String object(String... kvPairs) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < kvPairs.length; i += 2) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escape(kvPairs[i])).append("\":");
            sb.append(kvPairs[i + 1]); // value already formatted
        }
        sb.append("}");
        return sb.toString();
    }

    public static String array(List<String> jsonItems) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < jsonItems.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(jsonItems.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String array(String... jsonItems) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < jsonItems.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(jsonItems[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /* ---------- escape ---------- */

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /* ---------- body reading ---------- */

    public static String readBody(javax.servlet.http.HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * Very simple JSON value extraction — works for flat objects only.
     * Returns null if key not found.
     */
    public static String extractString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        idx = json.indexOf(":", idx + search.length());
        if (idx < 0) return null;
        idx++;
        while (idx < json.length() && json.charAt(idx) == ' ') idx++;
        if (idx >= json.length()) return null;
        if (json.charAt(idx) == 'n') return null; // null
        if (json.charAt(idx) == '"') {
            int end = json.indexOf('"', idx + 1);
            if (end < 0) return null;
            return json.substring(idx + 1, end).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        // number or boolean
        int end = idx;
        while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
        return json.substring(idx, end).trim();
    }

    public static Integer extractInt(String json, String key) {
        String val = extractString(json, key);
        if (val == null) return null;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
