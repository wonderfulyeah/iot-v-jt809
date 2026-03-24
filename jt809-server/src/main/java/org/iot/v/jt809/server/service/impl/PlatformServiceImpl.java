package org.iot.v.jt809.server.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.session.Session;
import org.iot.v.jt809.server.service.PlatformService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 平台管理服务实现
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
public class PlatformServiceImpl implements PlatformService {
    
    /**
     * 平台会话映射 (platformId -> session)
     */
    private final Map<Long, Session> sessionMap = new ConcurrentHashMap<>();
    
    @Override
    public void registerSession(long platformId, Session session) {
        Session oldSession = sessionMap.put(platformId, session);
        if (oldSession != null) {
            log.warn("Platform {} session replaced, old session will be closed", platformId);
            // 关闭旧会话
            oldSession.close();
        }
        log.info("Platform {} session registered", platformId);
    }
    
    @Override
    public void unregisterSession(long platformId) {
        Session session = sessionMap.remove(platformId);
        if (session != null) {
            log.info("Platform {} session unregistered", platformId);
        }
    }
    
    @Override
    public Session getSession(long platformId) {
        return sessionMap.get(platformId);
    }
    
    @Override
    public List<Long> getOnlinePlatforms() {
        return new ArrayList<>(sessionMap.keySet());
    }
    
    @Override
    public boolean isOnline(long platformId) {
        Session session = sessionMap.get(platformId);
        return session != null && session.isActive();
    }
    
    @Override
    public int getOnlineCount() {
        return sessionMap.size();
    }
    
    @Override
    public Map<Long, Session> getAllSessions() {
        return new HashMap<>(sessionMap);
    }
}
