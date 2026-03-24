package org.iot.v.jt809.core.message.upstream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.base.MessageBody;

/**
 * 链路保持响应消息（心跳响应）（消息ID: 0x1006）
 * 上级平台响应下级平台的心跳请求
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpLinkTestResp extends BaseMessage {
    
    public UpLinkTestResp() {
        setMsgId(MessageType.UP_LINK_TEST_RESP);
        setBody(new Body());
    }
    
    @Override
    public String getMessageTypeName() {
        return "链路保持响应";
    }
    
    /**
     * 消息体
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Body extends MessageBody {
        
        /**
         * 结果
         * 0: 成功
         * 1: 其他
         */
        private byte result;
        
        @Override
        public byte[] encode() {
            return new byte[]{result};
        }
        
        @Override
        public void decode(byte[] data) {
            if (data != null && data.length > 0) {
                result = data[0];
            }
        }
    }
}
