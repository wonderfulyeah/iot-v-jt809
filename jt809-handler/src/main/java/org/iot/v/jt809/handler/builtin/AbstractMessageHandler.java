package org.iot.v.jt809.handler.builtin;

import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.handler.MessageHandler;
import org.iot.v.jt809.handler.context.MessageContext;

/**
 * 抽象消息处理器
 * 提供处理器的基础实现
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
public abstract class AbstractMessageHandler implements MessageHandler {
    
    /**
     * 处理器名称
     */
    protected String name = this.getClass().getSimpleName();
    
    /**
     * 处理器顺序
     */
    protected int order = 0;
    
    /**
     * 是否启用
     */
    protected boolean enabled = true;
    
    @Override
    public boolean handle(MessageContext context, BaseMessage message) {
        try {
            return doHandle(context, message);
        } catch (Exception e) {
            log.error("Handler [{}] failed to process message: msgId=0x{}", 
                name, Integer.toHexString(message.getMsgId()), e);
            return false;
        }
    }
    
    /**
     * 实际处理逻辑
     *
     * @param context 消息上下文
     * @param message 消息对象
     * @return true-继续执行后续处理器，false-终止处理链
     */
    protected abstract boolean doHandle(MessageContext context, BaseMessage message);
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public int getOrder() {
        return order;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 设置处理器名称
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 设置处理器顺序
     *
     * @param order 顺序
     */
    public void setOrder(int order) {
        this.order = order;
    }
    
    /**
     * 设置是否启用
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
