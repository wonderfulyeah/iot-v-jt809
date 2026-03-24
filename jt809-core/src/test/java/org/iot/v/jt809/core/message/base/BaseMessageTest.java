package org.iot.v.jt809.core.message.base;

import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.upstream.UpConnectReq;
import org.iot.v.jt809.core.message.upstream.UpLinkTestReq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BaseMessage 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("消息基类测试")
class BaseMessageTest {

    @Test
    @DisplayName("创建消息时自动生成流水号")
    void testAutoGenerateSequenceNumber() {
        UpLinkTestReq msg1 = new UpLinkTestReq();
        UpLinkTestReq msg2 = new UpLinkTestReq();
        
        assertNotNull(msg1.getHead());
        assertNotNull(msg1.getHead().getMsgSn());
        assertNotEquals(msg1.getMsgSn(), msg2.getMsgSn(), "不同消息的流水号应不同");
    }

    @Test
    @DisplayName("获取消息ID")
    void testGetMsgId() {
        UpLinkTestReq msg = new UpLinkTestReq();
        
        assertEquals(MessageType.UP_LINK_TEST_REQ, msg.getMsgId());
    }

    @Test
    @DisplayName("设置消息ID")
    void testSetMsgId() {
        UpLinkTestReq msg = new UpLinkTestReq();
        msg.setMsgId(0x1234);
        
        assertEquals(0x1234, msg.getMsgId());
        assertEquals(0x1234, msg.getHead().getMsgId());
    }

    @Test
    @DisplayName("获取消息流水号")
    void testGetMsgSn() {
        UpLinkTestReq msg = new UpLinkTestReq();
        
        assertEquals(msg.getHead().getMsgSn(), msg.getMsgSn());
    }

    @Test
    @DisplayName("消息体初始化")
    void testBodyInitialization() {
        UpConnectReq msg = new UpConnectReq();
        
        assertNotNull(msg.getBody());
        assertTrue(msg.getBody() instanceof UpConnectReq.Body);
    }
}
