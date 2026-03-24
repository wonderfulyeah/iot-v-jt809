package org.iot.v.jt809.handler;

import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.handler.context.MessageContext;

/**
 * 消息处理器接口
 * 所有自定义消息处理器都需要实现此接口
 *
 * @author haye
 * @date 2026-03-24
 */
public interface MessageHandler {
    
    /**
     * 处理消息
     *
     * @param context 消息上下文
     * @param message 消息对象
     * @return true-继续执行后续处理器，false-终止处理链
     */
    boolean handle(MessageContext context, BaseMessage message);
    
    /**
     * 支持的消息类型
     * 返回此处理器支持处理的消息ID数组
     *
     * @return 消息ID数组
     */
    int[] supportedMessageTypes();
    
    /**
     * 处理器顺序
     * 数值越小优先级越高
     *
     * @return 顺序值
     */
    default int getOrder() {
        return 0;
    }
    
    /**
     * 处理器名称
     *
     * @return 名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * 是否启用
     *
     * @return true-启用，false-禁用
     */
    default boolean isEnabled() {
        return true;
    }
}
