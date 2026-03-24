package org.iot.v.jt809.handler.context;

import lombok.Data;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.session.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息上下文
 * 封装消息处理过程中的所有相关信息
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
public class MessageContext {
    
    /**
     * 会话信息
     */
    private Session session;
    
    /**
     * 请求消息
     */
    private BaseMessage request;
    
    /**
     * 响应消息
     */
    private BaseMessage response;
    
    /**
     * 异常信息
     */
    private Exception exception;
    
    /**
     * 扩展属性
     */
    private Map<String, Object> attributes = new ConcurrentHashMap<>();
    
    /**
     * 处理开始时间
     */
    private long startTime = System.currentTimeMillis();
    
    public MessageContext() {
    }
    
    public MessageContext(Session session, BaseMessage request) {
        this.session = session;
        this.request = request;
    }
    
    /**
     * 发送响应
     *
     * @param response 响应消息
     */
    public void sendResponse(BaseMessage response) {
        this.response = response;
        if (session != null) {
            session.send(response);
        }
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
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
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
     * 获取处理耗时
     *
     * @return 耗时（毫秒）
     */
    public long getElapsed() {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * 获取消息ID
     *
     * @return 消息ID
     */
    public int getMsgId() {
        return request != null ? request.getMsgId() : 0;
    }
    
    /**
     * 获取平台ID
     *
     * @return 平台ID
     */
    public long getPlatformId() {
        return session != null ? session.getPlatformId() : 0;
    }
    
    /**
     * 检查是否有异常
     *
     * @return true-有异常，false-无异常
     */
    public boolean hasException() {
        return exception != null;
    }
}
