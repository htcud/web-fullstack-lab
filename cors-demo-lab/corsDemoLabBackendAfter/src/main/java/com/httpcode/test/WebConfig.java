package com.httpcode.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Web 配置类
 * 
 * 核心功能：
 * 1. 注册认证拦截器（检查登陆状态）
 * 2. CORS 由 @CrossOrigin 注解在 Controller 处理，不在此处理
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JsonApiController jsonApiController;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/api/**")
                // 关键：完全排除登陆接口，不进行任何拦截
                .excludePathPatterns("/api/login");
    }

    /**
     * 认证拦截器
     * 
     * 职责：检查请求是否有有效的认证 token（Cookie）
     * 注意：不处理 CORS，由 @CrossOrigin 注解处理
     */
    private class AuthInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
                throws Exception {
            // 从 Cookies 中寻找 AUTH_TOKEN
            String token = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("AUTH_TOKEN".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }

            // 验证 Token
            if (token != null && jsonApiController.isValidToken(token)) {
                return true;  // 认证成功，继续请求
            }

            // 认证失败，返回 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("请先登录");
            return false;
        }
    }
}
