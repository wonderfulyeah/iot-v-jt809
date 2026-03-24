package org.iot.v.jt809.core.session;

import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.message.base.BaseMessage;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话管理器
 * 单例模式，管理所有连接会话
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
public class SessionManager {
    
    /**
     * 单例实例
     */
    private static final SessionManager INSTANCE = new SessionManager();
    
    /**
     * 会话映射表（sessionId -> Session）
     */
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    
    /**
     * 平台ID映射表（platformId -> Session）
     */
    private final Map<Long, Session> platformSessions = new ConcurrentHashMap<>();
    
    private SessionManager() {
    }
    
    /**
     * 获取单例实例
     *
     * @return SessionManager实例
     */
    public static SessionManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 添加会话
     *
     * @param session 会话对象
     */
    public void addSession(Session session) {
        if (session == null) {
            return;
        }
        
        sessions.put(session.getSessionId(), session);
        
        // 如果已认证，添加到平台会话映射
        if (session.getPlatformId() > 0) {
            platformSessions.put(session.getPlatformId(), session);
        }
        
        log.info("Session added: sessionId={}, platformId={}, remoteAddress={}", 
            session.getSessionId(), session.getPlatformId(), session.getRemoteAddress());
    }
    
    /**
     * 移除会话
     *
     * @param sessionId 会话ID
     */
    public void removeSession(String sessionId) {
        Session session = sessions.remove(sessionId);
        
        if (session != null) {
            if (session.getPlatformId() > 0) {
                platformSessions.remove(session.getPlatformId());
            }
            
            log.info("Session removed: sessionId={}, platformId={}", 
                session.getSessionId(), session.getPlatformId());
        }
    }
    
    /**
     * 根据会话ID获取会话
     *
     * @param sessionId 会话ID
     * @return Session对象
     */
    public Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    /**
     * 根据平台ID获取会话
     *
     * @param platformId 平台ID
     * @return Session对象
     */
    public Session getSessionByPlatformId(long platformId) {
        return platformSessions.get(platformId);
    }
    
    /**
     * 获取所有会话
     *
     * @return 会话集合
     */
    public Collection<Session> getAllSessions() {
        return sessions.values();
    }
    
    /**
     * 获取会话数量
     *
     * @return 会话数量
     */
    public int getSessionCount() {
        return sessions.size();
    }
    
    /**
     * 广播消息给所有会话
     *
     * @param message 消息对象
     */
    public void broadcast(BaseMessage message) {
        sessions.values().forEach(session -> {
            if (session.isActive()) {
                session.send(message);
            }
        });
        
        log.debug("Broadcast message to {} sessions", sessions.size());
    }
    
    /**
     * 向指定平台发送消息
     *
     * @param platformId 平台ID
     * @param message 消息对象
     * @return true-发送成功, false-发送失败
     */
    public boolean sendToPlatform(long platformId, BaseMessage message) {
        Session session = getSessionByPlatformId(platformId);
        
        if (session != null && session.isActive()) {
            session.send(message);
            return true;
        }
        
        log.warn("Send message failed: platformId={} not found or inactive", platformId);
        return false;
    }
    
    /**
     * 清空所有会话
     */
    public void clear() {
        sessions.clear();
        platformSessions.clear();
        log.info("All sessions cleared");
    }
    
    /**
     * 检查会话是否存在
     *
     * @param sessionId 会话ID
     * @return true-存在, false-不存在
     */
    public boolean containsSession(String sessionId) {
        return sessions.containsKey(sessionId);
    }
    
    /**
     * 检查平台是否在线
     *
     * @param platformId 平台ID
     * @return true-在线, false-离线
     */
    public boolean isPlatformOnline(long platformId) {
        Session session = platformSessions.get(platformId);
        return session != null && session.isActive();
    }
}
