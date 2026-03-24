package org.iot.v.jt809.core.message.downstream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.base.MessageBody;

/**
 * 下行链路测试请求消息（消息ID: 0x9005）
 * 上级平台发送心跳请求
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DownLinkTestReq extends BaseMessage {
    
    public DownLinkTestReq() {
        setMsgId(MessageType.DOWN_LINK_TEST_REQ);
        setBody(new Body());
    }
    
    @Override
    public String getMessageTypeName() {
        return "下行链路测试请求";
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
