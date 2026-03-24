package org.iot.v.jt809.example.handler;

import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.handler.annotation.JT809Handler;
import org.iot.v.jt809.handler.builtin.AbstractMessageHandler;
import org.iot.v.jt809.handler.context.MessageContext;

/**
 * 自定义报警数据处理器
 * 处理车辆报警消息(0x1401)
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
@JT809Handler(messageTypes = {MessageType.VEHICLE_ALARM}, order = 20)
public class CustomAlarmHandler extends AbstractMessageHandler {
    
    @Override
    protected boolean doHandle(MessageContext context, BaseMessage message) {
        log.info("处理车辆报警消息: msgId=0x{}", Integer.toHexString(message.getMsgId()));
        
        // TODO: 实现报警数据处理逻辑
        // VehicleAlarmMsg alarmMsg = (VehicleAlarmMsg) message;
        // ...
        
        // 返回true继续执行后续处理器
        return true;
    }
    
    @Override
    public int[] supportedMessageTypes() {
        return new int[]{MessageType.VEHICLE_ALARM};
    }
}
