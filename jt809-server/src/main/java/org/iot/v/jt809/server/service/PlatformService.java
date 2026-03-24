package org.iot.v.jt809.server.service;

import org.iot.v.jt809.core.session.Session;

import java.util.List;
import java.util.Map;

/**
 * 平台管理服务接口
 *
 * @author haye
 * @date 2026-03-24
 */
public interface PlatformService {
    
    /**
     * 注册平台会话
     *
     * @param platformId 平台ID
     * @param session    会话
     */
    void registerSession(long platformId, Session session);
    
    /**
     * 注销平台会话
     *
     * @param platformId 平台ID
     */
    void unregisterSession(long platformId);
    
    /**
     * 获取平台会话
     *
     * @param platformId 平台ID
     * @return 会话
     */
    Session getSession(long platformId);
    
    /**
     * 获取所有在线平台
     *
     * @return 平台ID列表
     */
    List<Long> getOnlinePlatforms();
    
    /**
     * 检查平台是否在线
     *
     * @param platformId 平台ID
     * @return true-在线, false-离线
     */
    boolean isOnline(long platformId);
    
    /**
     * 获取在线平台数量
     *
     * @return 在线数量
     */
    int getOnlineCount();
    
    /**
     * 获取所有平台会话
     *
     * @return 平台ID与会话映射
     */
    Map<Long, Session> getAllSessions();
}
