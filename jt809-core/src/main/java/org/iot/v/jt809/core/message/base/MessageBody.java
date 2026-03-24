package org.iot.v.jt809.core.message.base;

import io.netty.buffer.ByteBuf;

/**
 * 消息体基类
 * 所有具体消息体都需要继承此类并实现encode和decode方法
 *
 * @author haye
 * @date 2026-03-24
 */
public abstract class MessageBody {
    
    /**
     * 编码消息体为字节数组
     *
     * @return 编码后的字节数组
     */
    public abstract byte[] encode();
    
    /**
     * 从字节数组解码消息体
     *
     * @param data 字节数组
     */
    public abstract void decode(byte[] data);
    
    /**
     * 编码消息体到ByteBuf
     * 默认实现：调用encode()方法转换为字节数组后写入ByteBuf
     * 子类可以重写此方法以提高性能
     *
     * @param buf ByteBuf
     */
    public void encodeToBuf(ByteBuf buf) {
        byte[] data = encode();
        buf.writeBytes(data);
    }
    
    /**
     * 从ByteBuf解码消息体
     * 默认实现：从ByteBuf读取字节数组后调用decode()方法
     * 子类可以重写此方法以提高性能
     *
     * @param buf ByteBuf
     * @param length 消息体长度
     */
    public void decodeFromBuf(ByteBuf buf, int length) {
        byte[] data = new byte[length];
        buf.readBytes(data);
        decode(data);
    }
}
