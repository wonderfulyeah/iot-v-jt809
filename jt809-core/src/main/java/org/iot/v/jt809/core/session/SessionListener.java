package org.iot.v.jt809.core.session;

/**
 * 会话监听器接口
 * 用于监听会话生命周期事件
 *
 * @author haye
 * @date 2026-03-24
 */
public interface SessionListener {
    
    /**
     * 会话创建时调用
     *
     * @param session 创建的会话
     */
    default void onSessionCreated(Session session) {}
    
    /**
     * 会话认证成功时调用
     *
     * @param session 认证的会话
     */
    default void onSessionAuthenticated(Session session) {}
    
    /**
     * 会话即将关闭时调用
     *
     * @param session 即将关闭的会话
     */
    default void onSessionClosing(Session session) {}
    
    /**
     * 会话已关闭时调用
     *
     * @param session 已关闭的会话
     */
    default void onSessionClosed(Session session) {}
    
    /**
     * 会话发生错误时调用
     *
     * @param session 发生错误的会话
     * @param cause   错误原因
     */
    default void onSessionError(Session session, Throwable cause) {}
    
    /**
     * 会话空闲时调用
     *
     * @param session 空闲的会话
     */
    default void onSessionIdle(Session session) {}
}
