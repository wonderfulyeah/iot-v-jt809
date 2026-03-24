package org.iot.v.jt809.handler.builtin;

import io.netty.channel.embedded.EmbeddedChannel;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.upstream.UpLinkTestReq;
import org.iot.v.jt809.core.session.Session;
import org.iot.v.jt809.handler.context.MessageContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HeartbeatHandler 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("心跳处理器测试")
class HeartbeatHandlerTest {

    private HeartbeatHandler handler;

    @BeforeEach
    void setUp() {
        handler = new HeartbeatHandler();
    }

    @Test
    @DisplayName("支持的消息类型")
    void testSupportedMessageTypes() {
        int[] types = handler.supportedMessageTypes();
        
        assertEquals(2, types.length);
        assertTrue(contains(types, MessageType.UP_LINK_TEST_REQ));
        assertTrue(contains(types, MessageType.DOWN_LINK_TEST_REQ));
    }

    @Test
    @DisplayName("处理器名称")
    void testGetName() {
        assertEquals("HeartbeatHandler", handler.getName());
    }

    @Test
    @DisplayName("处理器优先级")
    void testGetOrder() {
        assertEquals(Integer.MIN_VALUE, handler.getOrder());
    }

    @Test
    @DisplayName("处理上行心跳请求")
    void testHandleUpLinkTestReq() {
        EmbeddedChannel channel = new EmbeddedChannel();
        Session session = new Session(channel);
        
        MessageContext context = new MessageContext(session, new UpLinkTestReq());
        UpLinkTestReq request = new UpLinkTestReq();
        
        boolean result = handler.handle(context, request);
        
        assertTrue(result);
        
        // 验证响应消息被写入channel
        Object outbound = channel.readOutbound();
        assertNotNull(outbound);
        
        channel.finish();
    }

    @Test
    @DisplayName("处理无会话的心跳请求")
    void testHandleWithoutSession() {
        MessageContext context = new MessageContext();
        UpLinkTestReq request = new UpLinkTestReq();
        
        // 不应抛出异常
        boolean result = handler.handle(context, request);
        
        assertTrue(result);
    }

    @Test
    @DisplayName("会话最后活动时间更新")
    void testSessionLastActiveTimeUpdate() throws InterruptedException {
        EmbeddedChannel channel = new EmbeddedChannel();
        Session session = new Session(channel);
        long before = session.getLastActiveTime().toEpochMilli();
        
        Thread.sleep(10);
        
        MessageContext context = new MessageContext(session, new UpLinkTestReq());
        handler.handle(context, new UpLinkTestReq());
        
        long after = session.getLastActiveTime().toEpochMilli();
        assertTrue(after > before);
        
        channel.finish();
    }

    private boolean contains(int[] arr, int value) {
        for (int v : arr) {
            if (v == value) return true;
        }
        return false;
    }
}
