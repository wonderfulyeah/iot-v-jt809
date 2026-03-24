package org.iot.v.jt809.core.constant;

/**
 * 会话状态枚举
 *
 * @author haye
 * @date 2026-03-24
 */
public enum SessionState {
    
    /**
     * 已连接
     */
    CONNECTED("已连接"),
    
    /**
     * 已认证
     */
    AUTHENTICATED("已认证"),
    
    /**
     * 已断开
     */
    DISCONNECTED("已断开");
    
    private final String description;
    
    SessionState(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
