package org.iot.v.jt809.core.message.upstream.vehicle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.base.MessageBody;
import org.iot.v.jt809.core.util.ByteBufUtil;
import org.iot.v.jt809.core.util.BCDUtil;

/**
 * 车辆定位信息消息（消息ID: 0x1200）
 * 下级平台上报车辆定位数据
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleLocationMsg extends BaseMessage {
    
    public VehicleLocationMsg() {
        setMsgId(MessageType.VEHICLE_LOCATION);
        setBody(new Body());
    }
    
    @Override
    public String getMessageTypeName() {
        return "车辆定位信息";
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
         * 定位数据
         */
        private LocationData locationData;
        
        @Override
        public byte[] encode() {
            byte[] locationBytes = locationData != null ? locationData.encode() : new byte[0];
            int totalLength = 28 + locationBytes.length;
            
            ByteBuf buf = Unpooled.buffer(totalLength);
            
            // 车牌号码 (21字节)
            ByteBufUtil.writeString(buf, vehicleNo, 21);
            
            // 车牌颜色 (1字节)
            buf.writeByte(vehicleColor);
            
            // 子业务类型标识 (2字节)
            buf.writeShort(subBusinessType);
            
            // 后续数据长度 (4字节)
            buf.writeInt(locationBytes.length);
            
            // 定位数据
            if (locationBytes.length > 0) {
                buf.writeBytes(locationBytes);
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
            
            // 定位数据
            if (subsequentDataLength > 0 && buf.isReadable(subsequentDataLength)) {
                byte[] locationBytes = new byte[subsequentDataLength];
                buf.readBytes(locationBytes);
                locationData = new LocationData();
                locationData.decode(locationBytes);
            }
            
            buf.release();
        }
    }
    
    /**
     * 定位数据
     */
    @Data
    public static class LocationData {
        
        /**
         * 位置汇报时间（6字节 BCD）
         */
        private String time;
        
        /**
         * 经度（4字节，单位：1e-6度）
         */
        private int longitude;
        
        /**
         * 纬度（4字节，单位：1e-6度）
         */
        private int latitude;
        
        /**
         * 速度（2字节，单位：1/10km/h）
         */
        private int speed;
        
        /**
         * 行驶记录速度（2字节，单位：1/10km/h）
         */
        private int tachoSpeed;
        
        /**
         * 方向（2字节，0-359度）
         */
        private int direction;
        
        /**
         * 海拔高度（2字节，单位：米）
         */
        private int altitude;
        
        /**
         * 车辆状态（4字节）
         */
        private long vehicleStatus;
        
        /**
         * 报警状态（4字节）
         */
        private long alarmStatus;
        
        public byte[] encode() {
            ByteBuf buf = Unpooled.buffer(28);
            
            // 时间 (6字节 BCD)
            byte[] timeBytes = BCDUtil.encodeBCD(time, 6);
            buf.writeBytes(timeBytes);
            
            // 经度 (4字节)
            buf.writeInt(longitude);
            
            // 纬度 (4字节)
            buf.writeInt(latitude);
            
            // 速度 (2字节)
            buf.writeShort(speed);
            
            // 行驶记录速度 (2字节)
            buf.writeShort(tachoSpeed);
            
            // 方向 (2字节)
            buf.writeShort(direction);
            
            // 海拔高度 (2字节)
            buf.writeShort(altitude);
            
            // 车辆状态 (4字节)
            buf.writeInt((int) vehicleStatus);
            
            // 报警状态 (4字节)
            buf.writeInt((int) alarmStatus);
            
            byte[] result = new byte[buf.readableBytes()];
            buf.readBytes(result);
            buf.release();
            
            return result;
        }
        
        public void decode(byte[] data) {
            ByteBuf buf = Unpooled.wrappedBuffer(data);
            
            // 时间 (6字节 BCD)
            byte[] timeBytes = new byte[6];
            buf.readBytes(timeBytes);
            time = BCDUtil.decodeBCD(timeBytes);
            
            // 经度 (4字节)
            longitude = buf.readInt();
            
            // 纬度 (4字节)
            latitude = buf.readInt();
            
            // 速度 (2字节)
            speed = buf.readUnsignedShort();
            
            // 行驶记录速度 (2字节)
            tachoSpeed = buf.readUnsignedShort();
            
            // 方向 (2字节)
            direction = buf.readUnsignedShort();
            
            // 海拔高度 (2字节)
            altitude = buf.readUnsignedShort();
            
            // 车辆状态 (4字节)
            vehicleStatus = buf.readUnsignedInt();
            
            // 报警状态 (4字节)
            alarmStatus = buf.readUnsignedInt();
            
            buf.release();
        }
    }
}
