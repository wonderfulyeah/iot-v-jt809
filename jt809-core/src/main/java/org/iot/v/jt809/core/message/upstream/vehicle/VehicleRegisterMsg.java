package org.iot.v.jt809.core.message.upstream.vehicle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.base.MessageBody;
import org.iot.v.jt809.core.util.ByteBufUtil;

/**
 * 车辆注册信息消息（消息ID: 0x1601）
 * 下级平台上报车辆注册信息
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleRegisterMsg extends BaseMessage {
    
    public VehicleRegisterMsg() {
        setMsgId(MessageType.VEHICLE_REGISTER);
        setBody(new Body());
    }
    
    @Override
    public String getMessageTypeName() {
        return "车辆注册信息";
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
         * 注册数据
         */
        private RegisterData registerData;
        
        @Override
        public byte[] encode() {
            byte[] registerBytes = registerData != null ? registerData.encode() : new byte[0];
            int totalLength = 28 + registerBytes.length;
            
            ByteBuf buf = Unpooled.buffer(totalLength);
            
            // 车牌号码 (21字节)
            ByteBufUtil.writeString(buf, vehicleNo, 21);
            
            // 车牌颜色 (1字节)
            buf.writeByte(vehicleColor);
            
            // 子业务类型标识 (2字节)
            buf.writeShort(subBusinessType);
            
            // 后续数据长度 (4字节)
            buf.writeInt(registerBytes.length);
            
            // 注册数据
            if (registerBytes.length > 0) {
                buf.writeBytes(registerBytes);
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
            
            // 注册数据
            if (subsequentDataLength > 0 && buf.isReadable(subsequentDataLength)) {
                byte[] registerBytes = new byte[subsequentDataLength];
                buf.readBytes(registerBytes);
                registerData = new RegisterData();
                registerData.decode(registerBytes);
            }
            
            buf.release();
        }
    }
    
    /**
     * 注册数据
     */
    @Data
    public static class RegisterData {
        
        /**
         * 平台唯一识别码（11字节）
         */
        private String platformId;
        
        /**
         * 终端制造商ID（5字节）
         */
        private String manufacturerId;
        
        /**
         * 终端型号（20字节）
         */
        private String terminalModel;
        
        /**
         * 终端ID（7字节）
         */
        private String terminalId;
        
        /**
         * 终端SIM卡电话号码（12字节）
         */
        private String simNumber;
        
        public byte[] encode() {
            ByteBuf buf = Unpooled.buffer(55);
            
            // 平台唯一识别码 (11字节)
            ByteBufUtil.writeString(buf, platformId, 11);
            
            // 终端制造商ID (5字节)
            ByteBufUtil.writeString(buf, manufacturerId, 5);
            
            // 终端型号 (20字节)
            ByteBufUtil.writeString(buf, terminalModel, 20);
            
            // 终端ID (7字节)
            ByteBufUtil.writeString(buf, terminalId, 7);
            
            // 终端SIM卡电话号码 (12字节)
            ByteBufUtil.writeString(buf, simNumber, 12);
            
            byte[] result = new byte[buf.readableBytes()];
            buf.readBytes(result);
            buf.release();
            
            return result;
        }
        
        public void decode(byte[] data) {
            ByteBuf buf = Unpooled.wrappedBuffer(data);
            
            // 平台唯一识别码 (11字节)
            platformId = ByteBufUtil.readString(buf, 11);
            
            // 终端制造商ID (5字节)
            manufacturerId = ByteBufUtil.readString(buf, 5);
            
            // 终端型号 (20字节)
            terminalModel = ByteBufUtil.readString(buf, 20);
            
            // 终端ID (7字节)
            terminalId = ByteBufUtil.readString(buf, 7);
            
            // 终端SIM卡电话号码 (12字节)
            simNumber = ByteBufUtil.readString(buf, 12);
            
            buf.release();
        }
    }
}
