package org.iot.v.jt809.core.message.downstream.vehicle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.base.MessageBody;
import org.iot.v.jt809.core.util.ByteBufUtil;

/**
 * 车辆控制请求消息（消息ID: 0x9301）
 * 上级平台发送车辆控制指令
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleControlReq extends BaseMessage {
    
    public VehicleControlReq() {
        setMsgId(MessageType.VEHICLE_CONTROL);
        setBody(new Body());
    }
    
    @Override
    public String getMessageTypeName() {
        return "车辆控制请求";
    }
    
    /**
     * 消息体
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Body extends MessageBody {
        
        /**
         * 车牌号码（21字节）
         */
        private String vehicleNo;
        
        /**
         * 车牌颜色（1字节）
         */
        private int vehicleColor;
        
        /**
         * 子业务类型标识（2字节）
         */
        private int subBusinessType;
        
        /**
         * 后续数据长度（4字节）
         */
        private int subsequentDataLength;
        
        /**
         * 控制参数（变长）
         */
        private byte[] controlParams;
        
        @Override
        public byte[] encode() {
            int totalLength = 28 + (controlParams != null ? controlParams.length : 0);
            ByteBuf buf = Unpooled.buffer(totalLength);
            
            // 车牌号码 (21字节)
            ByteBufUtil.writeString(buf, vehicleNo, 21);
            
            // 车牌颜色 (1字节)
            buf.writeByte(vehicleColor);
            
            // 子业务类型标识 (2字节)
            buf.writeShort(subBusinessType);
            
            // 后续数据长度 (4字节)
            buf.writeInt(controlParams != null ? controlParams.length : 0);
            
            // 控制参数
            if (controlParams != null) {
                buf.writeBytes(controlParams);
            }
            
            byte[] result = new byte[buf.readableBytes()];
            buf.readBytes(result);
            buf.release();
            
            return result;
        }
        
        @Override
        public void decode(byte[] data) {
            ByteBuf buf = Unpooled.wrappedBuffer(data);
            
            // 车牌号码 (21字节)
            vehicleNo = ByteBufUtil.readString(buf, 21);
            
            // 车牌颜色 (1字节)
            vehicleColor = buf.readUnsignedByte();
            
            // 子业务类型标识 (2字节)
            subBusinessType = buf.readUnsignedShort();
            
            // 后续数据长度 (4字节)
            subsequentDataLength = buf.readInt();
            
            // 控制参数
            if (subsequentDataLength > 0 && buf.isReadable(subsequentDataLength)) {
                controlParams = new byte[subsequentDataLength];
                buf.readBytes(controlParams);
            }
            
            buf.release();
        }
    }
}
