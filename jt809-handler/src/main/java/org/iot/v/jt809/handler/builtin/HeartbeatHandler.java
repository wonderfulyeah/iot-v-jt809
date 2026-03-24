package org.iot.v.jt809.handler.builtin;

import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.upstream.UpLinkTestReq;
import org.iot.v.jt809.core.message.upstream.UpLinkTestResp;
import org.iot.v.jt809.core.message.downstream.DownLinkTestReq;
import org.iot.v.jt809.core.message.downstream.DownLinkTestResp;
import org.iot.v.jt809.core.session.Session;
import org.iot.v.jt809.handler.MessageHandler;
import org.iot.v.jt809.handler.context.MessageContext;

/**
 * 内置心跳处理器
 * 处理心跳请求并返回响应
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
public class HeartbeatHandler implements MessageHandler {
    
    @Override
    public int[] supportedMessageTypes() {
        return new int[] {
            MessageType.UP_LINK_TEST_REQ,
            MessageType.DOWN_LINK_TEST_REQ
        };
    }
    
    @Override
    public boolean handle(MessageContext context, BaseMessage message) {
        int msgId = message.getMsgId();
        
        // 更新会话最后活动时间
        Session session = context.getSession();
        if (session != null) {
            session.updateLastActiveTime();
        }
        
        // 根据消息类型返回相应响应
        BaseMessage response = null;
        if (msgId == MessageType.UP_LINK_TEST_REQ) {
            log.debug("Received up link test request from platform: {}", 
                session != null ? session.getPlatformId() : "unknown");
            response = new UpLinkTestResp();
        } else if (msgId == MessageType.DOWN_LINK_TEST_REQ) {
            log.debug("Received down link test request");
            response = new DownLinkTestResp();
        }
        
        // 发送响应
        if (response != null && session != null) {
            session.send(response);
        }
        
        return true; // 继续执行后续处理器
    }
    
    @Override
    public int getOrder() {
        return Integer.MIN_VALUE; // 最高优先级
    }
    
    @Override
    public String getName() {
        return "HeartbeatHandler";
    }
}
