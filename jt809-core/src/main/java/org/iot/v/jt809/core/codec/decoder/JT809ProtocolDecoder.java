package org.iot.v.jt809.core.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.codec.EscapeHandler;
import org.iot.v.jt809.core.codec.MessageTypeRegistry;
import org.iot.v.jt809.core.constant.JT809Constant;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.base.MessageHead;
import org.iot.v.jt809.core.message.base.ProtocolVersion;
import org.iot.v.jt809.core.util.CRCUtil;

import java.time.Instant;
import java.util.List;

/**
 * JT809消息解码器
 * 基于Netty的ByteToMessageDecoder实现
 * 支持粘包拆包处理
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
public class JT809ProtocolDecoder extends ByteToMessageDecoder {

    private final EscapeHandler escapeHandler = new EscapeHandler();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        String rawData = ByteBufUtil.hexDump(in);
        try {

            // 消息长度字段的值包含自身，所以直接比较
            int actualLength = in.readableBytes();

            // 跳过起始标识
            in.skipBytes(1);

            // 读取消息长度并验证
            int declaredLength = in.getInt(in.readerIndex());


            // 消息长度字段的值包含自身，所以直接比较
            if (declaredLength != actualLength) {
                log.warn("Message length mismatch: declared={}, actual={}",
                        declaredLength, actualLength);
                ReferenceCountUtil.release(in);
                // 跳过结束标识
                in.skipBytes(1);
                return;
            }

            // CRC校验
            // -1 末尾标识符
            int totalLength = in.capacity();
            int messageDataLength = declaredLength - JT809Constant.CRC_LENGTH - 1;

            log.debug("CRC check: totalLength={}, declaredLength={}, messageDataLength={}",
                    totalLength, declaredLength, messageDataLength);

            // 对数据校验，去掉开始、结束标识
            int checkLen = declaredLength - 4;
            ByteBuf checkData = in.slice(1, checkLen);
            int calculatedCrc = CRCUtil.calculate(checkData);
            int receivedCrc = in.getUnsignedShort(messageDataLength);

            if (calculatedCrc != receivedCrc) {
                log.warn("CRC check failed: calculated=0x{}, received=0x{}",
                        Integer.toHexString(calculatedCrc).toUpperCase(),
                        Integer.toHexString(receivedCrc).toUpperCase());
                // 跳过数据
                in.skipBytes(in.readableBytes());
                return;
            }

            // 9. 解码消息头
            MessageHead head = decodeHead(in);

            // 10. 根据消息ID创建对应的消息对象
            BaseMessage message = MessageTypeRegistry.createMessage(head.getMsgId());
            message.setHead(head);

            // 11. 解码消息体
            int bodyLength = declaredLength - JT809Constant.MESSAGE_HEAD_LENGTH - JT809Constant.CRC_LENGTH;
            if (bodyLength > 0) {
                byte[] bodyBytes = new byte[bodyLength];
                in.readBytes(bodyBytes);
                message.getBody().decode(bodyBytes);
            }

//            in.skipBytes(in.readableBytes());

            // 12. 添加到输出列表
            out.add(message);

            // 13. 跳过结束标识
//            in.skipBytes(1);


            log.debug("Decoded message: msgId=0x{}, msgSn={}",
                    Integer.toHexString(head.getMsgId()), head.getMsgSn());

        } catch (Exception e) {
            log.error("Decode message failed,raw data: {}", rawData, e);
        }
    }

    /**
     * 查找起始标识
     *
     * @param buf ByteBuf
     * @return 起始标识位置，-1表示未找到
     */
    private int findStartFlag(ByteBuf buf) {
        for (int i = buf.readerIndex(); i < buf.writerIndex(); i++) {
            if (buf.getByte(i) == JT809Constant.FLAG_START) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 查找结束标识
     *
     * @param buf ByteBuf
     * @return 结束标识位置，-1表示未找到
     */
    private int findEndFlag(ByteBuf buf) {
        for (int i = buf.readerIndex(); i < buf.writerIndex(); i++) {
            if (buf.getByte(i) == JT809Constant.FLAG_END) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 解码消息头
     *
     * @param buf ByteBuf
     * @return MessageHead
     */
    private MessageHead decodeHead(ByteBuf buf) {
        MessageHead head = new MessageHead();

        // 消息长度（已读取，回退读取）
        head.setMsgLength(buf.readInt());

        // 消息流水号
        head.setMsgSn(buf.readInt());

        // 消息ID
        head.setMsgId(buf.readUnsignedShort());

        // 下级平台接入码 (BCD码，4字节)
        head.setPlatformId(buf.readUnsignedInt());

        // 协议版本
        head.setVersion(new ProtocolVersion(buf.readByte(), buf.readByte()));
        // 调过修正版本号
        buf.readByte();
        // 上级平台接入码
//        head.setSuperPlatformId(buf.readUnsignedInt());

        // 加密方式
        head.setEncrypt(buf.readByte());

        // 加密密钥
        head.setEncryptKey(buf.readUnsignedInt());

        // 读取时间
        head.setTime(Instant.ofEpochSecond(buf.readLong()));

        return head;
    }
}
