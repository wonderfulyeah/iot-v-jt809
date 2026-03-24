package org.iot.v.jt809.core.message.upstream;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.base.MessageBody;

/**
 * 链路保持请求消息（心跳）（消息ID: 0x1005）
 * 下级平台定时发送心跳保持链路
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpLinkTestReq extends BaseMessage {
    
    public UpLinkTestReq() {
        setMsgId(MessageType.UP_LINK_TEST_REQ);
        setBody(new Body());
    }
    
    @Override
    public String getMessageTypeName() {
        return "链路保持请求";
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
