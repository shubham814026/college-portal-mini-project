package com.college.filter;

import com.college.utils.AppConstants;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@WebFilter("/*")
public class AuthFilter implements Filter {
    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
            "/login", "/login.jsp", "/error", "/"
    ));

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

            if (PUBLIC_PATHS.contains(path)
                || path.startsWith("/css")
                || path.startsWith("/js")
                || path.startsWith("/common")
                || path.startsWith("/uploads")) {
                chain.doFilter(req, res);
                return;
            }

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                response.sendRedirect(request.getContextPath() + "/login?timeout=1");
                return;
            }

            if (path.startsWith("/admin")) {
                String role = (String) session.getAttribute("role");
                if (!AppConstants.ROLE_ADMIN.equals(role)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
                    return;
                }
            }

            chain.doFilter(req, res);
        } finally {
            RequestContext.clear();
        }
    }

    @Override
    public void destroy() {
    }
}
