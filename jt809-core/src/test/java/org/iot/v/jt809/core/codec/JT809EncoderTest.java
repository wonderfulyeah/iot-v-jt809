package org.iot.v.jt809.core.codec;

import com.alibaba.fastjson2.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.iot.v.jt809.core.constant.JT809Constant;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.MessageHead;
import org.iot.v.jt809.core.message.base.ProtocolVersion;
import org.iot.v.jt809.core.message.upstream.UpConnectReq;
import org.iot.v.jt809.core.message.upstream.UpConnectResp;
import org.iot.v.jt809.core.message.upstream.UpLinkTestReq;
import org.iot.v.jt809.core.message.upstream.UpLinkTestResp;
import org.iot.v.jt809.core.util.CRCUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JT809Encoder 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("JT809编码器测试")
class JT809EncoderTest {

    private JT809Encoder encoder;
    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        encoder = new JT809Encoder();
        channel = new EmbeddedChannel(encoder);
    }

    @Test
    @DisplayName("编码UpConnectReq消息")
    void testEncodeUpConnectReq() {
        UpConnectReq msg = new UpConnectReq();
        MessageHead head = msg.getHead();
        head.setMsgSn(1);
        head.setPlatformId(1000001L);
        head.setVersion(new ProtocolVersion((byte) 1, (byte) 0, (byte) 0));
        head.setEncrypt((byte) 0);
        head.setEncryptKey(0L);

        UpConnectReq.Body body = (UpConnectReq.Body) msg.getBody();
        body.setUserId(1000001L);
        body.setPassword("12345678");
        body.setDownLinkIp("192.168.1.100");
        body.setDownLinkPort(9000);

        channel.writeOutbound(msg);
        channel.finish();

        ByteBuf encoded = channel.readOutbound();
        assertNotNull(encoded, "编码后的数据不应为空");

        // 验证编码后的消息长度至少是最小长度
        assertTrue(encoded.readableBytes() >= JT809Constant.MIN_MESSAGE_LENGTH,
                "编码后的消息长度应大于等于最小消息长度");

        encoded.release();
    }

    @Test
    @DisplayName("编码UpLinkTestReq消息-无消息体")
    void testEncodeUpLinkTestReq() {
        UpLinkTestReq msg = new UpLinkTestReq();
        MessageHead head = msg.getHead();
        head.setMsgSn(1);
        head.setPlatformId(1000001L);
        head.setVersion(new ProtocolVersion((byte) 1, (byte) 0, (byte) 0));
        head.setEncrypt((byte) 0);
        head.setEncryptKey(0L);

        channel.writeOutbound(msg);
        channel.finish();

        ByteBuf encoded = channel.readOutbound();
        assertNotNull(encoded);

        // 验证消息头
        encoded.readByte(); // 跳过起始标识
        int msgLength = encoded.readInt();
        assertTrue(msgLength >= 24); // 至少是消息头 + CRC

        encoded.release();
    }

    @Test
    @DisplayName("编码包含转义字符的消息")
    void testEncodeWithEscapeChar() {
        UpConnectReq msg = new UpConnectReq();
        MessageHead head = msg.getHead();
        head.setPlatformId(1000001L);
        head.setVersion(new ProtocolVersion((byte) 1, (byte) 0, (byte) 0));
        head.setEncrypt((byte) 0);
        head.setEncryptKey(0L);

        UpConnectReq.Body body = (UpConnectReq.Body) msg.getBody();
        body.setUserId(0x5B5B5B5BL); // 包含0x5B
        body.setPassword("12345678");
        body.setDownLinkIp("192.168.1.100");
        body.setDownLinkPort(9000);

        channel.writeOutbound(msg);
        channel.finish();

        ByteBuf encoded = channel.readOutbound();
        assertNotNull(encoded);

        encoded.release();
    }

    @Test
    @DisplayName("验证消息结构完整性")
    void testMessageStructure() {
        UpLinkTestReq msg = new UpLinkTestReq();
        MessageHead head = msg.getHead();
        head.setMsgSn(12345);
        head.setPlatformId(12345678L);
        head.setVersion(new ProtocolVersion((byte) 1, (byte) 0, (byte) 0));
        head.setEncrypt((byte) 0);
        head.setEncryptKey(0L);

        channel.writeOutbound(msg);
        channel.finish();

        ByteBuf encoded = channel.readOutbound();
        assertNotNull(encoded);

        // 解析消息结构
        encoded.readByte(); // 跳过起始标识

        // 读取消息长度
        int msgLength = encoded.readInt();
        assertTrue(msgLength > 0);

        // 读取流水号
        int msgSn = encoded.readInt();
        assertEquals(12345, msgSn);

        // 读取消息ID
        int msgId = encoded.readUnsignedShort();
        assertEquals(MessageType.UP_LINK_TEST_REQ, msgId);

        encoded.release();
    }

    @Test
    @DisplayName("验证CRC校验")
    void testCRCValidation() {
        UpLinkTestReq msg = new UpLinkTestReq();
        MessageHead head = msg.getHead();
        head.setPlatformId(1000001L);
        head.setVersion(new ProtocolVersion((byte) 1, (byte) 0, (byte) 0));
        head.setEncrypt((byte) 0);
        head.setEncryptKey(0L);

        channel.writeOutbound(msg);
        channel.finish();

        ByteBuf encoded = channel.readOutbound();
        assertNotNull(encoded);

        // 跳过起始标识
        encoded.readByte();

        // 读取转义后的消息内容（不包含起始和结束标识）
        ByteBuf escapedContent = encoded.slice(encoded.readerIndex(), encoded.readableBytes() - 1);

        // 反转义
        EscapeHandler escapeHandler = new EscapeHandler();
        ByteBuf originalContent = escapeHandler.unescape(escapedContent);

        // 验证CRC
        int contentLength = originalContent.readableBytes();
        ByteBuf dataWithoutCrc = originalContent.slice(0, contentLength - 2);
        int calculatedCrc = CRCUtil.calculate(dataWithoutCrc);

        int receivedCrc = originalContent.getUnsignedShort(contentLength - 2);
        assertEquals(calculatedCrc, receivedCrc, "CRC校验应匹配");

        encoded.release();
        originalContent.release();
    }

    @Test
    @DisplayName("编码后消息长度正确")
    void testEncodedMessageLength() {
        UpConnectReq msg = new UpConnectReq();
        MessageHead head = msg.getHead();
        head.setPlatformId(1000001L);
        head.setVersion(new ProtocolVersion((byte) 1, (byte) 0, (byte) 0));
        head.setEncrypt((byte) 0);
        head.setEncryptKey(0L);

        UpConnectReq.Body body = (UpConnectReq.Body) msg.getBody();
        body.setUserId(1000001L);
        body.setPassword("12345678");
        body.setDownLinkIp("192.168.1.100");
        body.setDownLinkPort(9000);

        channel.writeOutbound(msg);
        channel.finish();

        ByteBuf encoded = channel.readOutbound();
        assertNotNull(encoded);

        // 总长度至少是最小消息长度
        assertTrue(encoded.readableBytes() >= JT809Constant.MIN_MESSAGE_LENGTH);

        encoded.release();
    }

    @Test
    @DisplayName("数据编码测试")
    void testEncodedUpLinkTestRespMessage() {

        UpConnectResp resp = new UpConnectResp();
        MessageHead head = new MessageHead();
        head.setMsgSn(46);
        head.setMsgId(4098);
        head.setPlatformId(0);
        head.setVersion(new ProtocolVersion((byte) 1, (byte) 0, (byte) 0));
        head.setEncrypt((byte) 0);
        head.setEncryptKey(0);
        head.setTime(Instant.now());

        resp.setHead(head);
        UpConnectResp.Body respBody = new UpConnectResp.Body();
        respBody.setResult((byte) 0);
        respBody.setVerifyCode(47);
        resp.setBody(respBody);
        System.out.printf("origin message:%s\n", JSON.toJSONString(resp));

        channel.writeOutbound(resp);
        channel.finish();

        ByteBuf encoded = channel.readOutbound();
        System.out.printf("encoded:%s\n", ByteBufUtil.hexDump(encoded));

        assertNotNull(encoded);

        // 总长度至少是最小消息长度
        assertTrue(encoded.readableBytes() >= JT809Constant.MIN_MESSAGE_LENGTH);

        encoded.release();
    }
}
