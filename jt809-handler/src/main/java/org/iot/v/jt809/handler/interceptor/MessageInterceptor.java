package org.iot.v.jt809.handler.interceptor;

import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.handler.context.MessageContext;

/**
 * 消息拦截器接口
 * 用于在消息处理前后进行拦截处理
 *
 * @author haye
 * @date 2026-03-24
 */
public interface MessageInterceptor {
    
    /**
     * 消息处理前拦截
     *
     * @param context 消息上下文
     * @param message 消息对象
     * @return true-继续处理，false-终止处理
     */
    default boolean beforeHandle(MessageContext context, BaseMessage message) {
        return true;
    }
    
    /**
     * 消息处理后拦截
     *
     * @param context 消息上下文
     * @param message 消息对象
     */
    default void afterHandle(MessageContext context, BaseMessage message) {
    }
    
    /**
     * 异常处理
     *
     * @param context 消息上下文
     * @param message 消息对象
     * @param e 异常
     */
    default void onError(MessageContext context, BaseMessage message, Exception e) {
    }
    
    /**
     * 拦截器顺序
     * 数值越小优先级越高
     *
     * @return 顺序值
     */
    default int getOrder() {
        return 0;
    }
}
