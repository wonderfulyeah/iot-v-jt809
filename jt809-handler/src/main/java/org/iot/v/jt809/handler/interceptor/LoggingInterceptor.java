package org.iot.v.jt809.handler.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.handler.context.MessageContext;

/**
 * 日志拦截器
 * 记录消息处理的日志信息
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
public class LoggingInterceptor implements MessageInterceptor {
    
    /**
     * 是否记录详细日志
     */
    private boolean verbose = false;
    
    @Override
    public boolean beforeHandle(MessageContext context, BaseMessage message) {
        if (log.isDebugEnabled()) {
            log.debug("[LOG] Before handle: msgId=0x{}, platformId={}, sessionId={}",
                Integer.toHexString(message.getMsgId()),
                context.getPlatformId(),
                context.getSession() != null ? context.getSession().getSessionId() : "N/A");
            
            if (verbose) {
                log.debug("[LOG] Message detail: {}", message);
            }
        }
        return true;
    }
    
    @Override
    public void afterHandle(MessageContext context, BaseMessage message) {
        if (log.isDebugEnabled()) {
            log.debug("[LOG] After handle: msgId=0x{}, elapsed={}ms",
                Integer.toHexString(message.getMsgId()),
                context.getElapsed());
        }
    }
    
    @Override
    public void onError(MessageContext context, BaseMessage message, Exception e) {
        log.error("[LOG] Error handling message: msgId=0x{}, error={}",
            Integer.toHexString(message.getMsgId()), e.getMessage(), e);
    }
    
    @Override
    public int getOrder() {
        return Integer.MIN_VALUE; // 最先执行
    }
    
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
