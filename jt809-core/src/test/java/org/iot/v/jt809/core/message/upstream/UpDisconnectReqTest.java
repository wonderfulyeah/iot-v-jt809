package org.iot.v.jt809.core.message.upstream;

import org.iot.v.jt809.core.constant.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UpDisconnectReq 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("上行断开请求消息测试")
class UpDisconnectReqTest {

    @Test
    @DisplayName("创建消息-默认值")
    void testCreateDefault() {
        UpDisconnectReq msg = new UpDisconnectReq();
        
        assertEquals(MessageType.UP_DISCONNECT_REQ, msg.getMsgId());
        assertEquals("上行断开请求", msg.getMessageTypeName());
        assertNotNull(msg.getBody());
    }

    @Test
    @DisplayName("消息体编码-空消息体")
    void testEncodeBody() {
        UpDisconnectReq msg = new UpDisconnectReq();
        UpDisconnectReq.Body body = (UpDisconnectReq.Body) msg.getBody();
        
        byte[] encoded = body.encode();
        
        assertNotNull(encoded);
        assertEquals(0, encoded.length, "断开请求消息体应为空");
    }

    @Test
    @DisplayName("消息体解码-空数据")
    void testDecodeBody() {
        UpDisconnectReq msg = new UpDisconnectReq();
        UpDisconnectReq.Body body = (UpDisconnectReq.Body) msg.getBody();
        
        // 解码空数据，不应抛出异常
        assertDoesNotThrow(() -> body.decode(new byte[0]));
        assertDoesNotThrow(() -> body.decode(null));
    }
}
