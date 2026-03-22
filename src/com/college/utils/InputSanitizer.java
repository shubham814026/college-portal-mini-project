package com.college.utils;

public final class InputSanitizer {
    private InputSanitizer() {
    }

    public static String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isEmpty() ? null : normalized;
    }

    public static String safeFileName(String fileName) {
        if (fileName == null) {
            return null;
        }
        String normalized = fileName.replace("\\", "/");
        int idx = normalized.lastIndexOf('/');
        if (idx >= 0) {
            normalized = normalized.substring(idx + 1);
        }
        return normalizeText(normalized);
    }
}
