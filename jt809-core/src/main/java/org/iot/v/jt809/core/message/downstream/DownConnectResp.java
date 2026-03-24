package org.iot.v.jt809.core.message.downstream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.base.MessageBody;

/**
 * 下行连接响应消息（消息ID: 0x9002）
 * 下级平台响应上级平台的下行连接请求
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DownConnectResp extends BaseMessage {
    
    public DownConnectResp() {
        setMsgId(MessageType.DOWN_CONNECT_RESP);
        setBody(new Body());
    }
    
    @Override
    public String getMessageTypeName() {
        return "下行连接响应";
    }
    
    /**
     * 消息体
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Body extends MessageBody {
        
        /**
         * 结果码（1字节）
         * 0x00: 成功
         * 0x01: 校验码错误
         * 0x02: 资源不足
         * 0x03: 其他原因
         */
        private int result;
        
        @Override
        public byte[] encode() {
            ByteBuf buf = Unpooled.buffer(1);
            buf.writeByte(result);
            
            byte[] resultBytes = new byte[buf.readableBytes()];
            buf.readBytes(resultBytes);
            buf.release();
            
            return resultBytes;
        }
        
        @Override
        public void decode(byte[] data) {
            ByteBuf buf = Unpooled.wrappedBuffer(data);
            result = buf.readUnsignedByte();
            buf.release();
        }
    }
}
