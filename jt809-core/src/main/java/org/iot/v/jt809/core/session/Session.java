package org.iot.v.jt809.core.session;

import io.netty.channel.Channel;
import lombok.Data;
import org.iot.v.jt809.core.constant.SessionState;
import org.iot.v.jt809.core.message.base.BaseMessage;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话对象
 * 表示一个连接会话
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
public class Session {
    
    /**
     * 会话ID（使用Channel的长ID）
     */
    private String sessionId;
    
    /**
     * Netty通道
     */
    private Channel channel;
    
    /**
     * 平台ID
     */
    private long platformId;
    
    /**
     * 会话状态
     */
    private SessionState state;
    
    /**
     * 登录时间
     */
    private Instant loginTime;
    
    /**
     * 最后活跃时间
     */
    private Instant lastActiveTime;
    
    /**
     * 扩展属性
     */
    private Map<String, Object> attributes = new ConcurrentHashMap<>();
    
    /**
     * 校验码（登录成功后由上级平台返回）
     */
    private long verifyCode;
    
    /**
     * 是否已认证
     */
    private boolean authenticated;
    
    /**
     * 错误计数
     */
    private int errorCount;
    
    public Session() {
        this.state = SessionState.CONNECTED;
        this.lastActiveTime = Instant.now();
    }
    
    public Session(Channel channel) {
        this();
        this.channel = channel;
        this.sessionId = channel.id().asLongText();
    }
    
    /**
     * 发送消息
     *
     * @param message 消息对象
     */
    public void send(BaseMessage message) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(message);
            updateLastActiveTime();
        }
    }
    
    /**
     * 关闭会话
     */
    public void close() {
        if (channel != null) {
            channel.close();
        }
        this.state = SessionState.DISCONNECTED;
    }
    
    /**
     * 更新最后活跃时间
     */
    public void updateLastActiveTime() {
        this.lastActiveTime = Instant.now();
    }
    
    /**
     * 检查会话是否活跃
     *
     * @return true-活跃, false-不活跃
     */
    public boolean isActive() {
        return channel != null && channel.isActive();
    }
    
    /**
     * 设置属性
     *
     * @param key 键
     * @param value 值
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    /**
     * 获取属性
     *
     * @param key 键
     * @return 值
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    /**
     * 移除属性
     *
     * @param key 键
     */
    public void removeAttribute(String key) {
        attributes.remove(key);
    }
    
    /**
     * 获取远程地址
     *
     * @return 远程地址字符串
     */
    public String getRemoteAddress() {
        if (channel != null && channel.remoteAddress() != null) {
            return channel.remoteAddress().toString();
        }
        return "unknown";
    }
    
    /**
     * 增加错误计数
     */
    public void incrementErrorCount() {
        this.errorCount++;
    }
    
    /**
     * 重置错误计数
     */
    public void resetErrorCount() {
        this.errorCount = 0;
    }
}
