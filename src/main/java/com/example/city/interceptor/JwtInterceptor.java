//package com.example.city.interceptor;
//
//import com.example.city.Utils.JwtUtil;
//import io.jsonwebtoken.Claims;
//import jakarta.annotation.Resource;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import java.io.IOException;
//
//@Component
//public class JwtInterceptor implements HandlerInterceptor {
//
//    @Resource
//    private JwtUtil jwtUtil;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
//            return true;
//        }
//
//        String authorization = request.getHeader("Authorization");
//        if (authorization == null || !authorization.startsWith("Bearer ")) {
//            writeUnauthorized(response, "未登录或 token 缺失");
//            return false;
//        }
//
//        String token = authorization.substring(7);
//        try {
//            Claims claims = jwtUtil.parseToken(token);
//            request.setAttribute("claims", claims);
//            return true;
//        } catch (Exception e) {
//            writeUnauthorized(response, "token 无效或已过期");
//            return false;
//        }
//    }
//
//    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        response.setContentType("application/json;charset=UTF-8");
//        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
//    }
//}
