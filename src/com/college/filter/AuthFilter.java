package com.college.filter;

import com.college.utils.AppConstants;
import com.college.utils.JsonUtil;
import com.college.utils.RequestContext;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String requestId = UUID.randomUUID().toString();

        RequestContext.setRequestId(requestId);
        request.setAttribute("requestId", requestId);
        response.setHeader("X-Request-Id", requestId);

        try {
            String path = request.getServletPath();

            // Public paths — no auth required
            if ("/api/login".equals(path)
                || path.startsWith("/css")
                || path.startsWith("/js")
                || path.startsWith("/uploads")) {
                chain.doFilter(req, res);
                return;
            }

            // All /api/* paths require authentication
            if (path.startsWith("/api/")) {
                HttpSession session = request.getSession(false);
                if (session == null || session.getAttribute("userId") == null) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                            "Authentication required");
                    return;
                }

                // Admin-only paths
                if (path.startsWith("/api/logs") || path.startsWith("/api/admin")) {
                    String role = (String) session.getAttribute("role");
                    if (!AppConstants.ROLE_ADMIN.equals(role)) {
                        JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                                "Access denied");
                        return;
                    }
                }

                chain.doFilter(req, res);
                return;
            }

            // Everything else — pass through (static files, etc.)
            chain.doFilter(req, res);
        } finally {
            RequestContext.clear();
        }
    }

    @Override
    public void destroy() {
    }
}
