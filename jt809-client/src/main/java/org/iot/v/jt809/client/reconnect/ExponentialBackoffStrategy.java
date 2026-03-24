package org.iot.v.jt809.client.reconnect;

import lombok.extern.slf4j.Slf4j;

/**
 * 指数退避重连策略
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
public class ExponentialBackoffStrategy implements ReconnectStrategy {
    
    /**
     * 初始延迟（毫秒）
     */
    private final long initialDelay;
    
    /**
     * 最大延迟（毫秒）
     */
    private final long maxDelay;
    
    /**
     * 乘数因子
     */
    private final double multiplier;
    
    /**
     * 最大重连次数
     */
    private final int maxAttempts;
    
    /**
     * 当前延迟
     */
    private long currentDelay;
    
    public ExponentialBackoffStrategy() {
        this(1000, 60000, 2.0, -1);
    }
    
    public ExponentialBackoffStrategy(long initialDelay, long maxDelay, double multiplier, int maxAttempts) {
        this.initialDelay = initialDelay;
        this.maxDelay = maxDelay;
        this.multiplier = multiplier;
        this.maxAttempts = maxAttempts;
        this.currentDelay = initialDelay;
    }
    
    @Override
    public long getNextDelay(int attemptCount) {
        if (!shouldRetry(attemptCount)) {
            return -1;
        }
        
        long delay = currentDelay;
        currentDelay = Math.min((long) (currentDelay * multiplier), maxDelay);
        
        log.debug("Next reconnect delay: {}ms, attempt: {}", delay, attemptCount);
        return delay;
    }
    
    @Override
    public void reset() {
        this.currentDelay = initialDelay;
        log.debug("Reconnect strategy reset");
    }
    
    @Override
    public boolean shouldRetry(int attemptCount) {
        if (maxAttempts < 0) {
            return true; // 无限重连
        }
        return attemptCount < maxAttempts;
    }
    
    /**
     * 创建默认策略
     */
    public static ExponentialBackoffStrategy createDefault() {
        return new ExponentialBackoffStrategy(1000, 60000, 2.0, -1);
    }
    
    /**
     * 创建有限次数策略
     */
    public static ExponentialBackoffStrategy createLimited(int maxAttempts) {
        return new ExponentialBackoffStrategy(1000, 60000, 2.0, maxAttempts);
    }
}
