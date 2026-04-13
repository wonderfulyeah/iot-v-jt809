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

import java.util.ArrayList;
import java.util.List;

/**
 * 车辆定位信息消息（消息ID: 0x1200）
 * 下级平台上报车辆定位数据
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleDynamicMsg extends BaseMessage {
    
    /**
     * 子业务类型标识：车辆定位信息自动补报请求消息
     */
    public static final int SUB_BUSINESS_TYPE_1201 = 0x1201;
    public static final int SUB_BUSINESS_TYPE_1202 = 0x1202;
    public static final int SUB_BUSINESS_TYPE_1203 = 0x1203;

    public VehicleDynamicMsg() {
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
         * 定位数据（兼容旧版本）
         */
        private LocationData locationData;
        
        /**
         * 1202：实时上传车辆定位信息（单条）
         */
        private VehicleLocationInfo vehicleLocationInfo;
        
        /**
         * 1203特有数据：车辆定位信息自动补报数据（多条）
         */
        private LocationSupplementData locationSupplementData;
        
        /**
         * 1201：上传车辆注册信息
         */
        private VehicleRegistrationData vehicleRegistrationData;
        
        @Override
        public byte[] encode() {
            byte[] dataBytes;
            
            if (subBusinessType == SUB_BUSINESS_TYPE_1203 && locationSupplementData != null) {
                dataBytes = locationSupplementData.encode();
            } else if (subBusinessType == SUB_BUSINESS_TYPE_1202 && vehicleLocationInfo != null) {
                dataBytes = vehicleLocationInfo.encode();
            } else if (subBusinessType == SUB_BUSINESS_TYPE_1201 && vehicleRegistrationData != null) {
                dataBytes = vehicleRegistrationData.encode();
            } else if (locationData != null) {
                dataBytes = locationData.encode();
            } else {
                dataBytes = new byte[0];
            }
            
            int totalLength = 28 + dataBytes.length;
            
            ByteBuf buf = Unpooled.buffer(totalLength);
            
            // 车牌号码 (21字节)
            ByteBufUtil.writeString(buf, vehicleNo, 21);
            
            // 车牌颜色 (1字节)
            buf.writeByte(vehicleColor);
            
            // 子业务类型标识 (2字节)
            buf.writeShort(subBusinessType);
            
            // 后续数据长度 (4字节)
            buf.writeInt(dataBytes.length);
            
            // 定位数据
            if (dataBytes.length > 0) {
                buf.writeBytes(dataBytes);
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
            
            // 根据子业务类型解码不同的数据
            if (subsequentDataLength > 0 && buf.isReadable(subsequentDataLength)) {
                byte[] dataBytes = new byte[subsequentDataLength];
                buf.readBytes(dataBytes);
                
                if (subBusinessType == SUB_BUSINESS_TYPE_1203) {
                    locationSupplementData = new LocationSupplementData();
                    locationSupplementData.decode(dataBytes);
                } else if (subBusinessType == SUB_BUSINESS_TYPE_1202) {
                    vehicleLocationInfo = new VehicleLocationInfo();
                    vehicleLocationInfo.decode(Unpooled.wrappedBuffer(dataBytes));
                } else if (subBusinessType == SUB_BUSINESS_TYPE_1201) {
                    vehicleRegistrationData = new VehicleRegistrationData();
                    vehicleRegistrationData.decode(dataBytes);
                } else {
                    locationData = new LocationData();
                    locationData.decode(dataBytes);
                }
            }
            
            buf.release();
        }
    }
    
    /**
     * 定位数据（兼容其他子业务类型）
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
    
    /**
     * 1203特有数据：车辆定位信息自动补报数据
     */
    @Data
    public static class LocationSupplementData {
        
        /**
         * 定位数数量（1字节）
         */
        private int locationCount;
        
        /**
         * 定位数据列表
         */
        private List<VehicleLocationInfo> locationList;
        
        public byte[] encode() {
            if (locationList == null || locationList.isEmpty()) {
                return new byte[1]; // 只包含locationCount
            }
            
            // 计算总长度
            int dataLength = 1; // locationCount
            for (VehicleLocationInfo info : locationList) {
                dataLength += info.calculateEncodeLength();
            }
            
            ByteBuf buf = Unpooled.buffer(dataLength);
            
            // 定位数数量 (1字节)
            buf.writeByte(locationList.size());
            
            // 定位数据列表
            for (VehicleLocationInfo info : locationList) {
                byte[] infoBytes = info.encode();
                buf.writeBytes(infoBytes);
            }
            
            byte[] result = new byte[buf.readableBytes()];
            buf.readBytes(result);
            buf.release();
            
            return result;
        }
        
        public void decode(byte[] data) {
            ByteBuf buf = Unpooled.wrappedBuffer(data);
            
            // 定位数数量 (1字节)
            locationCount = buf.readUnsignedByte();
            
            // 定位数据列表
            locationList = new ArrayList<>();
 for (int i = 0; i < locationCount; i++) {
                VehicleLocationInfo info = new VehicleLocationInfo();
                info.decode(buf);
                locationList.add(info);
            }
            
            buf.release();
        }
    }
    
    /**
     * 车辆定位信息（2019版）
     */
    @Data
    public static class VehicleLocationInfo {
        
        /**
         * 是否使用国家测绘局批准的地图保密插件进行加密（1字节）
         * 0-未加密，1-已加密
         */
        private int isEncrypted;
        
        /**
         * 车辆定位信息数据长度（4字节）
         */
        private int dataLength;
        
        /**
         * 报警标志（4字节 = 32位）
         */
        private AlarmFlag alarmFlag;
        
        /**
         * 状态标志（4字节 = 32位）
         */
        private StatusFlag statusFlag;
        
        /**
         * 纬度（4字节，单位：1e-6度）
         */
        private int latitude;
        
        /**
         * 经度（4字节，单位：1e-6度）
         */
        private int longitude;
        
        /**
         * 海拔高度/高程（2字节，单位：米）
         */
        private int altitude;
        
        /**
         * 速度（2字节，单位：1/10km/h）
         */
        private int speed;
        
        /**
         * 方向（2字节，0-359度）
         */
        private int direction;
        
        /**
         * 位置汇报时间（6字节 BCD）
         */
        private String time;
        
        /**
         * 附加信息列表
         */
        private List<AdditionalInfo> additionalInfoList;
        
        /**
         * 监控平台唯一编码（11字节）
         */
        private String platformId1;
        
        /**
         * 报警状态1（4字节）
         */
        private long alarmStatus1;
        
        /**
         * 市级监控平台唯一编码（11字节）
         */
        private String platformId2;
        
        /**
         * 报警状态2（4字节）
         */
        private long alarmStatus2;
        
        /**
         * 省级监控平台唯一编码（11字节）
         */
        private String platformId3;
        
        /**
         * 报警状态3（4字节）
         */
        private long alarmStatus3;
        
        public int calculateEncodeLength() {
            int length = 1 + 4 + 4 + 4 + 4 + 4 + 2 + 2 + 2 + 6;
            if (additionalInfoList != null) {
                for (AdditionalInfo info : additionalInfoList) {
                    length += 1 + 2 + info.getInfoLength();
                }
            }
            length += 11 + 4 + 11 + 4 + 11 + 4; // 三个平台ID和报警状态
            return length;
        }
        
        public byte[] encode() {
            int length = calculateEncodeLength();
            ByteBuf buf = Unpooled.buffer(length);
            
            // 是否加密 (1字节)
            buf.writeByte(isEncrypted);
            
            // 数据长度 (4字节)
            buf.writeInt(dataLength);
            
            // 报警标志 (4字节)
            if (alarmFlag != null) {
                buf.writeBytes(alarmFlag.encode());
            } else {
                buf.writeBytes(new byte[4]);
            }
            
            // 状态标志 (4字节)
            if (statusFlag != null) {
                buf.writeBytes(statusFlag.encode());
            } else {
                buf.writeBytes(new byte[4]);
            }
            
            // 纬度 (4字节)
            buf.writeInt(latitude);
            
            // 经度 (4字节)
            buf.writeInt(longitude);
            
            // 高程 (2字节)
            buf.writeShort(altitude);
            
            // 速度 (2字节)
            buf.writeShort(speed);
            
            // 方向 (2字节)
            buf.writeShort(direction);
            
            // 位置汇报时间 (6字节 BCD)
            if (time != null) {
                byte[] timeBytes = BCDUtil.encodeBCD(time, 6);
                buf.writeBytes(timeBytes);
            } else {
                buf.writeBytes(new byte[6]);
            }
            
            // 附加信息列表
            if (additionalInfoList != null) {
                for (AdditionalInfo info : additionalInfoList) {
                    byte[] infoBytes = info.encode();
                    buf.writeBytes(infoBytes);
                }
            }
            
            // 监控平台ID1 (11字节)
            ByteBufUtil.writeString(buf, platformId1, 11);
            
            // 报警状态1 (4字节)
            buf.writeInt((int) alarmStatus1);
            
            // 市级监控平台ID2 (11字节)
            ByteBufUtil.writeString(buf, platformId2, 11);
            
            // 报警状态2 (4字节)
            buf.writeInt((int) alarmStatus2);
            
            // 省级监控平台ID3 (11字节)
            ByteBufUtil.writeString(buf, platformId3, 11);
            
            // 报警状态3 (4字节)
            buf.writeInt((int) alarmStatus3);
            
            byte[] result = new byte[buf.readableBytes()];
            buf.readBytes(result);
            buf.release();
            
            return result;
        }
        
        public void decode(ByteBuf buf) {
            // 是否加密 (1字节)
            isEncrypted = buf.readUnsignedByte();
            
            // 数据长度 (4字节)
            dataLength = buf.readInt();
            
            // 报警标志 (4字节)
            byte[] alarmBytes = new byte[4];
            buf.readBytes(alarmBytes);
            alarmFlag = new AlarmFlag();
            alarmFlag.decode(alarmBytes);
            
            // 状态标志 (4字节)
            byte[] statusBytes = new byte[4];
            buf.readBytes(statusBytes);
            statusFlag = new StatusFlag();
            statusFlag.decode(statusBytes);
            
            // 纬度 (4字节)
            latitude = buf.readInt();
            
            // 经度 (4字节)
            longitude = buf.readInt();
            
            // 高程 (2字节)
            altitude = buf.readUnsignedShort();
            
            // 速度 (2字节)
            speed = buf.readUnsignedShort();
            
            // 方向 (2字节)
            direction = buf.readUnsignedShort();
            
            // 位置汇报时间 (6字节 BCD)
            byte[] timeBytes = new byte[6];
            buf.readBytes(timeBytes);
            time = BCDUtil.decodeBCD(timeBytes);
            
            // 附加信息列表
            // 固定部分长度：alarmFlag(4) + statusFlag(4) + lat(4) + lon(4) + alt(2) + speed(2) + dir(2) + time(6) = 28字节
            // 附加信息长度 = dataLength - 28
            int fixedPartLength = 4 + 4 + 4 + 4 + 2 + 2 + 2 + 6; // 28字节
            int additionalInfoTotalLength = dataLength - fixedPartLength;
            
            additionalInfoList = new ArrayList<>();
            int additionalInfoBytesRead = 0;
            while (additionalInfoBytesRead < additionalInfoTotalLength && buf.isReadable(3)) {
                AdditionalInfo info = new AdditionalInfo();
                int startReaderIndex = buf.readerIndex();
                if (!info.decode(buf)) {
                    break;
                }
                int bytesRead = buf.readerIndex() - startReaderIndex;
                additionalInfoBytesRead += bytesRead;
                additionalInfoList.add(info);
            }
            
            // 监控平台ID1 (11字节)
            if (buf.isReadable(11)) {
                platformId1 = ByteBufUtil.readString(buf, 11);
            }
            
            // 报警状态1 (4字节)
            if (buf.isReadable(4)) {
                alarmStatus1 = buf.readUnsignedInt();
            }
            
            // 市级监控平台ID2 (11字节)
            if (buf.isReadable(11)) {
                platformId2 = ByteBufUtil.readString(buf, 11);
            }
            
            // 报警状态2 (4字节)
            if (buf.isReadable(4)) {
                alarmStatus2 = buf.readUnsignedInt();
            }
            
            // 省级监控平台ID3 (11字节)
            if (buf.isReadable(11)) {
                platformId3 = ByteBufUtil.readString(buf, 11);
            }
            
            // 报警状态3 (4字节)
            if (buf.isReadable(4)) {
                alarmStatus3 = buf.readUnsignedInt();
            }
        }
    }
    
    /**
     * 报警标志（4字节 = 32位）
     */
    @Data
    public static class AlarmFlag {
        
        /**
         * 原始字节数据
         */
        private byte[] rawData = new byte[4];
        
        // ========== 报警标志位（根据JT809-2019协议定义） ==========
        
        /**
         * bit0: 超速报警
         */
        private boolean overspeedAlarm;
        
        /**
         * bit1: 疲劳驾驶报警
         */
        private boolean fatigueDrivingAlarm;
        
        /**
         * bit2: 紧急报警
         */
        private boolean emergencyAlarm;
        
        /**
         * bit3: 进出区域报警
         */
        private boolean enterExitAreaAlarm;
        
        /**
         * bit4: 路段堵塞报警
         */
        private boolean roadBlockAlarm;
        
        /**
         * bit5: 危险路段报警
         */
        private boolean dangerousRoadAlarm;
        
        /**
         * bit6: 越界报警
         */
        private boolean boundaryAlarm;
        
        /**
         * bit7: 盗车报警
         */
        private boolean vehicleTheftAlarm;
        
        /**
         * bit8: 劫车报警
         */
        private boolean vehicleHijackAlarm;
        
        /**
         * bit9: 偏离路线报警
         */
        private boolean routeDeviationAlarm;
        
        /**
         * bit10: 车辆VSD（行驶记录仪）故障报警
         */
        private boolean vsdFaultAlarm;
        
        /**
         * bit11: 车辆油量异常报警
         */
        private boolean fuelAbnormalAlarm;
        
        /**
         * bit12: 车辆被盗报警（未确认）
         */
        private boolean vehicleStolenAlarm;
        
        /**
         * bit13: 车辆非法点火报警
         */
        private boolean illegalIgnitionAlarm;
        
        /**
         * bit14: 车辆非法位移报警
         */
        private boolean illegalDisplacementAlarm;
        
        /**
         * bit15: 抢劫报警
         */
        private boolean robberyAlarm;
        
        /**
         * bit16: 越界报警（可疑）
         */
        private boolean boundaryAlarm2;
        
        /**
         * bit17: GPRS模块故障报警
         */
        private boolean gprsFaultAlarm;
        
        /**
         * bit18: GNSS模块故障报警
         */
        private boolean gnssFaultAlarm;
        
        /**
         * bit19: GNSS天线短路报警
         */
        private boolean gnssAntennaShortAlarm;
        
        /**
         * bit20: GNSS天线开路报警
         */
        private boolean gnssAntennaOpenAlarm;
        
        /**
         * bit21: LCD显示屏故障报警
         */
        private boolean lcdFaultAlarm;
        
        /**
         * bit22: TTS模块故障报警
         */
        private boolean ttsFaultAlarm;
        
        /**
         * bit23: 摄像头故障报警
         */
        private boolean cameraFaultAlarm;
        
        /**
         * bit24: 当天累计驾驶超时报警
         */
        private boolean dailyOvertimeAlarm;
        
        /**
         * bit25: 超时停车报警
         */
        private boolean overtimeParkingAlarm;
        
        /**
         * bit26: 进出路线报警
         */
        private boolean enterExitRouteAlarm;
        
        /**
         * bit27: 进出线城市报警
         */
        private boolean enterExitCityAlarm;
        
        /**
         * bit28: 路线偏离报警2
         */
        private boolean routeDeviationAlarm2;
        
        /**
         * bit29: 油量监控报警
         */
        private boolean fuelMonitorAlarm;
        
        /**
         * bit30: 熄火后异常移动报警
         */
        private boolean abnormalMoveAfterFlameoutAlarm;
        
        /**
         * bit31: 碰撞侧翻报警
         */
        private boolean collisionRolloverAlarm;
        
        public byte[] encode() {
            return rawData.clone();
        }
        
        public void decode(byte[] data) {
            if (data == null || data.length < 4) {
                return;
            }
            
            System.arraycopy(data, 0, rawData, 0, 4);
            
            // 解析低位字节（从低位到高位）
            // 使用大端序：第一个字节是最高位字节
            // bit0-bit7 在最后一个字节（索引3）
            // bit8-bit15 在倒数第二个字节（索引2）
            // 依此类推
            
            // 解析报警标志位
            overspeedAlarm = getBit(0);
            fatigueDrivingAlarm = getBit(1);
            emergencyAlarm = getBit(2);
            enterExitAreaAlarm = getBit(3);
            roadBlockAlarm = getBit(4);
            dangerousRoadAlarm = getBit(5);
            boundaryAlarm = getBit(6);
            vehicleTheftAlarm = getBit(7);
            vehicleHijackAlarm = getBit(8);
            routeDeviationAlarm = getBit(9);
            vsdFaultAlarm = getBit(10);
            fuelAbnormalAlarm = getBit(11);
            vehicleStolenAlarm = getBit(12);
            illegalIgnitionAlarm = getBit(13);
            illegalDisplacementAlarm = getBit(14);
            robberyAlarm = getBit(15);
            boundaryAlarm2 = getBit(16);
            gprsFaultAlarm = getBit(17);
            gnssFaultAlarm = getBit(18);
            gnssAntennaShortAlarm = getBit(19);
            gnssAntennaOpenAlarm = getBit(20);
            lcdFaultAlarm = getBit(21);
            ttsFaultAlarm = getBit(22);
            cameraFaultAlarm = getBit(23);
            dailyOvertimeAlarm = getBit(24);
            overtimeParkingAlarm = getBit(25);
            enterExitRouteAlarm = getBit(26);
            enterExitCityAlarm = getBit(27);
            routeDeviationAlarm2 = getBit(28);
            fuelMonitorAlarm = getBit(29);
            abnormalMoveAfterFlameoutAlarm = getBit(30);
            collisionRolloverAlarm = getBit(31);
        }
        
        /**
         * 获取指定位的值
         *
         * @param bitIndex 位索引（0-31）
         * @return true-该位为1, false-该位为0
         */
        private boolean getBit(int bitIndex) {
            // 大端序：第0位在最后一个字节的最低位
            int byteIndex = 3 - (bitIndex / 8);
            int bitOffset = bitIndex % 8;
            return (rawData[byteIndex] & (1 << bitOffset)) != 0;
        }
        
        /**
         * 设置指定位的值
         *
         * @param bitIndex 位索引（0-31）
         * @param value true-设置为1, false-设置为0
         */
        private void setBit(int bitIndex, boolean value) {
            int byteIndex = 3 - (bitIndex / 8);
            int bitOffset = bitIndex % 8;
            if (value) {
                rawData[byteIndex] |= (1 << bitOffset);
            } else {
                rawData[byteIndex] &= ~(1 << bitOffset);
            }
        }
    }
    
    /**
     * 状态标志（4字节 = 32位）
     */
    @Data
    public static class StatusFlag {
        
        /**
         * 原始字节数据
         */
        private byte[] rawData = new byte[4];
        
        // ========== 状态标志位（根据JT809-2019协议定义） ==========
        
        /**
         * bit0: ACC状态（0-ACC关，1-ACC开）
         */
        private boolean accStatus;
        
        /**
         * bit1: 左转向灯状态（0-关闭，1-开启）
         */
        private boolean leftTurnSignal;
        
        /**
         * bit2: 右转向灯状态（0-关闭，1-开启）
         */
        private boolean rightTurnSignal;
        
        /**
         * bit3: 远光灯状态（0-关闭，1-开启）
         */
        private boolean highBeam;
        
        /**
         * bit4: 近光灯状态（0-关闭，1-开启）
         */
        private boolean lowBeam;
        
        /**
         * bit5: 制动状态（0-未制动，1-制动）
         */
        private boolean brakeStatus;
        
        /**
         * bit6: 门状态（0-门关，1-门开）
         */
        private boolean doorStatus;
        
        /**
         * bit7: 空调状态（0-关闭，1-开启）
         */
        private boolean airConditioner;
        
        /**
         * bit8: 示廓灯状态（0-关闭，1-开启）
         */
        private boolean outlineLight;
        
        /**
         * bit9: 倒车状态（0-未倒车，1-倒车）
         */
        private boolean reverseStatus;
        
        /**
         * bit10: 雾灯状态（0-关闭，1-开启）
         */
        private boolean fogLight;
        
        /**
         * bit11: 危险报警闪光灯状态（0-关闭，1-开启）
         */
        private boolean hazardLight;
        
        /**
         * bit12: 遮阳板状态（0-关闭，1-开启）
         */
        private boolean sunVisor;
        
        /**
         * bit13: 顶棚灯状态（0-关闭，1-开启）
         */
        private boolean ceilingLight;
        
        /**
         * bit14: 巡航状态（0-未巡航，1-巡航）
         */
        private boolean cruiseStatus;
        
        /**
         * bit15: GPS卫星定位状态（0-无效，1-有效）
         */
        private boolean gpsValid;
        
        /**
         * bit16: BEIDOU卫星定位状态（0-无效，1-有效）
         */
        private boolean beidouValid;
        
        /**
         * bit17: GLONASS卫星定位状态（0-无效，1-有效）
         */
        private boolean glonassValid;
        
        /**
         * bit18: GALILEO卫星定位状态（0-无效，1-有效）
         */
        private boolean galileoValid;
        
        /**
         * bit19: 道路运输证IC卡读卡器状态（0-正常，1-异常）
         */
        private boolean icCardReaderFault;
        
        /**
         * bit20: 道路运输证IC卡状态（0-未插卡，1-已插卡）
         */
        private boolean icCardInserted;
        
        /**
         * bit21: 门状态2（0-门关，1-门开）
         */
        private boolean doorStatus2;
        
        /**
         * bit22-31: 保留位
         */
        
        public byte[] encode() {
            return rawData.clone();
        }
        
        public void decode(byte[] data) {
            if (data == null || data.length < 4) {
                return;
            }
            
            System.arraycopy(data, 0, rawData, 0, 4);
            
            // 解析状态标志位（大端序）
            accStatus = getBit(0);
            leftTurnSignal = getBit(1);
            rightTurnSignal = getBit(2);
            highBeam = getBit(3);
            lowBeam = getBit(4);
            brakeStatus = getBit(5);
            doorStatus = getBit(6);
            airConditioner = getBit(7);
            outlineLight = getBit(8);
            reverseStatus = getBit(9);
            fogLight = getBit(10);
            hazardLight = getBit(11);
            sunVisor = getBit(12);
            ceilingLight = getBit(13);
            cruiseStatus = getBit(14);
            gpsValid = getBit(15);
            beidouValid = getBit(16);
            glonassValid = getBit(17);
            galileoValid = getBit(18);
            icCardReaderFault = getBit(19);
            icCardInserted = getBit(20);
            doorStatus2 = getBit(21);
        }
        
        /**
         * 获取指定位的值
         *
         * @param bitIndex 位索引（0-31）
         * @return true-该位为1, false-该位为0
         */
        private boolean getBit(int bitIndex) {
            int byteIndex = 3 - (bitIndex / 8);
            int bitOffset = bitIndex % 8;
            return (rawData[byteIndex] & (1 << bitOffset)) != 0;
        }
        
        /**
         * 设置指定位的值
         *
         * @param bitIndex 位索引（0-31）
         * @param value true-设置为1, false-设置为0
         */
        private void setBit(int bitIndex, boolean value) {
            int byteIndex = 3 - (bitIndex / 8);
            int bitOffset = bitIndex % 8;
            if (value) {
                rawData[byteIndex] |= (1 << bitOffset);
            } else {
                rawData[byteIndex] &= ~(1 << bitOffset);
            }
        }
    }
    
    /**
     * 附加信息
     */
    @Data
    public static class AdditionalInfo {
        
        /**
         * 附加信息ID（1字节）
         */
        private int infoId;
        
        /**
         * 附加信息长度（2字节）
         */
        private int infoLength;
        
        /**
         * 附加信息内容
         */
        private byte[] infoContent;
        
        public int getInfoLength() {
            return infoContent != null ? infoContent.length : 0;
        }
        
        public byte[] encode() {
            int length = 1 + 2 + (infoContent != null ? infoContent.length : 0);
            ByteBuf buf = Unpooled.buffer(length);
            
            // 附加信息ID (1字节)
            buf.writeByte(infoId);
            
            // 附加信息长度 (2字节)
            buf.writeShort(infoContent != null ? infoContent.length : 0);
            
            // 附加信息内容
            if (infoContent != null && infoContent.length > 0) {
                buf.writeBytes(infoContent);
            }
            
            byte[] result = new byte[buf.readableBytes()];
            buf.readBytes(result);
            buf.release();
            
            return result;
        }
        
        /**
         * 从ByteBuf解码附加信息
         *
         * @param buf ByteBuf
         * @return true-成功解码, false-数据不完整
         */
        public boolean decode(ByteBuf buf) {
            if (!buf.isReadable(3)) {
                return false;
            }
            
            // 附加信息ID (1字节)
            infoId = buf.readUnsignedByte();

            // 附加信息长度 (2字节)
            infoLength = buf.readUnsignedByte();
            
            // 附加信息内容
            if (infoLength > 0) {
                if (!buf.isReadable(infoLength)) {
                    return false;
                }
                infoContent = new byte[infoLength];
                buf.readBytes(infoContent);
            } else {
                infoContent = new byte[0];
            }
            
            return true;
        }
    }
    
    /**
     * 1201：上传车辆注册信息
     */
    @Data
    public static class VehicleRegistrationData {
        
        /**
         * 平台唯一编码（20字节）
         */
        private String platformId;
        
        /**
         * 车载终端厂商唯一编码（20字节）
         */
        private String terminalManufacturerId;
        
        /**
         * 车载终端型号（40字节）
         */
        private String terminalModel;
        
        /**
         * 车载终端通讯模块IMEI码（20字节）
         */
        private String terminalImei;
        
        /**
         * 车载终端编号（20字节）
         */
        private String terminalId;
        
        /**
         * 车载终端（10字节）
         */
        private String terminalSimNumber;
        
        public byte[] encode() {
            ByteBuf buf = Unpooled.buffer(130);
            
            // 平台唯一编码 (20字节)
            ByteBufUtil.writeString(buf, platformId, 20);
            
            // 车载终端厂商唯一编码 (20字节)
            ByteBufUtil.writeString(buf, terminalManufacturerId, 20);
            
            // 车载终端型号 (40字节)
            ByteBufUtil.writeString(buf, terminalModel, 40);
            
            // 车载终端通讯模块IMEI码 (20字节)
            ByteBufUtil.writeString(buf, terminalImei, 20);
            
            // 车载终端编号 (20字节)
            ByteBufUtil.writeString(buf, terminalId, 20);
            
            // 车载终端 (10字节)
            ByteBufUtil.writeString(buf, terminalSimNumber, 10);
            
            byte[] result = new byte[buf.readableBytes()];
            buf.readBytes(result);
            buf.release();
            
            return result;
        }
        
        public void decode(byte[] data) {
            ByteBuf buf = Unpooled.wrappedBuffer(data);
            
            // 平台唯一编码 (20字节)
            platformId = ByteBufUtil.readString(buf, 11);
            
            // 车载终端厂商唯一编码 (20字节)
            terminalManufacturerId = ByteBufUtil.readString(buf, 11);
            
            // 车载终端型号 (40字节)
            terminalModel = ByteBufUtil.readString(buf, 30);
            
            // 车载终端通讯模块IMEI码 (20字节)
            terminalImei = ByteBufUtil.readString(buf, 15);
            
            // 车载终端编号 (20字节)
            terminalId = ByteBufUtil.readString(buf, 30);
            
            // 车载终端 (10字节)
            terminalSimNumber = ByteBufUtil.readString(buf, 13);
            
            buf.release();
        }
    }
}