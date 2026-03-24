package org.iot.v.jt809.client.reconnect;

/**
 * 重连策略接口
 *
 * @author haye
 * @date 2026-03-24
 */
public interface ReconnectStrategy {
    
    /**
     * 获取下次重连的延迟时间（毫秒）
     *
     * @param attemptCount 当前尝试次数
     * @return 延迟时间（毫秒），返回-1表示不再重连
     */
    long getNextDelay(int attemptCount);
    
    /**
     * 重置策略（连接成功后调用）
     */
    void reset();
    
    /**
     * 是否应该继续重连
     *
     * @param attemptCount 当前尝试次数
     * @return true-继续重连, false-停止重连
     */
    boolean shouldRetry(int attemptCount);
}
