package org.iot.v.jt809.core.message.upstream;

import org.iot.v.jt809.core.constant.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UpLinkTestReq 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("链路保持请求消息测试")
class UpLinkTestReqTest {

    @Test
    @DisplayName("创建消息-默认值")
    void testCreateDefault() {
        UpLinkTestReq msg = new UpLinkTestReq();
        
        assertEquals(MessageType.UP_LINK_TEST_REQ, msg.getMsgId());
        assertEquals("链路保持请求", msg.getMessageTypeName());
        assertNotNull(msg.getBody());
    }

    @Test
    @DisplayName("消息体编码-空消息体")
    void testEncodeBody() {
        UpLinkTestReq msg = new UpLinkTestReq();
        UpLinkTestReq.Body body = (UpLinkTestReq.Body) msg.getBody();
        
        byte[] encoded = body.encode();
        
        assertNotNull(encoded);
        assertEquals(0, encoded.length, "心跳请求消息体应为空");
    }

    @Test
    @DisplayName("消息体解码-空数据")
    void testDecodeBody() {
        UpLinkTestReq msg = new UpLinkTestReq();
        UpLinkTestReq.Body body = (UpLinkTestReq.Body) msg.getBody();
        
        // 解码空数据，不应抛出异常
        assertDoesNotThrow(() -> body.decode(new byte[0]));
        assertDoesNotThrow(() -> body.decode(null));
    }
}
