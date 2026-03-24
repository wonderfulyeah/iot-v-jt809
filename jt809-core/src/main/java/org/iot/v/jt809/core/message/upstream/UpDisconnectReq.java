package org.iot.v.jt809.core.message.upstream;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.base.MessageBody;

/**
 * 上行断开请求消息（消息ID: 0x1003）
 * 下级平台主动断开连接
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpDisconnectReq extends BaseMessage {
    
    public UpDisconnectReq() {
        setMsgId(MessageType.UP_DISCONNECT_REQ);
        setBody(new Body());
    }
    
    @Override
    public String getMessageTypeName() {
        return "上行断开请求";
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
            // 空消息体，无需处理
        }
    }
}
