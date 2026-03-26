package org.iot.v.jt809.example.handler;

import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.upstream.vehicle.VehicleAlarmMsg;
import org.iot.v.jt809.core.message.upstream.vehicle.VehicleLocationMsg;
import org.iot.v.jt809.handler.annotation.JT809Handler;
import org.iot.v.jt809.handler.builtin.AbstractMessageHandler;
import org.iot.v.jt809.handler.context.MessageContext;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 测试用的车辆定位消息处理器
 * 用于验证 @JT809Handler 注解功能
 *
 * @author haye
 * @date 2026-03-26
 */
@Slf4j
@JT809Handler(messageTypes = {MessageType.VEHICLE_LOCATION}, order = 10)
public class TestVehicleLocationHandler extends AbstractMessageHandler {

    private static final AtomicInteger messageCount = new AtomicInteger(0);
    private static final AtomicReference<BaseMessage> lastMessage = new AtomicReference<>();

    @Override
    protected boolean doHandle(MessageContext context, BaseMessage message) {
        int count = messageCount.incrementAndGet();
        lastMessage.set(message);
        
        VehicleLocationMsg locationMsg = (VehicleLocationMsg) message;
        VehicleLocationMsg.Body body = (VehicleLocationMsg.Body) locationMsg.getBody();
        
        log.info("[TestVehicleLocationHandler] 处理车辆定位消息 #{}: msgId=0x{}, 车牌={}",
            count, Integer.toHexString(message.getMsgId()), 
            body != null ? body.getVehicleNo() : "N/A");
        
        return true;
    }

    @Override
    public int[] supportedMessageTypes() {
        return new int[]{MessageType.VEHICLE_LOCATION};
    }

    public static int getMessageCount() {
        return messageCount.get();
    }

    public static BaseMessage getLastMessage() {
        return lastMessage.get();
    }

    public static void reset() {
        messageCount.set(0);
        lastMessage.set(null);
    }
}
