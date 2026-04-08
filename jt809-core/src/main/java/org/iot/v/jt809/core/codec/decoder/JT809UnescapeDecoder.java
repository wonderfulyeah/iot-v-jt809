package org.iot.v.jt809.core.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.iot.v.jt809.core.constant.JT809Constant;

import java.util.List;

/**
 * JT809协议反转义解码器
 * 将转义后的数据还原为原始数据
 * <p>
 * 转义规则：
 * 解码时：
 * - 0x5A 0x01 -> 0x5B
 * - 0x5A 0x02 -> 0x5A
 * - 0x5E 0x01 -> 0x5D
 * - 0x5E 0x02 -> 0x5E
 *
 * @author haye
 * @date 4/7/26 6:09 PM
 * <p>
 * Keep It Simple, Stupid
 */
public class JT809UnescapeDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        ByteBuf unescaped = unescape(msg);
        out.add(unescaped);
    }

    /**
     * 反转义处理
     *
     * @param source 转义后的数据
     * @return 原始数据
     */
    private ByteBuf unescape(ByteBuf source) {
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
}
