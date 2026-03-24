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
 * 车辆定位信息查询请求（消息ID: 0x9201）
 * 上级平台查询指定车辆的定位信息
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleLocationQueryReq extends BaseMessage {
    
    public VehicleLocationQueryReq() {
        setMsgId(MessageType.VEHICLE_LOCATION_QUERY);
        setBody(new Body());
    }
    
    @Override
    public String getMessageTypeName() {
        return "车辆定位信息查询请求";
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
         * 1: 蓝色
         * 2: 黄色
         * 3: 黑色
         * 4: 白色
         * 5: 绿色
         * 9: 其他
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
        
        @Override
        public byte[] encode() {
            ByteBuf buf = Unpooled.buffer(28);
            
            // 车牌号码 (21字节)
            ByteBufUtil.writeString(buf, vehicleNo, 21);
            
            // 车牌颜色 (1字节)
            buf.writeByte(vehicleColor);
            
            // 子业务类型标识 (2字节)
            buf.writeShort(subBusinessType);
            
            // 后续数据长度 (4字节)
            buf.writeInt(subsequentDataLength);
            
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
            
            buf.release();
        }
    }
}
