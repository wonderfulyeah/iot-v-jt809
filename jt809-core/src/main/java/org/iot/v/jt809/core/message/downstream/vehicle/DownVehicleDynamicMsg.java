package org.iot.v.jt809.core.message.downstream.vehicle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.base.MessageBody;
import org.iot.v.jt809.core.util.ByteBufUtil;

/**
 * 从链路动态信息交换消息（消息ID: 0x9200）
 *
 * @author haye
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class DownVehicleDynamicMsg extends BaseMessage {

    /**
     * 920A：上报驾驶员身份信息请求消息。
     */
    public static final int SUB_BUSINESS_TYPE_920A = 0x920A;

    public DownVehicleDynamicMsg() {
        setMsgId(MessageType.DOWN_EXG_MSG);
        setBody(new Body());
    }

    @Override
    public String getMessageTypeName() {
        return "从链路动态信息交换消息";
    }

    /**
     * 消息体。
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
         * 920A：上报驾驶员身份信息请求数据。
         */
        private DriverInfoRequestData driverInfoRequestData;

        @Override
        public byte[] encode() {
            byte[] dataBytes;
            if (subBusinessType == SUB_BUSINESS_TYPE_920A && driverInfoRequestData != null) {
                dataBytes = driverInfoRequestData.encode();
            } else {
                dataBytes = new byte[0];
            }

            ByteBuf buf = Unpooled.buffer(28 + dataBytes.length);
            ByteBufUtil.writeString(buf, vehicleNo, 21);
            buf.writeByte(vehicleColor);
            buf.writeShort(subBusinessType);
            buf.writeInt(dataBytes.length);
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
            vehicleNo = ByteBufUtil.readString(buf, 21);
            vehicleColor = buf.readUnsignedByte();
            subBusinessType = buf.readUnsignedShort();
            subsequentDataLength = buf.readInt();

            if (subsequentDataLength > 0 && buf.isReadable(subsequentDataLength)) {
                byte[] dataBytes = new byte[subsequentDataLength];
                buf.readBytes(dataBytes);

                if (subBusinessType == SUB_BUSINESS_TYPE_920A) {
                    driverInfoRequestData = new DriverInfoRequestData();
                    driverInfoRequestData.decode(dataBytes);
                } else {
                    log.warn("Unsupported Sub Business Type: 0x{}",
                            Integer.toHexString(subBusinessType).toUpperCase());
                }
            }

            buf.release();
        }
    }

    /**
     * 920A：上报驾驶员身份信息请求数据。
     */
    @Data
    public static class DriverInfoRequestData {

        /**
         * 上传标志。0x00：最近收到的消息；0x01：从终端获取。
         */
        private int flag;

        public byte[] encode() {
            ByteBuf buf = Unpooled.buffer(1);
            buf.writeByte(flag);

            byte[] result = new byte[buf.readableBytes()];
            buf.readBytes(result);
            buf.release();

            return result;
        }

        public void decode(byte[] data) {
            ByteBuf buf = Unpooled.wrappedBuffer(data);
            flag = buf.readUnsignedByte();
            buf.release();
        }
    }
}
