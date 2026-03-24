package org.iot.v.jt809.core.message.upstream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UpConnectReq 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("上行连接请求消息测试")
class UpConnectReqTest {

    @Test
    @DisplayName("创建消息")
    void testCreateMessage() {
        UpConnectReq msg = new UpConnectReq();
        
        assertNotNull(msg.getHead());
        assertNotNull(msg.getBody());
        assertEquals("上行连接请求", msg.getMessageTypeName());
    }

    @Test
    @DisplayName("编解码往返测试")
    void testEncodeDecodeRoundTrip() {
        UpConnectReq original = new UpConnectReq();
        original.getHead().setMsgSn(12345);
        original.getHead().setPlatformId(1000001L);
        
        UpConnectReq.Body body = (UpConnectReq.Body) original.getBody();
        body.setUserId(1000001L);
        body.setPassword("12345678");
        body.setDownLinkIp("192.168.1.100");
        body.setDownLinkPort(9000);
        body.setDownLinkName("TestPlatform");
        
        // 编码
        byte[] encoded = body.encode();
        // 消息体长度：userId(4) + password(8) + IP(32) + port(4) + name(64) = 112字节
        assertTrue(encoded.length > 0, "消息体编码后应有数据");
        
        // 解码
        UpConnectReq decoded = new UpConnectReq();
        UpConnectReq.Body decodedBody = (UpConnectReq.Body) decoded.getBody();
        decodedBody.decode(encoded);
        
        // 验证
        assertEquals(body.getUserId(), decodedBody.getUserId());
        assertEquals(body.getPassword().trim(), decodedBody.getPassword().trim());
        assertEquals(body.getDownLinkIp().trim(), decodedBody.getDownLinkIp().trim());
        assertEquals(body.getDownLinkPort(), decodedBody.getDownLinkPort());
    }

    @Test
    @DisplayName("消息体编码-完整数据")
    void testEncodeBodyFullData() {
        UpConnectReq msg = new UpConnectReq();
        UpConnectReq.Body body = (UpConnectReq.Body) msg.getBody();
        
        body.setUserId(12345678L);
        body.setPassword("abcdefgh");
        body.setDownLinkIp("10.20.30.40");
        body.setDownLinkPort(8888);
        body.setDownLinkName("PlatformName");
        
        byte[] encoded = body.encode();
        
        // 消息体长度：userId(4) + password(8) + IP(32) + port(4) + name(64) = 112字节
        assertTrue(encoded.length > 0, "编码后应有数据");
        
        // 使用ByteBuf解析验证
        ByteBuf buf = Unpooled.wrappedBuffer(encoded);
        
        long userId = buf.readUnsignedInt();
        assertEquals(12345678L, userId);
        
        byte[] passwordBytes = new byte[8];
        buf.readBytes(passwordBytes);
        assertTrue(new String(passwordBytes).startsWith("abcdefgh"));
        
        buf.release();
    }

    @Test
    @DisplayName("消息体解码-完整数据")
    void testDecodeBodyFullData() {
        ByteBuf buf = Unpooled.buffer(108);
        
        // 构造消息体数据
        buf.writeInt(12345678);           // userId
        buf.writeBytes("abcdefgh".getBytes()); // password (8字节)
        buf.writeZero(8 - "abcdefgh".length());
        buf.writeBytes("10.20.30.40".getBytes()); // IP (32字节)
        buf.writeZero(32 - "10.20.30.40".length());
        buf.writeInt(8888);               // port
        buf.writeBytes("PlatformName".getBytes()); // name (64字节)
        buf.writeZero(64 - "PlatformName".length());
        
        byte[] data = new byte[108];
        buf.resetReaderIndex();
        buf.readBytes(data);
        
        UpConnectReq msg = new UpConnectReq();
        UpConnectReq.Body body = (UpConnectReq.Body) msg.getBody();
        body.decode(data);
        
        assertEquals(12345678L, body.getUserId());
        assertTrue(body.getPassword().contains("abcdefgh"));
        assertTrue(body.getDownLinkIp().contains("10.20.30.40"));
        assertEquals(8888, body.getDownLinkPort());
        
        buf.release();
    }

    @Test
    @DisplayName("消息体编码-null字段")
    void testEncodeBodyWithNullFields() {
        UpConnectReq msg = new UpConnectReq();
        UpConnectReq.Body body = (UpConnectReq.Body) msg.getBody();
        
        body.setUserId(12345L);
        body.setPassword(null);
        body.setDownLinkIp(null);
        body.setDownLinkPort(0);
        body.setDownLinkName(null);
        
        byte[] encoded = body.encode();
        
        // 编码后应该有固定长度的数据
        assertTrue(encoded.length > 0);
    }

    @Test
    @DisplayName("消息体解码-部分数据")
    void testDecodeBodyPartialData() {
        // 构造完整的消息体数据（112字节）
        ByteBuf buf = Unpooled.buffer(112);
        
        buf.writeInt(12345678);           // userId (4字节)
        buf.writeBytes("pass1234".getBytes()); // password (8字节)
        buf.writeZero(8 - "pass1234".length());
        buf.writeBytes("192.168.1.1".getBytes()); // IP (32字节)
        buf.writeZero(32 - "192.168.1.1".length());
        buf.writeInt(9000);               // port (4字节)
        buf.writeZero(64);                // name (64字节，填充零)
        
        byte[] data = new byte[112];
        buf.resetReaderIndex();
        buf.readBytes(data);
        
        UpConnectReq msg = new UpConnectReq();
        UpConnectReq.Body body = (UpConnectReq.Body) msg.getBody();
        body.decode(data);
        
        assertEquals(12345678L, body.getUserId());
        assertEquals(9000, body.getDownLinkPort());
        
        buf.release();
    }
}
