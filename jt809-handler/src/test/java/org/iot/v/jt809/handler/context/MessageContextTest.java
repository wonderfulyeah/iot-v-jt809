package org.iot.v.jt809.handler.context;

import io.netty.channel.embedded.EmbeddedChannel;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.upstream.UpLinkTestReq;
import org.iot.v.jt809.core.session.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MessageContext 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("消息上下文测试")
class MessageContextTest {

    private MessageContext context;

    @BeforeEach
    void setUp() {
        context = new MessageContext();
    }

    @Test
    @DisplayName("创建默认上下文")
    void testCreateDefault() {
        assertNotNull(context.getAttributes());
        assertTrue(context.getStartTime() > 0);
    }

    @Test
    @DisplayName("创建带会话和请求的上下文")
    void testCreateWithSessionAndRequest() {
        EmbeddedChannel channel = new EmbeddedChannel();
        Session session = new Session(channel);
        BaseMessage request = new UpLinkTestReq();
        
        MessageContext ctx = new MessageContext(session, request);
        
        assertEquals(session, ctx.getSession());
        assertEquals(request, ctx.getRequest());
    }

    @Test
    @DisplayName("设置和获取属性")
    void testAttribute() {
        context.setAttribute("key1", "value1");
        context.setAttribute("key2", 123);
        
        assertEquals("value1", context.<String>getAttribute("key1"));
        assertEquals(123, context.<Integer>getAttribute("key2"));
        assertNull(context.getAttribute("key3"));
    }

    @Test
    @DisplayName("移除属性")
    void testRemoveAttribute() {
        context.setAttribute("key1", "value1");
        context.removeAttribute("key1");
        
        assertNull(context.getAttribute("key1"));
    }

    @Test
    @DisplayName("获取处理耗时")
    void testGetElapsed() throws InterruptedException {
        long elapsed = context.getElapsed();
        assertTrue(elapsed >= 0);
        
        Thread.sleep(10);
        
        long elapsedAfter = context.getElapsed();
        assertTrue(elapsedAfter >= 10);
    }

    @Test
    @DisplayName("获取消息ID")
    void testGetMsgId() {
        assertEquals(0, context.getMsgId());
        
        UpLinkTestReq request = new UpLinkTestReq();
        context.setRequest(request);
        
        assertEquals(0x1005, context.getMsgId());
    }

    @Test
    @DisplayName("获取平台ID")
    void testGetPlatformId() {
        assertEquals(0, context.getPlatformId());
        
        EmbeddedChannel channel = new EmbeddedChannel();
        Session session = new Session(channel);
        session.setPlatformId(12345L);
        context.setSession(session);
        
        assertEquals(12345L, context.getPlatformId());
    }

    @Test
    @DisplayName("检查异常状态")
    void testHasException() {
        assertFalse(context.hasException());
        
        context.setException(new RuntimeException("Test"));
        
        assertTrue(context.hasException());
    }

    @Test
    @DisplayName("设置响应")
    void testSetResponse() {
        BaseMessage response = new UpLinkTestReq();
        context.setResponse(response);
        
        assertEquals(response, context.getResponse());
    }
}
