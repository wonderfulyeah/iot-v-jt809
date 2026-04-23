package org.iot.v.jt809.core.message.upstream.vehicle.entity;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import org.iot.v.jt809.core.message.upstream.vehicle.VehicleDynamicMsg;
import org.iot.v.jt809.core.util.BCDUtil;
import org.iot.v.jt809.core.util.ByteBufUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 车辆定位信息（2019版）
 */
@Data
public class VehicleLocationInfo {

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
    private VehicleDynamicMsg.AlarmFlag alarmFlag;

    /**
     * 状态标志（4字节 = 32位）
     */
    private VehicleDynamicMsg.StatusFlag statusFlag;

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
        alarmFlag = new VehicleDynamicMsg.AlarmFlag();
        alarmFlag.decode(alarmBytes);

        // 状态标志 (4字节)
        byte[] statusBytes = new byte[4];
        buf.readBytes(statusBytes);
        statusFlag = new VehicleDynamicMsg.StatusFlag();
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
