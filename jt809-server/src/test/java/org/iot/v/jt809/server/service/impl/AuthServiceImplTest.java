package org.iot.v.jt809.server.service.impl;

import org.iot.v.jt809.core.message.upstream.UpConnectReq;
import org.iot.v.jt809.server.config.ServerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthServiceImpl 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("认证服务测试")
class AuthServiceImplTest {

    private AuthServiceImpl authService;
    private ServerProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ServerProperties();
        
        List<ServerProperties.PlatformConfig> platforms = new ArrayList<>();
        
        ServerProperties.PlatformConfig config1 = new ServerProperties.PlatformConfig();
        config1.setPlatformId(12345678L);
        config1.setPassword("password123");
        config1.setEnabled(true);
        
        ServerProperties.PlatformConfig config2 = new ServerProperties.PlatformConfig();
        config2.setPlatformId(87654321L);
        config2.setPassword("password456");
        config2.setEnabled(true);
        
        platforms.add(config1);
        platforms.add(config2);
        properties.setPlatforms(platforms);
        
        authService = new AuthServiceImpl(properties);
    }

    @Test
    @DisplayName("认证成功")
    void testAuthenticateSuccess() {
        UpConnectReq req = createConnectReq(12345678L, "password123");
        
        int result = authService.authenticate(req);
        
        assertEquals(0x00, result, "认证成功应返回0x00");
    }

    @Test
    @DisplayName("认证失败-错误密码")
    void testAuthenticateWrongPassword() {
        UpConnectReq req = createConnectReq(12345678L, "wrongPassword");
        
        int result = authService.authenticate(req);
        
        assertEquals(0x02, result, "密码错误应返回0x02");
    }

    @Test
    @DisplayName("认证失败-未授权平台")
    void testAuthenticateUnauthorizedPlatform() {
        UpConnectReq req = createConnectReq(99999999L, "anyPassword");
        
        int result = authService.authenticate(req);
        
        assertEquals(0x02, result, "未授权平台应返回0x02");
    }

    @Test
    @DisplayName("认证失败-空请求")
    void testAuthenticateNullRequest() {
        int result = authService.authenticate(null);
        
        assertEquals(0x03, result, "空请求应返回0x03");
    }

    @Test
    @DisplayName("检查平台是否授权")
    void testIsPlatformAuthorized() {
        assertTrue(authService.isPlatformAuthorized(12345678L));
        assertTrue(authService.isPlatformAuthorized(87654321L));
        assertFalse(authService.isPlatformAuthorized(99999999L));
    }

    @Test
    @DisplayName("获取平台密码")
    void testGetPlatformPassword() {
        assertEquals("password123", authService.getPlatformPassword(12345678L));
        assertEquals("password456", authService.getPlatformPassword(87654321L));
        assertNull(authService.getPlatformPassword(99999999L));
    }

    /**
     * 创建连接请求
     */
    private UpConnectReq createConnectReq(long userId, String password) {
        UpConnectReq req = new UpConnectReq();
        UpConnectReq.Body body = (UpConnectReq.Body) req.getBody();
        body.setUserId(userId);
        body.setPassword(password);
        return req;
    }
}
