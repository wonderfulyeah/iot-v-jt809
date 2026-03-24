package org.iot.v.jt809.example.handler;

import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.handler.annotation.JT809Handler;
import org.iot.v.jt809.handler.builtin.AbstractMessageHandler;
import org.iot.v.jt809.handler.context.MessageContext;

/**
 * 自定义车辆数据处理器
 * 处理车辆定位消息(0x1200)
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
@JT809Handler(messageTypes = {MessageType.VEHICLE_LOCATION}, order = 10)
public class CustomVehicleHandler extends AbstractMessageHandler {
    
    @Override
    protected boolean doHandle(MessageContext context, BaseMessage message) {
        log.info("处理车辆定位消息: msgId=0x{}", Integer.toHexString(message.getMsgId()));
        
        // TODO: 实现车辆定位数据处理逻辑
        // VehicleLocationMsg locationMsg = (VehicleLocationMsg) message;
        // String vehicleNo = locationMsg.getBody().getVehicleNo();
        // ...
        
        // 返回true继续执行后续处理器
        return true;
    }
    
    @Override
    public int[] supportedMessageTypes() {
        return new int[]{MessageType.VEHICLE_LOCATION};
    }
}
