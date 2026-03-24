package org.iot.v.jt809.core.message.downstream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.base.MessageBody;
import org.iot.v.jt809.core.util.ByteBufUtil;

/**
 * 下行连接请求消息（消息ID: 0x9001）
 * 上级平台向下级平台发起连接请求
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DownConnectReq extends BaseMessage {
    
    public DownConnectReq() {
        setMsgId(MessageType.DOWN_CONNECT_REQ);
        setBody(new Body());
    }
    
    @Override
    public String getMessageTypeName() {
        return "下行连接请求";
    }
    
    /**
     * 消息体
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Body extends MessageBody {
        
        /**
         * 校验码（4字节）
         */
        private long verifyCode;
        
        @Override
        public byte[] encode() {
            ByteBuf buf = Unpooled.buffer(4);
            buf.writeInt((int) verifyCode);
            
            byte[] result = new byte[buf.readableBytes()];
            buf.readBytes(result);
            buf.release();
            
            return result;
        }
        
        @Override
        public void decode(byte[] data) {
            ByteBuf buf = Unpooled.wrappedBuffer(data);
            verifyCode = buf.readUnsignedInt();
            buf.release();
        }
    }
}
