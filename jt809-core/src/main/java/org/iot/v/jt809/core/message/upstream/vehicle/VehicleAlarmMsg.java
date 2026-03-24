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
 * 车辆报警信息消息（消息ID: 0x1401）
 * 下级平台上报车辆报警信息
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleAlarmMsg extends BaseMessage {
    
    public VehicleAlarmMsg() {
        setMsgId(MessageType.VEHICLE_ALARM);
        setBody(new Body());
    }
    
    @Override
    public String getMessageTypeName() {
        return "车辆报警信息";
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
         * 报警数据
         */
        private AlarmData alarmData;
        
        @Override
        public byte[] encode() {
            byte[] alarmBytes = alarmData != null ? alarmData.encode() : new byte[0];
            int totalLength = 28 + alarmBytes.length;
            
            ByteBuf buf = Unpooled.buffer(totalLength);
            
            // 车牌号码 (21字节)
            ByteBufUtil.writeString(buf, vehicleNo, 21);
            
            // 车牌颜色 (1字节)
            buf.writeByte(vehicleColor);
            
            // 子业务类型标识 (2字节)
            buf.writeShort(subBusinessType);
            
            // 后续数据长度 (4字节)
            buf.writeInt(alarmBytes.length);
            
            // 报警数据
            if (alarmBytes.length > 0) {
                buf.writeBytes(alarmBytes);
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
            
            // 报警数据
            if (subsequentDataLength > 0 && buf.isReadable(subsequentDataLength)) {
                byte[] alarmBytes = new byte[subsequentDataLength];
                buf.readBytes(alarmBytes);
                alarmData = new AlarmData();
                alarmData.decode(alarmBytes);
            }
            
            buf.release();
        }
    }
    
    /**
     * 报警数据
     */
    @Data
    public static class AlarmData {
        
        /**
         * 报警信息来源（2字节）
         * 1: 车载终端
         * 2: 企业监控平台
         * 3: 其他
         */
        private int alarmSource;
        
        /**
         * 报警类型（4字节）
         */
        private long alarmType;
        
        /**
         * 报警时间（6字节 BCD）
         */
        private String alarmTime;
        
        /**
         * 经度（4字节）
         */
        private int longitude;
        
        /**
         * 纬度（4字节）
         */
        private int latitude;
        
        /**
         * 速度（2字节）
         */
        private int speed;
        
        /**
         * 方向（2字节）
         */
        private int direction;
        
        /**
         * 海拔高度（2字节）
         */
        private int altitude;
        
        /**
         * 车辆状态（4字节）
         */
        private long vehicleStatus;
        
        /**
         * 报警描述（变长）
         */
        private String alarmDesc;
        
        public byte[] encode() {
            byte[] descBytes = alarmDesc != null ? alarmDesc.getBytes() : new byte[0];
            ByteBuf buf = Unpooled.buffer(26 + descBytes.length);
            
            // 报警信息来源 (2字节)
            buf.writeShort(alarmSource);
            
            // 报警类型 (4字节)
            buf.writeInt((int) alarmType);
            
            // 报警时间 (6字节 BCD)
            byte[] timeBytes = BCDUtil.encodeBCD(alarmTime, 6);
            buf.writeBytes(timeBytes);
            
            // 经度 (4字节)
            buf.writeInt(longitude);
            
            // 纬度 (4字节)
            buf.writeInt(latitude);
            
            // 速度 (2字节)
            buf.writeShort(speed);
            
            // 方向 (2字节)
            buf.writeShort(direction);
            
            // 海拔高度 (2字节)
            buf.writeShort(altitude);
            
            // 车辆状态 (4字节)
            buf.writeInt((int) vehicleStatus);
            
            // 报警描述
            if (descBytes.length > 0) {
                buf.writeBytes(descBytes);
            }
            
            byte[] result = new byte[buf.readableBytes()];
            buf.readBytes(result);
            buf.release();
            
            return result;
        }
        
        public void decode(byte[] data) {
            ByteBuf buf = Unpooled.wrappedBuffer(data);
            
            // 报警信息来源 (2字节)
            alarmSource = buf.readUnsignedShort();
            
            // 报警类型 (4字节)
            alarmType = buf.readUnsignedInt();
            
            // 报警时间 (6字节 BCD)
            byte[] timeBytes = new byte[6];
            buf.readBytes(timeBytes);
            alarmTime = BCDUtil.decodeBCD(timeBytes);
            
            // 经度 (4字节)
            longitude = buf.readInt();
            
            // 纬度 (4字节)
            latitude = buf.readInt();
            
            // 速度 (2字节)
            speed = buf.readUnsignedShort();
            
            // 方向 (2字节)
            direction = buf.readUnsignedShort();
            
            // 海拔高度 (2字节)
            altitude = buf.readUnsignedShort();
            
            // 车辆状态 (4字节)
            vehicleStatus = buf.readUnsignedInt();
            
            // 报警描述
            if (buf.isReadable()) {
                byte[] descBytes = new byte[buf.readableBytes()];
                buf.readBytes(descBytes);
                alarmDesc = new String(descBytes);
            }
            
            buf.release();
        }
    }
}
