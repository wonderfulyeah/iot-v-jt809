package org.iot.v.jt809.core.message.upstream.vehicle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.base.MessageBody;
import org.iot.v.jt809.core.util.ByteBufUtil;

import java.time.Instant;

/**
 * 报警信息交互消息（消息ID: 0x1400）
 * 主链路报警信息交互消息，子业务类型1402：上报报警信息消息
 *
 * @author haye
 * @date 2026-04-13
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class AlarmInfoMsg extends BaseMessage {

    /**
     * 子业务类型标识：上报报警信息消息
     */
    public static final int SUB_BUSINESS_TYPE_1402 = 0x1402;
    
    /**
     * 子业务类型标识：主动上报报警处理结果消息
     */
    public static final int SUB_BUSINESS_TYPE_1412 = 0x1412;

    public AlarmInfoMsg() {
        setMsgId(MessageType.ALARM_INFO_INTERACTION);
        setBody(new Body());
    }

    @Override
    public String getMessageTypeName() {
        return "报警信息交互消息";
    }

    /**
     * 消息体
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Body extends MessageBody {

        /**
         * 子业务类型标识（2字节）
         */
        private int subBusinessType;

        /**
         * 后续数据长度（4字节）
         */
        private int subsequentDataLength;

        /**
         * 报警数据（1402）
         */
        private AlarmReportData alarmReportData;
        
        /**
         * 报警处理结果数据（1412）
         */
        private AlarmResultData alarmResultData;

        @Override
        public byte[] encode() {
            byte[] dataBytes;
            if (subBusinessType == SUB_BUSINESS_TYPE_1402 && alarmReportData != null) {
                dataBytes = alarmReportData.encode();
            } else if (subBusinessType == SUB_BUSINESS_TYPE_1412 && alarmResultData != null) {
                dataBytes = alarmResultData.encode();
            } else {
                dataBytes = new byte[0];
            }
            
            int totalLength = 6 + dataBytes.length;

            ByteBuf buf = Unpooled.buffer(totalLength);

            // 子业务类型标识 (2字节)
            buf.writeShort(subBusinessType);

            // 后续数据长度 (4字节)
            buf.writeInt(dataBytes.length);

            // 报警数据
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

            // 子业务类型标识 (2字节)
            subBusinessType = buf.readUnsignedShort();

            // 后续数据长度 (4字节)
            subsequentDataLength = buf.readInt();

            // 根据子业务类型解码
            byte[] dataBytes = new byte[subsequentDataLength];
            buf.readBytes(dataBytes);

            if (subBusinessType == SUB_BUSINESS_TYPE_1402) {
                alarmReportData = new AlarmReportData();
                alarmReportData.decode(dataBytes);
            } else if (subBusinessType == SUB_BUSINESS_TYPE_1412) {
                alarmResultData = new AlarmResultData();
                alarmResultData.decode(dataBytes);
            } else {
                log.error("Unsupported Sub Message type:{}", Integer.toHexString(subBusinessType));
            }

            buf.release();
        }
    }

    /**
     * 1402：上报报警信息数据
     */
    @Data
    public static class AlarmReportData {

        /**
         * 发起报警平台唯一编码（12字节）
         */
        private String sourcePlatformId;

        /**
         * 报警类型（1字节）
         * 1: 超速报警
         * 2: 疲劳驾驶报警
         * 等...
         */
        private int alarmType;

        /**
         * 报警时间（8字节，UTC时间戳）
         */
        private Instant alarmTime;

        /**
         * 事件开始时间（8字节，UTC时间戳）
         */
        private Instant eventStartTime;

        /**
         * 事件结束时间（8字节，UTC时间戳）
         */
        private Instant eventEndTime;

        /**
         * 车牌号码（21字节）
         */
        private String vehicleNo;

        /**
         * 车牌颜色（1字节）
         * 1: 白色
         * 2: 黄色
         * 3: 黑色
         * 4: 蓝色
         * 等...
         */
        private int vehicleColor;

        /**
         * 被报警平台唯一编码（11字节）
         */
        private String targetPlatformId;

        /**
         * 线路ID（4字节）
         */
        private int lineId;

        /**
         * 信息内容长度（4字节）
         */
        private int infoLength;

        /**
         * 信息内容（变长）
         */
        private String infoContent;

        /**
         * 数据总长度（用于计算各字段长度）
         */
        private int dataLength;

        public byte[] encode() {
            byte[] contentBytes = infoContent != null ? infoContent.getBytes(java.nio.charset.Charset.forName("GBK")) : new byte[0];
            int totalLength = 12 + 1 + 8 + 8 + 8 + 21 + 1 + 11 + 4 + 4 + contentBytes.length;

            ByteBuf buf = Unpooled.buffer(totalLength);

            // 发起报警平台唯一编码 (12字节)
            ByteBufUtil.writeString(buf, sourcePlatformId, 12);

            // 报警类型 (1字节)
            buf.writeByte(alarmType);

            // 报警时间 (8字节)
            buf.writeLong(alarmTime.getEpochSecond());

            // 事件开始时间 (8字节)
            buf.writeLong(eventStartTime.getEpochSecond());

            // 事件结束时间 (8字节)
            buf.writeLong(eventEndTime.getEpochSecond());

            // 车牌号码 (21字节)
            ByteBufUtil.writeString(buf, vehicleNo, 21);

            // 车牌颜色 (1字节)
            buf.writeByte(vehicleColor);

            // 被报警平台唯一编码 (11字节)
            ByteBufUtil.writeString(buf, targetPlatformId, 11);

            // 线路ID (4字节)
            buf.writeInt(lineId);

            // 信息内容长度 (4字节)
            buf.writeInt(contentBytes.length);

            // 信息内容
            if (contentBytes.length > 0) {
                buf.writeBytes(contentBytes);
            }

            byte[] result = new byte[buf.readableBytes()];
            buf.readBytes(result);
            buf.release();

            return result;
        }

        public void decode(byte[] data) {
            ByteBuf buf = Unpooled.wrappedBuffer(data);
            dataLength = data.length;

            // 读取发起报警平台唯一编码（12字节，含1字节填充）
            sourcePlatformId = ByteBufUtil.readString(buf, 12);

            // 报警类型 (1字节)
            alarmType = buf.readUnsignedByte();

            // 报警时间 (8字节)
            alarmTime = Instant.ofEpochSecond(buf.readLong());

            // 事件开始时间 (8字节)
            eventStartTime = Instant.ofEpochSecond(buf.readLong());

            // 事件结束时间 (8字节)
            eventEndTime = Instant.ofEpochSecond(buf.readLong());

            // 读取车牌号码（21字节）
            vehicleNo = ByteBufUtil.readString(buf, 21);

            // 车牌颜色 (1字节)
            vehicleColor = buf.readUnsignedByte();

            // 读取被报警平台唯一编码（11字节）
            targetPlatformId = ByteBufUtil.readString(buf, 11);

            // 线路ID (4字节)
            lineId = buf.readInt();

            // 信息内容长度 (4字节)
            infoLength = buf.readInt();

            // 信息内容
            if (infoLength > 0 && buf.isReadable(infoLength)) {
                byte[] contentBytes = new byte[infoLength];
                buf.readBytes(contentBytes);
                infoContent = new String(contentBytes, java.nio.charset.Charset.forName("GBK"));
            }

            buf.release();
        }
    }

    /**
     * 1412：主动上报报警处理结果数据
     */
    @Data
    public static class AlarmResultData {

        /**
         * 对应启动车辆定位信息交换请求消息源子业务类型标识（2字节）
         */
        private int sourceSubBusinessType;

        /**
         * 对应启动车辆定位信息交换请求消息源报文序列号（4字节）
         */
        private int sourceSeqNo;

        /**
         * 报警处理结果（1字节）
         * 0: 未处理
         * 1: 已处理完毕
         */
        private int alarmResult;

        public byte[] encode() {
            ByteBuf buf = Unpooled.buffer(7);

            // 源子业务类型标识 (2字节)
            buf.writeShort(sourceSubBusinessType);

            // 源报文序列号 (4字节)
            buf.writeInt(sourceSeqNo);

            // 报警处理结果 (1字节)
            buf.writeByte(alarmResult);

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

            // 报警处理结果 (1字节)
            alarmResult = buf.readUnsignedByte();

            buf.release();
        }
    }
}