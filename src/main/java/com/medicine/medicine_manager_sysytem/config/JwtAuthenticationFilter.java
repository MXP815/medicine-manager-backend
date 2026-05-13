package com.medicine.medicine_manager_sysytem.config;

import com.medicine.medicine_manager_sysytem.entity.User;
import com.medicine.medicine_manager_sysytem.mapper.UserMapper;
import com.medicine.medicine_manager_sysytem.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        
        log.info("🔐 JWT Filter - 请求URL: {}, Token存在: {}", 
                request.getRequestURI(), token != null ? "是" : "否");

        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            
            log.info("✅ JWT Token 解析成功 - userId: {}, username: {}", userId, username);
            
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            String role = jwtUtil.getUserRoleFromToken(token);
            if (role != null && !role.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                log.info("🎭 用户角色: {}", role);
            }

            User user = userMapper.selectById(userId);
            if (user != null) {
                log.info("👤 从数据库加载用户信息 - ID: {}, 用户名: {}, 角色: {}", 
                        user.getId(), user.getUsername(), user.getRole());
                
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("✅ 认证信息设置成功");
            } else {
                log.warn("⚠️ 用户不存在 - userId: {}, username: {}", userId, username);
            }
        } else {
            if (token != null) {
                log.warn("⚠️ Token 验证失败");
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
