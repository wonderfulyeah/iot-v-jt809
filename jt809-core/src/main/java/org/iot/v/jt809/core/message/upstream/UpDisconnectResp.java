package org.iot.v.jt809.core.message.upstream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.base.MessageBody;

/**
 * 上行断开响应消息（消息ID: 0x1004）
 * 上级平台响应断开连接请求
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpDisconnectResp extends BaseMessage {
    
    public UpDisconnectResp() {
        setMsgId(MessageType.UP_DISCONNECT_RESP);
        setBody(new Body());
    }
    
    @Override
    public String getMessageTypeName() {
        return "上行断开响应";
    }
    
    /**
     * 消息体（空）
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Body extends MessageBody {
        
        @Override
        public byte[] encode() {
            return new byte[0];
        }
        
        @Override
        public void decode(byte[] data) {
            // 空消息体
        }
    }
}
