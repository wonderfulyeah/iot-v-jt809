package org.iot.v.jt809.handler.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.handler.context.MessageContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 监控拦截器
 * 收集消息处理的监控指标
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
public class MetricInterceptor implements MessageInterceptor {
    
    /**
     * 消息计数器（按消息类型）
     */
    private final Map<Integer, AtomicLong> messageCounters = new ConcurrentHashMap<>();
    
    /**
     * 总处理时间（按消息类型）
     */
    private final Map<Integer, AtomicLong> processingTimes = new ConcurrentHashMap<>();
    
    /**
     * 错误计数器
     */
    private final AtomicLong errorCounter = new AtomicLong(0);
    
    @Override
    public boolean beforeHandle(MessageContext context, BaseMessage message) {
        return true;
    }
    
    @Override
    public void afterHandle(MessageContext context, BaseMessage message) {
        int msgId = message.getMsgId();
        long elapsed = context.getElapsed();
        
        // 更新计数器
        messageCounters.computeIfAbsent(msgId, k -> new AtomicLong(0)).incrementAndGet();
        processingTimes.computeIfAbsent(msgId, k -> new AtomicLong(0)).addAndGet(elapsed);
    }
    
    @Override
    public void onError(MessageContext context, BaseMessage message, Exception e) {
        errorCounter.incrementAndGet();
        
        log.warn("[METRIC] Error occurred: msgId=0x{}, totalErrors={}",
            Integer.toHexString(message.getMsgId()), errorCounter.get());
    }
    
    @Override
    public int getOrder() {
        return Integer.MIN_VALUE + 1; // 日志拦截器之后
    }
    
    /**
     * 获取消息处理次数
     *
     * @param msgId 消息ID
     * @return 处理次数
     */
    public long getMessageCount(int msgId) {
        AtomicLong counter = messageCounters.get(msgId);
        return counter != null ? counter.get() : 0;
    }
    
    /**
     * 获取平均处理时间
     *
     * @param msgId 消息ID
     * @return 平均处理时间（毫秒）
     */
    public double getAverageProcessingTime(int msgId) {
        AtomicLong counter = messageCounters.get(msgId);
        AtomicLong totalTime = processingTimes.get(msgId);
        
        if (counter == null || totalTime == null || counter.get() == 0) {
            return 0;
        }
        
        return (double) totalTime.get() / counter.get();
    }
    
    /**
     * 获取错误总数
     *
     * @return 错误总数
     */
    public long getErrorCount() {
        return errorCounter.get();
    }
    
    /**
     * 重置所有计数器
     */
    public void reset() {
        messageCounters.clear();
        processingTimes.clear();
        errorCounter.set(0);
    }
    
    /**
     * 打印统计信息
     */
    public void printStatistics() {
        log.info("=== Message Processing Statistics ===");
        log.info("Total errors: {}", errorCounter.get());
        
        messageCounters.forEach((msgId, counter) -> {
            AtomicLong totalTime = processingTimes.get(msgId);
            double avgTime = totalTime != null && counter.get() > 0 
                ? (double) totalTime.get() / counter.get() : 0;
            
            log.info("Message 0x{}: count={}, avgTime={}ms",
                Integer.toHexString(msgId), counter.get(), String.format("%.2f", avgTime));
        });
    }
}
