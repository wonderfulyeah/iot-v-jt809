package org.iot.v.jt809.core.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.embedded.EmbeddedChannel;
import org.iot.v.jt809.core.constant.SessionState;
import org.iot.v.jt809.core.message.upstream.UpLinkTestReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Session 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("会话测试")
class SessionTest {

    private Channel channel;
    private Session session;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel();
        session = new Session(channel);
    }

    @Test
    @DisplayName("创建会话-默认值")
    void testCreateDefault() {
        Session s = new Session();
        
        // 无参构造时sessionId为null，直到设置channel
        assertEquals(SessionState.CONNECTED, s.getState());
        assertNotNull(s.getLastActiveTime());
        assertNotNull(s.getAttributes());
    }

    @Test
    @DisplayName("创建会话-带Channel")
    void testCreateWithChannel() {
        assertNotNull(session.getSessionId());
        assertEquals(channel.id().asLongText(), session.getSessionId());
        assertEquals(SessionState.CONNECTED, session.getState());
        assertEquals(channel, session.getChannel());
    }

    @Test
    @DisplayName("发送消息")
    void testSendMessage() {
        UpLinkTestReq msg = new UpLinkTestReq();
        
        // 会话应该能够发送消息（不抛出异常）
        assertDoesNotThrow(() -> session.send(msg));
    }

    @Test
    @DisplayName("关闭会话")
    void testCloseSession() {
        session.close();
        
        assertEquals(SessionState.DISCONNECTED, session.getState());
    }

    @Test
    @DisplayName("更新最后活跃时间")
    void testUpdateLastActiveTime() throws InterruptedException {
        Instant before = session.getLastActiveTime();
        
        Thread.sleep(10);
        session.updateLastActiveTime();
        
        Instant after = session.getLastActiveTime();
        assertTrue(after.isAfter(before));
    }

    @Test
    @DisplayName("检查会话是否活跃")
    void testIsActive() {
        assertTrue(session.isActive());
        
        session.close();
        assertFalse(session.isActive());
    }

    @Test
    @DisplayName("设置和获取属性")
    void testAttribute() {
        session.setAttribute("key1", "value1");
        session.setAttribute("key2", 12345);
        
        assertEquals("value1", session.getAttribute("key1"));
        assertEquals(12345, session.getAttribute("key2"));
        assertNull(session.getAttribute("key3"));
    }

    @Test
    @DisplayName("移除属性")
    void testRemoveAttribute() {
        session.setAttribute("key1", "value1");
        session.removeAttribute("key1");
        
        assertNull(session.getAttribute("key1"));
    }

    @Test
    @DisplayName("设置和获取平台ID")
    void testPlatformId() {
        session.setPlatformId(12345678L);
        
        assertEquals(12345678L, session.getPlatformId());
    }

    @Test
    @DisplayName("设置和获取会话状态")
    void testState() {
        session.setState(SessionState.AUTHENTICATED);
        
        assertEquals(SessionState.AUTHENTICATED, session.getState());
    }

    @Test
    @DisplayName("设置和获取登录时间")
    void testLoginTime() {
        Instant loginTime = Instant.now();
        session.setLoginTime(loginTime);
        
        assertEquals(loginTime, session.getLoginTime());
    }

    @Test
    @DisplayName("获取远程地址")
    void testGetRemoteAddress() {
        String address = session.getRemoteAddress();
        
        // EmbeddedChannel的remoteAddress可能为null
        assertNotNull(address);
    }

    @Test
    @DisplayName("设置和获取校验码")
    void testVerifyCode() {
        session.setVerifyCode(12345678L);
        
        assertEquals(12345678L, session.getVerifyCode());
    }

    @Test
    @DisplayName("设置和获取认证状态")
    void testAuthenticated() {
        session.setAuthenticated(true);
        
        assertTrue(session.isAuthenticated());
    }

    @Test
    @DisplayName("错误计数操作")
    void testErrorCount() {
        assertEquals(0, session.getErrorCount());
        
        session.incrementErrorCount();
        session.incrementErrorCount();
        assertEquals(2, session.getErrorCount());
        
        session.resetErrorCount();
        assertEquals(0, session.getErrorCount());
    }
}
