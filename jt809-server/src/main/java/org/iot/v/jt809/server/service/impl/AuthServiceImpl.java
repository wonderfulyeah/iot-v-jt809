package org.iot.v.jt809.server.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.message.upstream.UpConnectReq;
import org.iot.v.jt809.server.config.ServerProperties;
import org.iot.v.jt809.server.service.AuthService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证服务实现
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
public class AuthServiceImpl implements AuthService {
    
    /**
     * 平台配置映射 (platformId -> password)
     */
    private final Map<Long, String> platformPasswords = new HashMap<>();
    
    public AuthServiceImpl(ServerProperties properties) {
        // 初始化平台配置
        List<ServerProperties.PlatformConfig> platforms = properties.getPlatforms();
        if (platforms != null) {
            for (ServerProperties.PlatformConfig config : platforms) {
                platformPasswords.put(config.getPlatformId(), config.getPassword());
            }
        }
        log.info("AuthService initialized with {} platforms", platformPasswords.size());
    }
    
    @Override
    public int authenticate(UpConnectReq connectReq) {
        if (connectReq == null || connectReq.getBody() == null) {
            log.warn("Invalid connect request: null");
            return 0x03; // 其他原因
        }
        
        UpConnectReq.Body body = (UpConnectReq.Body) connectReq.getBody();
        long userId = body.getUserId();
        String password = body.getPassword();
        
        // 检查平台是否授权
        if (!isPlatformAuthorized(userId)) {
            log.warn("Platform not authorized: {}", userId);
            return 0x02; // 用户名/密码错误
        }
        
        // 验证密码
        String expectedPassword = getPlatformPassword(userId);
        if (expectedPassword == null || !expectedPassword.equals(password)) {
            log.warn("Invalid password for platform: {}", userId);
            return 0x02; // 用户名/密码错误
        }
        
        log.info("Platform authenticated successfully: {}", userId);
        return 0x00; // 成功
    }
    
    @Override
    public boolean isPlatformAuthorized(long platformId) {
        return platformPasswords.containsKey(platformId);
    }
    
    @Override
    public String getPlatformPassword(long platformId) {
        return platformPasswords.get(platformId);
    }
}
