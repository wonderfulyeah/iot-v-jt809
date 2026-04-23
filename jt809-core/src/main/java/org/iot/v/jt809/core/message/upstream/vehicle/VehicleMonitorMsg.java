package org.iot.v.jt809.core.message.upstream.vehicle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.base.MessageBody;
import org.iot.v.jt809.core.message.upstream.vehicle.entity.VehicleLocationInfo;
import org.iot.v.jt809.core.util.ByteBufUtil;

/**
 * 主链路车辆监管消息（消息ID: 0x1500）
 * 子业务类型 1502: 车辆拍照应答
 *
 * @author haye
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleMonitorMsg extends BaseMessage {

    /**
     * 子业务类型：车辆拍照应答
     */
    public static final int SUB_BUSINESS_TYPE_1502 = 0x1502;

    public VehicleMonitorMsg() {
        setMsgId(MessageType.VEHICLE_MONITOR);
        setBody(new Body());
    }

    @Override
    public String getMessageTypeName() {
        return "主链路车辆监管消息";
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
         * 1502: 车辆拍照应答数据
         */
        private PhotoResponseData photoResponseData;

        @Override
        public byte[] encode() {
            byte[] dataBytes;
            if (subBusinessType == SUB_BUSINESS_TYPE_1502 && photoResponseData != null) {
                dataBytes = photoResponseData.encode();
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

            // 后续数据
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

                if (subBusinessType == SUB_BUSINESS_TYPE_1502) {
                    photoResponseData = new PhotoResponseData();
                    photoResponseData.decode(dataBytes);
                } else {
                    log.warn("Unsupported Sub Business Type: 0x{}", Integer.toHexString(subBusinessType));
                }
            }

            buf.release();
        }
    }

    /**
     * 1502：车辆拍照应答数据（外部类实体）
     * <p>
     * 复用 VehicleDynamicMsg.VehicleLocationInfo 作为定位信息字段，
     * 不修改 VehicleLocationInfo 的内部实现。
     *
     * @author haye
     */
    @Data
    public static class PhotoResponseData {

        /**
         * 对应启动车辆拍照请求消息源子业务类型标识（2字节）
         */
        private int sourceSubBusinessType;

        /**
         * 对应启动车辆拍照请求消息源报文序列号（4字节）
         */
        private int sourceSeqNo;

        /**
         * 拍照应答标识（1字节）
         * 0: 成功
         * 1: 失败
         */
        private int photoResponseFlag;

        /**
         * 车辆定位信息（2019版）— 复用已有类
         *
         * @see VehicleLocationInfo
         */
        private VehicleLocationInfo locationInfo;

        /**
         * 镜头ID（1字节）
         */
        private int cameraId;

        /**
         * 图片长度（4字节，单位：字节）
         */
        private int imageLength;

        /**
         * 图片大小
         */
        private int imageSize;


        /**
         * 图像格式（1字节）
         * 0: JPEG
         * 1: PNG
         * 2: GIF
         * ...
         */
        private int imageFormat;

        /**
         * 图片内容（变长，存储为十六进制字符串）
         */
        private String imageDataHex;

        public byte[] encode() {
            // 计算图片字节数组长度
            byte[] imageBytes = imageDataHex != null ? hexStringToBytes(imageDataHex) : new byte[0];

            // 定位信息编码
            byte[] locationBytes;
            if (locationInfo != null) {
                locationBytes = locationInfo.encode();
            } else {
                locationBytes = new byte[0];
            }

            // 总长度计算
            int totalLength = 2 + 4 + 1 + locationBytes.length + 1 + 4 + 2 + 2 + 1 + imageBytes.length;

            ByteBuf buf = Unpooled.buffer(totalLength);

            // 源子业务类型标识 (2字节)
            buf.writeShort(sourceSubBusinessType);

            // 源报文序列号 (4字节)
            buf.writeInt(sourceSeqNo);

            // 拍照应答标识 (1字节)
            buf.writeByte(photoResponseFlag);

            // 定位信息
            if (locationBytes.length > 0) {
                buf.writeBytes(locationBytes);
            }

            // 镜头ID (1字节)
            buf.writeByte(cameraId);

            // 图片长度 (4字节)
            buf.writeInt(imageLength);

            // 图片大小 (1字节)
            buf.writeByte(imageSize);

            // 图像格式 (1字节)
            buf.writeByte(imageFormat);

            // 图片数据
            if (imageBytes.length > 0) {
                buf.writeBytes(imageBytes);
            }

            byte[] result = new byte[buf.readableBytes()];
            buf.readBytes(result);
            buf.release();

            return result;
        }

        public void decode(byte[] data) {
            ByteBuf buf = Unpooled.wrappedBuffer(data);

            // 源子业务类型标识 (2字节)
            sourceSubBusinessType = buf.readUnsignedShort();

            // 源报文序列号 (4字节)
            sourceSeqNo = buf.readInt();

            // 拍照应答标识 (1字节)
            photoResponseFlag = buf.readUnsignedByte();

            // ===== 车辆定位信息（复用 VehicleLocationInfo） =====
            locationInfo = new VehicleLocationInfo();
            locationInfo.decode(buf);

            // 镜头ID (1字节)
            cameraId = buf.readUnsignedByte();

            // 图片长度 (4字节)
            imageLength = buf.readInt();

            // 图片大小 (1字节)
            imageSize = buf.readUnsignedByte();


            // 图像格式 (1字节)
            imageFormat = buf.readUnsignedByte();

            // 图片内容 (变长) → 存储为十六进制字符串
            if (imageLength > 0 && buf.isReadable(imageLength)) {
                byte[] imageBytes = new byte[imageLength];
                buf.readBytes(imageBytes);
                imageDataHex = bytesToHexString(imageBytes);
            } else {
                imageDataHex = "";
            }

            buf.release();
        }

        /**
         * 字节数组转十六进制字符串（紧凑无空格）
         */
        private static String bytesToHexString(byte[] bytes) {
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }

        /**
         * 十六进制字符串转字节数组
         */
        private static byte[] hexStringToBytes(String hex) {
            if (hex == null || hex.isEmpty()) {
                return new byte[0];
            }
            int len = hex.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                        + Character.digit(hex.charAt(i + 1), 16));
            }
            return data;
        }
    }
}
