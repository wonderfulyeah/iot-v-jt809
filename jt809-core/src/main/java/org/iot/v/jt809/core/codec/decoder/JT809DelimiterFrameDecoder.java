package org.iot.v.jt809.core.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import org.iot.v.jt809.core.constant.JT809Constant;

/**
 * JT809 协议分隔符帧解码器
 * 基于 0x5D 结束标识切分原始字节流，用于解决 TCP 粘包/拆包问题
 *
 * @author haye
 * @date 4/7/26 5:06 PM
 * <p>
 * Keep It Simple, Stupid
 */
public class JT809DelimiterFrameDecoder extends DelimiterBasedFrameDecoder {

    /**
     * 默认最大帧长度 (64KB)
     */
    private static final int DEFAULT_MAX_FRAME_LENGTH = 64 * 1024;

    /**
     * 结束分隔符 0x5D
     */
    private static final ByteBuf END_DELIMITER = Unpooled.wrappedBuffer(new byte[]{JT809Constant.FLAG_END});

    /**
     * 使用默认最大帧长度创建解码器
     */
    public JT809DelimiterFrameDecoder() {
        super(DEFAULT_MAX_FRAME_LENGTH, true, END_DELIMITER);
    }

    /**
     * 指定最大帧长度创建解码器
     *
     * @param maxFrameLength 最大帧长度，防止内存溢出攻击
     */
    public JT809DelimiterFrameDecoder(int maxFrameLength) {
        super(maxFrameLength, true, END_DELIMITER);
    }
}
