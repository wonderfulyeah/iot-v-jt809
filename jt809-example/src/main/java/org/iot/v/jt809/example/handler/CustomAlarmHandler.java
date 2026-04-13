package org.iot.v.jt809.example.handler;

import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.upstream.vehicle.AlarmInfoMsg;
import org.iot.v.jt809.handler.annotation.JT809Handler;
import org.iot.v.jt809.handler.builtin.AbstractMessageHandler;
import org.iot.v.jt809.handler.context.MessageContext;

/**
 * 自定义报警数据处理器
 * 处理报警信息交互消息(0x1400)
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
@JT809Handler(messageTypes = {MessageType.ALARM_INFO_INTERACTION}, order = 20)
public class CustomAlarmHandler extends AbstractMessageHandler {
    
    @Override
    protected boolean doHandle(MessageContext context, BaseMessage message) {
        log.info("处理报警信息消息: msgId=0x{}", Integer.toHexString(message.getMsgId()));
        
        AlarmInfoMsg alarmMsg = (AlarmInfoMsg) message;
        AlarmInfoMsg.Body body = (AlarmInfoMsg.Body) alarmMsg.getBody();
        if (body != null && body.getAlarmReportData() != null) {
            AlarmInfoMsg.AlarmReportData data = body.getAlarmReportData();
            log.info("报警详情: 子业务类型=0x{}, 平台编码={}, 报警类型={}, 车牌={}, 信息内容={}",
                Integer.toHexString(body.getSubBusinessType()),
                data.getSourcePlatformId(),
                data.getAlarmType(),
                data.getVehicleNo(),
                data.getInfoContent());
        }
        
        // 返回true继续执行后续处理器
        return true;
    }
    
    @Override
    public int[] supportedMessageTypes() {
        return new int[]{MessageType.ALARM_INFO_INTERACTION};
    }
}