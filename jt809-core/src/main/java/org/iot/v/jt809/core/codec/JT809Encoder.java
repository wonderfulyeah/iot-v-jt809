package org.iot.v.jt809.core.codec;

import com.alibaba.fastjson2.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.constant.JT809Constant;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.base.MessageHead;
import org.iot.v.jt809.core.util.BCDUtil;

/**
 * JT809消息编码器
 * 基于Netty的MessageToByteEncoder实现
 * 使用ByteBuf实现零拷贝优化
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
public class JT809Encoder extends MessageToByteEncoder<BaseMessage> {

    private final EscapeHandler escapeHandler = new EscapeHandler();

    @Override
    protected void encode(ChannelHandlerContext ctx, BaseMessage msg, ByteBuf out) {
        ByteBuf bodyBuf = null;
        ByteBuf headBuf = null;
        ByteBuf messageBuf = null;
        ByteBuf escapedBuf = null;

        try {
            // 1. 编码消息体
            bodyBuf = encodeBody(msg);

            // 2. 编码消息头
            headBuf = encodeHead(msg.getHead(), bodyBuf.readableBytes());

            // 3. 计算CRC校验（消息头 + 消息体）
            int crc = calculateCrc(headBuf, bodyBuf);

            // 4. 组装完整消息（消息头 + 消息体 + CRC）
            messageBuf = Unpooled.buffer();
            messageBuf.writeBytes(headBuf);
            messageBuf.writeBytes(bodyBuf);
            messageBuf.writeShort(crc);

            // 5. 转义处理
            escapedBuf = escapeHandler.escape(messageBuf);

            // 6. 添加起始和结束标识
            out.writeByte(JT809Constant.FLAG_START);
            out.writeBytes(escapedBuf);
            out.writeByte(JT809Constant.FLAG_END);

            log.info("encode message: {} result: {}", JSON.toJSONString(msg), ByteBufUtil.hexDump(out));

            log.debug("Encoded message: msgId=0x{}, length={}",
                    Integer.toHexString(msg.getMsgId()), out.readableBytes());

        } catch (Exception e) {
            log.error("Encode message failed: msgId=0x{}",
                    Integer.toHexString(msg.getMsgId()), e);
            throw new RuntimeException("Encode message failed", e);
        } finally {
            // 安全释放ByteBuf
            if (bodyBuf != null && bodyBuf.refCnt() > 0) {
                ReferenceCountUtil.release(bodyBuf);
            }
            if (headBuf != null && headBuf.refCnt() > 0) {
                ReferenceCountUtil.release(headBuf);
            }
            if (messageBuf != null && messageBuf.refCnt() > 0) {
                ReferenceCountUtil.release(messageBuf);
            }
            if (escapedBuf != null && escapedBuf.refCnt() > 0) {
                ReferenceCountUtil.release(escapedBuf);
            }
        }
    }

    /**
     * 计算CRC校验码
     */
    private int calculateCrc(ByteBuf headBuf, ByteBuf bodyBuf) {
        int crc = 0xFFFF;

        // 计算消息头的CRC
        int headReaderIndex = headBuf.readerIndex();
        for (int i = headReaderIndex; i < headBuf.writerIndex(); i++) {
            byte b = headBuf.getByte(i);
            crc ^= (b & 0xFF) << 8;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
            }
        }

        // 计算消息体的CRC
        int bodyReaderIndex = bodyBuf.readerIndex();
        for (int i = bodyReaderIndex; i < bodyBuf.writerIndex(); i++) {
            byte b = bodyBuf.getByte(i);
            crc ^= (b & 0xFF) << 8;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
            }
        }

        return crc & 0xFFFF;
    }

    /**
     * 编码消息体
     *
     * @param msg 消息对象
     * @return ByteBuf
     */
    private ByteBuf encodeBody(BaseMessage msg) {
        if (msg.getBody() == null) {
            return Unpooled.EMPTY_BUFFER;
        }

        byte[] bodyBytes = msg.getBody().encode();
        return Unpooled.wrappedBuffer(bodyBytes);
    }

    /**
     * 编码消息头
     *
     * @param head       消息头
     * @param bodyLength 消息体长度
     * @return ByteBuf
     */
    private ByteBuf encodeHead(MessageHead head, int bodyLength) {
        // 消息头总长度：消息长度(4) + 其他字段(25) = 29字节
        ByteBuf buf = Unpooled.buffer(29);

        // 消息长度 = 消息头(29) + 消息体长度 + CRC(2) +开始/结束标识（2）
        // 注意：JT809协议中，消息长度字段的值包含自身
        int msgLength = JT809Constant.MESSAGE_HEAD_LENGTH + bodyLength + JT809Constant.CRC_LENGTH +2;
        buf.writeInt(msgLength);

        // 消息流水号
        buf.writeInt(head.getMsgSn());

        // 消息ID
        buf.writeShort(head.getMsgId());

        // 下级平台接入码 (BCD码，8字节)
        BCDUtil.writeLong(buf, head.getPlatformId(), 8);

        // 协议版本
        buf.writeByte(head.getVersion().getMajor());
        buf.writeByte(head.getVersion().getMinor());

        // 上级平台接入码
        buf.writeInt((int) head.getSuperPlatformId());

        // 加密方式
        buf.writeByte(head.getEncrypt());

        // 加密密钥
        buf.writeInt((int) head.getEncryptKey());

        return buf;
    }
}
