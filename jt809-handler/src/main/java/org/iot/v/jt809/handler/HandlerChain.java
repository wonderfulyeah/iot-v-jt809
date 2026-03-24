package org.iot.v.jt809.handler;

import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.handler.context.MessageContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 消息处理器链
 * 按顺序执行所有注册的处理器
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
public class HandlerChain {
    
    /**
     * 处理器列表（线程安全）
     */
    private final List<MessageHandler> handlers = new CopyOnWriteArrayList<>();
    
    /**
     * 添加处理器
     *
     * @param handler 处理器
     */
    public void addHandler(MessageHandler handler) {
        if (handler == null) {
            return;
        }
        
        handlers.add(handler);
        // 按顺序排序
        handlers.sort(Comparator.comparingInt(MessageHandler::getOrder));
        
        log.info("Added message handler: {}, order: {}", 
            handler.getName(), handler.getOrder());
    }
    
    /**
     * 移除处理器
     *
     * @param handler 处理器
     */
    public void removeHandler(MessageHandler handler) {
        if (handler == null) {
            return;
        }
        
        handlers.remove(handler);
        log.info("Removed message handler: {}", handler.getName());
    }
    
    /**
     * 处理消息
     * 按顺序执行所有匹配的处理器
     *
     * @param context 消息上下文
     * @param message 消息对象
     */
    public void process(MessageContext context, BaseMessage message) {
        if (handlers.isEmpty()) {
            log.debug("No handlers registered, skipping message processing");
            return;
        }
        
        int msgId = message.getMsgId();
        log.debug("Processing message: msgId=0x{}, handlers={}", 
            Integer.toHexString(msgId), handlers.size());
        
        for (MessageHandler handler : handlers) {
            // 检查处理器是否启用
            if (!handler.isEnabled()) {
                continue;
            }
            
            // 检查是否支持该消息类型
            if (!isSupported(handler, msgId)) {
                continue;
            }
            
            try {
                long startTime = System.currentTimeMillis();
                
                // 执行处理器
                boolean continueChain = handler.handle(context, message);
                
                long elapsed = System.currentTimeMillis() - startTime;
                log.debug("Handler {} executed in {}ms", handler.getName(), elapsed);
                
                // 如果返回false，终止处理链
                if (!continueChain) {
                    log.debug("Handler chain stopped by: {}", handler.getName());
                    break;
                }
                
            } catch (Exception e) {
                log.error("Handler {} failed: {}", handler.getName(), e.getMessage(), e);
                
                // 设置异常到上下文
                context.setException(e);
                
                // 可以根据需要决定是否继续执行后续处理器
                // 这里选择继续执行
            }
        }
    }
    
    /**
     * 检查处理器是否支持指定消息类型
     *
     * @param handler 处理器
     * @param msgId 消息ID
     * @return true-支持，false-不支持
     */
    private boolean isSupported(MessageHandler handler, int msgId) {
        int[] supportedTypes = handler.supportedMessageTypes();
        if (supportedTypes == null || supportedTypes.length == 0) {
            // 空数组表示支持所有消息类型
            return true;
        }
        
        return Arrays.stream(supportedTypes).anyMatch(type -> type == msgId);
    }
    
    /**
     * 获取所有处理器
     *
     * @return 处理器列表
     */
    public List<MessageHandler> getHandlers() {
        return new ArrayList<>(handlers);
    }
    
    /**
     * 清空所有处理器
     */
    public void clear() {
        handlers.clear();
        log.info("All handlers cleared");
    }
    
    /**
     * 获取处理器数量
     *
     * @return 数量
     */
    public int size() {
        return handlers.size();
    }
}
