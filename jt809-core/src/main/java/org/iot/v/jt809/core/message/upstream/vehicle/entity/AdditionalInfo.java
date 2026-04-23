package org.iot.v.jt809.core.message.upstream.vehicle.entity;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;

/**
 * 附加信息
 */
@Data
public class AdditionalInfo {

    /**
     * 附加信息ID（1字节）
     */
    private int infoId;

    /**
     * 附加信息长度（2字节）
     */
    private int infoLength;

    /**
     * 附加信息内容
     */
    private byte[] infoContent;

    public int getInfoLength() {
        return infoContent != null ? infoContent.length : 0;
    }

    public byte[] encode() {
        int length = 1 + 2 + (infoContent != null ? infoContent.length : 0);
        ByteBuf buf = Unpooled.buffer(length);

        // 附加信息ID (1字节)
        buf.writeByte(infoId);

        // 附加信息长度 (2字节)
        buf.writeShort(infoContent != null ? infoContent.length : 0);

        // 附加信息内容
        if (infoContent != null && infoContent.length > 0) {
            buf.writeBytes(infoContent);
        }

        byte[] result = new byte[buf.readableBytes()];
        buf.readBytes(result);
        buf.release();

        return result;
    }

    /**
     * 从ByteBuf解码附加信息
     *
     * @param buf ByteBuf
     * @return true-成功解码, false-数据不完整
     */
    public boolean decode(ByteBuf buf) {
        if (!buf.isReadable(3)) {
            return false;
        }

        // 附加信息ID (1字节)
        infoId = buf.readUnsignedByte();

        // 附加信息长度 (2字节)
        infoLength = buf.readUnsignedByte();

        // 附加信息内容
        if (infoLength > 0) {
            if (!buf.isReadable(infoLength)) {
                return false;
            }
            infoContent = new byte[infoLength];
            buf.readBytes(infoContent);
        } else {
            infoContent = new byte[0];
        }

        return true;
    }
}
