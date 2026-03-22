package com.college.utils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class ServletResponseUtil {
    private ServletResponseUtil() {
    }

    public static void sendJson(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    public static void forwardError(HttpServletRequest request, HttpServletResponse response,
                                    int status, String safeMessage)
            throws IOException, ServletException {
        response.setStatus(status);
        request.setAttribute("errorMessage", safeMessage);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/common/error.jsp");
        dispatcher.forward(request, response);
    }

    public static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
