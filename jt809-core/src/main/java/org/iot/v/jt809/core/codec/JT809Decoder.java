package org.iot.v.jt809.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.constant.JT809Constant;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.base.MessageHead;
import org.iot.v.jt809.core.message.base.ProtocolVersion;
import org.iot.v.jt809.core.util.BCDUtil;
import org.iot.v.jt809.core.util.CRCUtil;

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
public class JT809Decoder extends ByteToMessageDecoder {
    
    private final EscapeHandler escapeHandler = new EscapeHandler();
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            // 1. 检查可读字节数
            if (in.readableBytes() < JT809Constant.MIN_MESSAGE_LENGTH) {
                return;
            }
            
            // 2. 标记读取位置（用于半包处理）
            in.markReaderIndex();
            
            // 3. 查找消息起始标识 0x5B
            int startIndex = findStartFlag(in);
            if (startIndex == -1) {
                // 没有找到起始标识，丢弃无效数据
                in.skipBytes(in.readableBytes());
                return;
            }
            
            // 跳过起始标识之前的数据
            if (startIndex > in.readerIndex()) {
                in.skipBytes(startIndex - in.readerIndex());
            }
            
            // 跳过起始标识
            in.skipBytes(1);
            
            // 4. 查找消息结束标识 0x5D
            int endIndex = findEndFlag(in);
            if (endIndex == -1) {
                // 没有找到结束标识，可能是半包，重置读取位置等待更多数据
                in.resetReaderIndex();
                return;
            }
            
            // 5. 读取完整消息（去除起始和结束标识）
            int messageLength = endIndex - in.readerIndex();
            ByteBuf escapedMessage = in.readSlice(messageLength);
            
            // 6. 反转义处理
            ByteBuf originalMessage = escapeHandler.unescape(escapedMessage);
            
            // 7. 读取消息长度并验证
            int declaredLength = originalMessage.readInt();
            // 消息长度字段的值包含自身，所以直接比较
            int actualLength = originalMessage.readableBytes() + 4; // 加上已读的长度字段
            
            if (declaredLength != actualLength) {
                log.warn("Message length mismatch: declared={}, actual={}", 
                    declaredLength, actualLength);
                ReferenceCountUtil.release(originalMessage);
                // 跳过结束标识
                in.skipBytes(1);
                return;
            }
            
            // 8. CRC校验
            // 消息数据不包含CRC的最后2字节，但需要包含长度字段
            // originalMessage 的 readerIndex 已经移动了 4 字节（读取了长度字段）
            // 所以 readableBytes 不包含长度字段
            // 我们需要从位置 0 开始切片，长度为 readableBytes + 4 - 2
            int totalLength = originalMessage.capacity();
            int messageDataLength = declaredLength - JT809Constant.CRC_LENGTH;
            
            log.debug("CRC check: totalLength={}, declaredLength={}, messageDataLength={}", 
                totalLength, declaredLength, messageDataLength);
            
            ByteBuf messageData = originalMessage.slice(0, messageDataLength);
            int calculatedCrc = CRCUtil.calculate(messageData);
            int receivedCrc = originalMessage.getUnsignedShort(messageDataLength);
            
            if (calculatedCrc != receivedCrc) {
                log.warn("CRC check failed: calculated=0x{}, received=0x{}", 
                    Integer.toHexString(calculatedCrc).toUpperCase(),
                    Integer.toHexString(receivedCrc).toUpperCase());
                ReferenceCountUtil.release(originalMessage);
                // 跳过结束标识
                in.skipBytes(1);
                return;
            }
            
            // 9. 解码消息头
            MessageHead head = decodeHead(originalMessage);
            
            // 10. 根据消息ID创建对应的消息对象
            BaseMessage message = MessageTypeRegistry.createMessage(head.getMsgId());
            message.setHead(head);
            
            // 11. 解码消息体
            int bodyLength = declaredLength - JT809Constant.MESSAGE_HEAD_LENGTH - JT809Constant.CRC_LENGTH;
            if (bodyLength > 0) {
                byte[] bodyBytes = new byte[bodyLength];
                originalMessage.readBytes(bodyBytes);
                message.getBody().decode(bodyBytes);
            }
            
            // 12. 添加到输出列表
            out.add(message);
            
            // 13. 跳过结束标识
            in.skipBytes(1);
            
            // 14. 释放临时ByteBuf
            ReferenceCountUtil.release(originalMessage);
            
            log.debug("Decoded message: msgId=0x{}, msgSn={}", 
                Integer.toHexString(head.getMsgId()), head.getMsgSn());
            
        } catch (Exception e) {
            log.error("Decode message failed", e);
            // 异常时跳过当前字节，继续尝试解析
            in.skipBytes(1);
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
        head.setMsgLength(buf.getInt(buf.readerIndex() - 4));
        
        // 消息流水号
        head.setMsgSn(buf.readInt());
        
        // 消息ID
        head.setMsgId(buf.readUnsignedShort());
        
        // 下级平台接入码 (BCD码，8字节)
        head.setPlatformId(BCDUtil.readLong(buf, 8));
        
        // 协议版本
        head.setVersion(new ProtocolVersion(buf.readByte(), buf.readByte()));
        
        // 上级平台接入码
        head.setSuperPlatformId(buf.readUnsignedInt());
        
        // 加密方式
        head.setEncrypt(buf.readByte());
        
        // 加密密钥
        head.setEncryptKey(buf.readUnsignedInt());
        
        return head;
    }
}
