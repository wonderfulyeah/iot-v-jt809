package org.iot.v.jt809.core.message.upstream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.base.MessageBody;
import org.iot.v.jt809.core.util.ByteBufUtil;

/**
 * 上行连接请求消息（消息ID: 0x1001）
 * 下级平台向上级平台发起连接请求
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpConnectReq extends BaseMessage {
    
    public UpConnectReq() {
        setMsgId(MessageType.UP_CONNECT_REQ);
        setBody(new Body());
    }
    
    @Override
    public String getMessageTypeName() {
        return "上行连接请求";
    }
    
    /**
     * 消息体
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Body extends MessageBody {
        
        /**
         * 用户ID（4字节）
         */
        private long userId;
        
        /**
         * 密码（8字节）
         */
        private String password;
        
        /**
         * 下级平台IP地址（32字节）
         */
        private String downLinkIp;
        
        /**
         * 下级平台端口（4字节）
         */
        private int downLinkPort;
        
        /**
         * 下级平台单位名称（64字节，可选）
         */
        private String downLinkName;
        
        @Override
        public byte[] encode() {
            ByteBuf buf = Unpooled.buffer(108);
            
            // 用户ID (4字节)
            buf.writeInt((int) userId);
            
            // 密码 (8字节)
            ByteBufUtil.writeString(buf, password, 8);
            
            // 下级平台IP地址 (32字节)
            ByteBufUtil.writeString(buf, downLinkIp, 32);
            
            // 下级平台端口 (4字节)
            buf.writeInt(downLinkPort);
            
            // 下级平台单位名称 (64字节)
            if (downLinkName != null) {
                ByteBufUtil.writeString(buf, downLinkName, 64);
            } else {
                buf.writeZero(64);
            }
            
            byte[] result = new byte[buf.readableBytes()];
            buf.readBytes(result);
            buf.release();
            
            return result;
        }
        
        @Override
        public void decode(byte[] data) {
            ByteBuf buf = Unpooled.wrappedBuffer(data);
            
            // 用户ID (4字节)
            userId = buf.readUnsignedInt();
            
            // 密码 (8字节)
            password = ByteBufUtil.readString(buf, 8);
            
            // 下级平台IP地址 (32字节)
            downLinkIp = ByteBufUtil.readString(buf, 32);
            
            // 下级平台端口 (4字节)
            downLinkPort = buf.readInt();
            
            // 下级平台单位名称 (64字节)
            if (buf.isReadable(64)) {
                downLinkName = ByteBufUtil.readString(buf, 64);
            }
            
            buf.release();
        }
    }
}
