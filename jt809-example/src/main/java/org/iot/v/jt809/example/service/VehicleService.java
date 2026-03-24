package org.iot.v.jt809.example.service;

import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.session.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 车辆服务示例
 * 演示如何发送消息到指定平台
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
@Service
public class VehicleService {
    
    @Autowired
    private SessionManager sessionManager;
    
    /**
     * 检查平台是否在线
     *
     * @param platformId 平台ID
     * @return true-在线，false-离线
     */
    public boolean isPlatformOnline(long platformId) {
        return sessionManager.isPlatformOnline(platformId);
    }
    
    /**
     * 获取在线平台数量
     *
     * @return 在线平台数量
     */
    public int getOnlinePlatformCount() {
        return sessionManager.getSessionCount();
    }
    
    /**
     * 发送消息到指定平台
     *
     * @param platformId 平台ID
     * @param message 消息对象
     * @return true-发送成功，false-发送失败
     */
    public boolean sendMessage(long platformId, Object message) {
        // TODO: 实现消息发送逻辑
        // return sessionManager.sendToPlatform(platformId, message);
        log.info("发送消息到平台: platformId={}", platformId);
        return false;
    }
}
