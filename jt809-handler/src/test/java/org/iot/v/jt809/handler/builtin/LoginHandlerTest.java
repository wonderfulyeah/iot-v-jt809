package org.iot.v.jt809.handler.builtin;

import io.netty.channel.embedded.EmbeddedChannel;
import org.iot.v.jt809.core.constant.JT809Constant;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.upstream.UpConnectReq;
import org.iot.v.jt809.core.session.Session;
import org.iot.v.jt809.core.session.SessionManager;
import org.iot.v.jt809.handler.context.MessageContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LoginHandler 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("登录处理器测试")
class LoginHandlerTest {

    private LoginHandler handler;

    @BeforeEach
    void setUp() {
        handler = new LoginHandler();
        SessionManager.getInstance().clear();
    }

    @Test
    @DisplayName("支持的消息类型")
    void testSupportedMessageTypes() {
        int[] types = handler.supportedMessageTypes();
        
        assertEquals(2, types.length);
        assertTrue(contains(types, MessageType.UP_CONNECT_REQ));
        assertTrue(contains(types, MessageType.DOWN_CONNECT_REQ));
    }

    @Test
    @DisplayName("处理器名称")
    void testGetName() {
        assertEquals("LoginHandler", handler.getName());
    }

    @Test
    @DisplayName("处理器优先级")
    void testGetOrder() {
        assertEquals(Integer.MIN_VALUE + 1, handler.getOrder());
    }

    @Test
    @DisplayName("处理登录请求-默认成功")
    void testHandleLoginRequestDefaultSuccess() {
        EmbeddedChannel channel = new EmbeddedChannel();
        Session session = new Session(channel);
        
        UpConnectReq request = new UpConnectReq();
        UpConnectReq.Body body = (UpConnectReq.Body) request.getBody();
        body.setUserId(12345L);
        body.setPassword("password");
        body.setDownLinkIp("192.168.1.100");
        body.setDownLinkPort(9000);
        
        MessageContext context = new MessageContext(session, request);
        
        boolean result = handler.handle(context, request);
        
        // 登录处理器应该终止处理链
        assertFalse(result);
        
        // 验证会话状态
        assertTrue(session.isAuthenticated());
        assertEquals(12345L, session.getPlatformId());
        
        // 验证响应消息被写入channel
        Object outbound = channel.readOutbound();
        assertNotNull(outbound);
        
        channel.finish();
    }

    @Test
    @DisplayName("处理登录请求-自定义认证器成功")
    void testHandleLoginRequestWithAuthenticatorSuccess() {
        handler.setAuthenticator(req -> 0x00); // 总是返回成功
        
        EmbeddedChannel channel = new EmbeddedChannel();
        Session session = new Session(channel);
        
        UpConnectReq request = new UpConnectReq();
        UpConnectReq.Body body = (UpConnectReq.Body) request.getBody();
        body.setUserId(12345L);
        
        MessageContext context = new MessageContext(session, request);
        
        boolean result = handler.handle(context, request);
        
        assertFalse(result);
        assertTrue(session.isAuthenticated());
        
        channel.finish();
    }

    @Test
    @DisplayName("处理登录请求-自定义认证器失败")
    void testHandleLoginRequestWithAuthenticatorFailure() {
        handler.setAuthenticator(req -> JT809Constant.LOGIN_FAIL_PASSWORD);
        
        EmbeddedChannel channel = new EmbeddedChannel();
        Session session = new Session(channel);
        
        UpConnectReq request = new UpConnectReq();
        UpConnectReq.Body body = (UpConnectReq.Body) request.getBody();
        body.setUserId(12345L);
        
        MessageContext context = new MessageContext(session, request);
        
        boolean result = handler.handle(context, request);
        
        assertFalse(result);
        assertFalse(session.isAuthenticated());
        
        channel.finish();
    }

    @Test
    @DisplayName("处理登录请求-无会话")
    void testHandleLoginRequestWithoutSession() {
        UpConnectReq request = new UpConnectReq();
        UpConnectReq.Body body = (UpConnectReq.Body) request.getBody();
        body.setUserId(12345L);
        
        MessageContext context = new MessageContext(null, request);
        
        // 不应抛出异常
        boolean result = handler.handle(context, request);
        
        assertFalse(result);
    }

    private boolean contains(int[] arr, int value) {
        for (int v : arr) {
            if (v == value) return true;
        }
        return false;
    }
}
