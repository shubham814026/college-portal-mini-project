package com.college.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class ValidationUtil {
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
            Arrays.asList("pdf", "docx", "pptx", "zip")
    );

    private ValidationUtil() {
    }

    public static boolean isBlank(String input) {
        return input == null || input.trim().isEmpty();
    }

    public static boolean isAllowedFileExtension(String fileName) {
        if (isBlank(fileName) || !fileName.contains(".")) {
            return false;
        }
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }
}
