package com.server.campushelpserver.config;

import com.server.campushelpserver.config.JwtConfig;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * WebSocket 拦截器
 * 用于在WebSocket连接时进行JWT认证
 */
@Component
public class WebSocketInterceptor implements ChannelInterceptor {
    
    @Autowired
    private JwtConfig jwtConfig;
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 从请求头获取token
            String token = getTokenFromHeaders(accessor);
            
            if (StringUtils.hasText(token) && jwtConfig.validateToken(token)) {
                try {
                    Claims claims = jwtConfig.getClaimsFromToken(token);
                    Long userId = ((Number) claims.get("userId")).longValue();
                    String role = (String) claims.get("role");
                    
                    if (userId != null) {
                        // 构建权限列表
                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        if (role != null) {
                            // 统一使用 ROLE_ 前缀的权限名称
                            String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                            authorities.add(new SimpleGrantedAuthority(roleWithPrefix));
                        }
                        
                        // 设置认证信息，使用用户ID作为Principal名称，以便convertAndSendToUser能够正确路由
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userId.toString(), null, authorities);
                        accessor.setUser(authentication);
                    }
                } catch (Exception e) {
                    // Token解析失败，拒绝连接
                    throw new RuntimeException("WebSocket认证失败", e);
                }
            } else {
                // Token无效，拒绝连接
                throw new RuntimeException("WebSocket认证失败：Token无效");
            }
        }
        
        return message;
    }
    
    /**
     * 从请求头获取Token
     */
    private String getTokenFromHeaders(StompHeaderAccessor accessor) {
        // 方式1：从Authorization头获取
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        
        // 方式2：从自定义头获取
        List<String> tokenHeaders = accessor.getNativeHeader("token");
        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
            return tokenHeaders.get(0);
        }
        
        return null;
    }
}

