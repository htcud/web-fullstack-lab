package com.httpcode.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JsonApiController jsonApiController;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/api/**") // 拦截所有 API
                .excludePathPatterns("/api/login"); // 排除登录接口
    }

    private class AuthInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            // 1. 放行 OPTIONS 预检请求（解决跨域 Network Error 的关键）
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

            String cookieToken = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("AUTH_TOKEN".equals(c.getName())) cookieToken = c.getValue();
                }
            }

            // --- XSRF 校验逻辑 ---
            // 只有非 GET 请求（改变数据的请求）才强制校验 XSRF
            //String method = request.getMethod();
            //if (!"GET".equalsIgnoreCase(method)) {
            //    String headerXsrfToken = request.getHeader("X-XSRF-TOKEN");

                // 校验：Cookie 里的值必须存在，且与 Header 里的值完全一致
            //    if (cookieToken == null || !cookieToken.equals(headerXsrfToken)) {
            //        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
            //        response.getWriter().write("XSRF Attack Detected!");
            //        return false;
            //    }
            //}
            String headerXsrfToken = request.getHeader("X-XSRF-TOKEN");

            // 校验：Cookie 里的值必须存在，且与 Header 里的值完全一致
            if (cookieToken == null || !cookieToken.equals(headerXsrfToken)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
                response.getWriter().write("XSRF Attack Detected!");
                return false;
            }

            if (jsonApiController.isValidToken(cookieToken)) {
                return true;
            }

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}