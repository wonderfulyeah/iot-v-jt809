package org.iot.v.jt809.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.iot.v.jt809.core.constant.JT809Constant;

/**
 * JT809消息转义处理器
 * 
 * 转义规则：
 * 编码时：
 * - 0x5B -> 0x5A 0x01
 * - 0x5A -> 0x5A 0x02
 * - 0x5D -> 0x5E 0x01
 * - 0x5E -> 0x5E 0x02
 * 
 * 解码时：
 * - 0x5A 0x01 -> 0x5B
 * - 0x5A 0x02 -> 0x5A
 * - 0x5E 0x01 -> 0x5D
 * - 0x5E 0x02 -> 0x5E
 *
 * @author haye
 * @date 2026-03-24
 */
public class EscapeHandler {
    
    /**
     * 转义处理（编码时使用）
     * 
     * @param source 原始数据
     * @return 转义后的数据
     */
    public ByteBuf escape(ByteBuf source) {
        if (source == null || !source.isReadable()) {
            return Unpooled.EMPTY_BUFFER;
        }
        
        // 预估转义后的大小（最多扩大一倍）
        ByteBuf target = Unpooled.buffer(source.readableBytes() * 2);
        
        int readerIndex = source.readerIndex();
        int writerIndex = source.writerIndex();
        
        for (int i = readerIndex; i < writerIndex; i++) {
            byte b = source.getByte(i);
            
            switch (b) {
                case JT809Constant.FLAG_START: // 0x5B
                    target.writeByte(JT809Constant.ESCAPE_CHAR);  // 0x5A
                    target.writeByte(0x01);
                    break;
                    
                case JT809Constant.ESCAPE_CHAR: // 0x5A
                    target.writeByte(JT809Constant.ESCAPE_CHAR);  // 0x5A
                    target.writeByte(0x02);
                    break;
                    
                case JT809Constant.FLAG_END: // 0x5D
                    target.writeByte(JT809Constant.ESCAPE_CHAR_2);  // 0x5E
                    target.writeByte(0x01);
                    break;
                    
                case JT809Constant.ESCAPE_CHAR_2: // 0x5E
                    target.writeByte(JT809Constant.ESCAPE_CHAR_2);  // 0x5E
                    target.writeByte(0x02);
                    break;
                    
                default:
                    target.writeByte(b);
                    break;
            }
        }
        
        return target;
    }
    
    /**
     * 反转义处理（解码时使用）
     * 
     * @param source 转义后的数据
     * @return 原始数据
     */
    public ByteBuf unescape(ByteBuf source) {
        if (source == null || !source.isReadable()) {
            return Unpooled.EMPTY_BUFFER;
        }
        
        ByteBuf target = Unpooled.buffer(source.readableBytes());
        
        int readerIndex = source.readerIndex();
        int writerIndex = source.writerIndex();
        
        int i = readerIndex;
        while (i < writerIndex) {
            byte b = source.getByte(i);
            
            if (b == JT809Constant.ESCAPE_CHAR || b == JT809Constant.ESCAPE_CHAR_2) {
                // 检查是否有下一个字节
                if (i + 1 < writerIndex) {
                    byte next = source.getByte(i + 1);
                    
                    if (b == JT809Constant.ESCAPE_CHAR) { // 0x5A
                        if (next == 0x01) {
                            target.writeByte(JT809Constant.FLAG_START); // 0x5B
                            i += 2;
                        } else if (next == 0x02) {
                            target.writeByte(JT809Constant.ESCAPE_CHAR); // 0x5A
                            i += 2;
                        } else {
                            // 无效的转义序列，保留原字节
                            target.writeByte(b);
                            i++;
                        }
                    } else { // 0x5E
                        if (next == 0x01) {
                            target.writeByte(JT809Constant.FLAG_END); // 0x5D
                            i += 2;
                        } else if (next == 0x02) {
                            target.writeByte(JT809Constant.ESCAPE_CHAR_2); // 0x5E
                            i += 2;
                        } else {
                            // 无效的转义序列，保留原字节
                            target.writeByte(b);
                            i++;
                        }
                    }
                } else {
                    // 最后一个字节是转义字符，保留
                    target.writeByte(b);
                    i++;
                }
            } else {
                target.writeByte(b);
                i++;
            }
        }
        
        return target;
    }
    
    /**
     * 转义处理（字节数组版本）
     * 
     * @param source 原始数据
     * @return 转义后的数据
     */
    public byte[] escape(byte[] source) {
        ByteBuf sourceBuf = Unpooled.wrappedBuffer(source);
        ByteBuf targetBuf = escape(sourceBuf);
        
        byte[] result = new byte[targetBuf.readableBytes()];
        targetBuf.readBytes(result);
        
        sourceBuf.release();
        targetBuf.release();
        
        return result;
    }
    
    /**
     * 反转义处理（字节数组版本）
     * 
     * @param source 转义后的数据
     * @return 原始数据
     */
    public byte[] unescape(byte[] source) {
        ByteBuf sourceBuf = Unpooled.wrappedBuffer(source);
        ByteBuf targetBuf = unescape(sourceBuf);
        
        byte[] result = new byte[targetBuf.readableBytes()];
        targetBuf.readBytes(result);
        
        sourceBuf.release();
        targetBuf.release();
        
        return result;
    }
}
