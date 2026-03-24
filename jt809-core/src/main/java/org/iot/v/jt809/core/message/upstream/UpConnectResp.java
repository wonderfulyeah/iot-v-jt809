package org.iot.v.jt809.core.message.upstream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.base.MessageBody;

/**
 * 上行连接响应消息（消息ID: 0x1002）
 * 上级平台响应下级平台的连接请求
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpConnectResp extends BaseMessage {
    
    public UpConnectResp() {
        setMsgId(MessageType.UP_CONNECT_RESP);
        setBody(new Body());
    }
    
    @Override
    public String getMessageTypeName() {
        return "上行连接响应";
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
         * 1: IP地址不正确
         * 2: 接入码不正确
         * 3: 用户不存在
         * 4: 密码错误
         * 5: 其他
         */
        private byte result;
        
        /**
         * 校验码（4字节）
         * 用于后续通信验证
         */
        private long verifyCode;
        
        @Override
        public byte[] encode() {
            ByteBuf buf = Unpooled.buffer(5);
            
            // 结果 (1字节)
            buf.writeByte(result);
            
            // 校验码 (4字节)
            buf.writeInt((int) verifyCode);
            
            byte[] result = new byte[buf.readableBytes()];
            buf.readBytes(result);
            buf.release();
            
            return result;
        }
        
        @Override
        public void decode(byte[] data) {
            ByteBuf buf = Unpooled.wrappedBuffer(data);
            
            // 结果 (1字节)
            result = buf.readByte();
            
            // 校验码 (4字节)
            verifyCode = buf.readUnsignedInt();
            
            buf.release();
        }
    }
}
