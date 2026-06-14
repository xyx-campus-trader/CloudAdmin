package com.mate.admin.system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mate.admin.api.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 管理员权限拦截器：校验 Gateway 透传的 X-User-Role 头部。
 * 仅拦截写操作（POST/PUT/DELETE），读操作放行。
 */
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String role = request.getHeader("X-User-Role");
        if (!"admin".equals(role)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    mapper.writeValueAsString(Result.fail(403, "无操作权限，仅管理员可执行此操作")));
            return false;
        }
        return true;
    }
}
